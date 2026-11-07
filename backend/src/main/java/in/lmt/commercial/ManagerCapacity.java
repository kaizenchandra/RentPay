package in.lmt.commercial;

import in.lmt.shared.ApiProblem;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Internal commercial commands; caller authorizes membership and supplies its transaction. */
@Service
@Transactional(propagation=Propagation.MANDATORY)
public class ManagerCapacity {
 private final JdbcClient db;
 public ManagerCapacity(JdbcClient db){this.db=db;}
 public void consume(UUID workspace,UUID member,UUID invitation){
  int recorded=db.sql("SELECT used FROM workspace_grant WHERE workspace_id=:w AND resource='managers' FOR UPDATE").param("w",workspace).query(Integer.class).single();
  long allocated=db.sql("SELECT count(*) FROM manager_allocation WHERE workspace_id=:w AND status='committed'").param("w",workspace).query(Long.class).single();
  long events=db.sql("SELECT coalesce(sum(quantity),0) FROM manager_usage_event WHERE workspace_id=:w").param("w",workspace).query(Long.class).single();
  if(recorded!=allocated||recorded!=events)throw new ApiProblem(409,"CAPACITY_RECONCILIATION_REQUIRED","Manager usage disagrees with allocation evidence. Ask an operator to review it; no membership was added.");
  // Reserve the last slot under the grant row lock, then consume in the same atomic transaction.
  int reserved=db.sql("UPDATE workspace_grant SET reserved=reserved+1 WHERE workspace_id=:w AND resource='managers' AND used+reserved<allowance")
   .param("w",workspace).update();
  if(reserved!=1)throw new ApiProblem(409,"QUOTA_EXCEEDED","No manager slot is available. Ask the owner to review existing managers; this invitation has not been accepted.");
  db.sql("INSERT INTO manager_allocation(id,workspace_id,member_id,status) VALUES(:id,:w,:m,'committed')")
   .param("id",invitation).param("w",workspace).param("m",member).update();
  db.sql("INSERT INTO manager_usage_event(id,workspace_id,allocation_id,kind,quantity,occurred_at) SELECT :event,workspace_id,id,'committed',1,committed_at FROM manager_allocation WHERE id=:id")
   .param("event",UUID.randomUUID()).param("id",invitation).update();
  db.sql("UPDATE workspace_grant SET used=used+1,reserved=reserved-1 WHERE workspace_id=:w AND resource='managers'").param("w",workspace).update();
 }
 public void release(UUID workspace,UUID member){
  int released=db.sql("UPDATE manager_allocation SET status='released',released_at=CURRENT_TIMESTAMP WHERE workspace_id=:w AND member_id=:m AND status='committed'")
   .param("w",workspace).param("m",member).update();
  if(released!=1)throw new ApiProblem(409,"CAPACITY_RECONCILIATION_REQUIRED","Manager capacity needs operator review. No access change was committed.");
  db.sql("INSERT INTO manager_usage_event(id,workspace_id,allocation_id,kind,quantity,occurred_at) SELECT :event,workspace_id,id,'released',-1,released_at FROM manager_allocation WHERE workspace_id=:w AND member_id=:m AND status='released' AND NOT EXISTS(SELECT 1 FROM manager_usage_event e WHERE e.allocation_id=manager_allocation.id AND e.kind='released')")
   .param("event",UUID.randomUUID()).param("w",workspace).param("m",member).update();
  int adjusted=db.sql("UPDATE workspace_grant SET used=used-1 WHERE workspace_id=:w AND resource='managers' AND used>0").param("w",workspace).update();
  if(adjusted!=1)throw new ApiProblem(409,"CAPACITY_RECONCILIATION_REQUIRED","Manager capacity needs operator review. No access change was committed.");
 }
}
