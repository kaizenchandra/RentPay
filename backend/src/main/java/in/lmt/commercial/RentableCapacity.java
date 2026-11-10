package in.lmt.commercial;
import in.lmt.shared.ApiProblem;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service @Transactional(propagation=Propagation.MANDATORY)
public class RentableCapacity {
 private final JdbcClient db;public RentableCapacity(JdbcClient db){this.db=db;}
 private java.util.Map<String,Object> policy(UUID w){return db.sql("SELECT p.version,p.shared_mode,p.description,w.mode FROM workspace_counting_policy b JOIN rentable_counting_policy p ON p.version=b.policy_version JOIN workspace w ON w.id=b.workspace_id WHERE b.workspace_id=:w").param("w",w).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(503,"COUNTING_POLICY_REQUIRED","Workspace counting policy is unavailable."));}
 public String sharedMode(UUID w){return (String)policy(w).get("shared_mode");}
 public boolean sharedAvailable(UUID w){var p=policy(w);return !p.get("shared_mode").equals("unconfigured")&&p.get("mode").equals("sandbox");}
 public boolean counted(UUID w,String kind,String mode){return db.sql("SELECT lmt_rentable_quantity(:kind,:mode,:policy)>0").param("kind",kind).param("mode",mode).param("policy",sharedMode(w)).query(Boolean.class).single();}
 public record Status(int allowance,int used,int reserved,Integer remaining,String status,int countingPolicyVersion,String definition){}
 public Status status(UUID w){return inspect(w,null);}
 private Status inspect(UUID w,UUID pending){
  var grant=db.sql("SELECT allowance,used,reserved FROM workspace_grant WHERE workspace_id=:w AND resource='rentable_items'").param("w",w).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(503,"ENTITLEMENT_CONFIGURATION_UNAVAILABLE","Rentable allowance is not configured."));
  var policy=policy(w);
  long allocated=db.sql("SELECT count(*) FROM rentable_allocation WHERE workspace_id=:w AND status='committed'").param("w",w).query(Long.class).single();
  long balance=db.sql("SELECT coalesce(sum(quantity),0) FROM rentable_usage_event WHERE workspace_id=:w").param("w",w).query(Long.class).single();
  long inventory=db.sql("SELECT count(*) FROM property_space WHERE workspace_id=:w AND status<>'archived' AND id<>:pending AND lmt_rentable_quantity(kind,rental_mode,:mode)>0").param("w",w).param("pending",pending==null?new UUID(0,0):pending).param("mode",policy.get("shared_mode")).query(Long.class).single();
  long inconsistent=db.sql("SELECT count(*) FROM rentable_allocation a WHERE a.workspace_id=:w AND ((a.status='committed' AND (SELECT coalesce(sum(e.quantity),0) FROM rentable_usage_event e WHERE e.allocation_id=a.id)<>1) OR (a.status='released' AND (SELECT coalesce(sum(e.quantity),0) FROM rentable_usage_event e WHERE e.allocation_id=a.id)<>0))").param("w",w).query(Long.class).single();
  long mapped=db.sql("SELECT count(*) FROM rentable_allocation a JOIN property_space s ON s.workspace_id=a.workspace_id AND s.id=a.resource_id WHERE a.workspace_id=:w AND a.status='committed' AND s.status<>'archived' AND lmt_rentable_quantity(s.kind,s.rental_mode,:mode)>0").param("w",w).param("mode",policy.get("shared_mode")).query(Long.class).single();
  int allowance=((Number)grant.get("allowance")).intValue(),used=((Number)grant.get("used")).intValue(),reserved=((Number)grant.get("reserved")).intValue();
  boolean reconciled=allocated==used&&mapped==allocated&&balance==used&&inventory==used&&inconsistent==0&&reserved==0;
  int remaining=Math.max(0,allowance-used-reserved);
  String state=!reconciled?"RECONCILIATION_REQUIRED":used+reserved>allowance?"OVER_LIMIT":remaining==0?"AT_LIMIT":(long)(used+reserved)*5>=(long)allowance*4?"NEAR_LIMIT":"AVAILABLE";
  return new Status(allowance,used,reserved,reconciled?remaining:null,state,((Number)policy.get("version")).intValue(),(String)policy.get("description"));
 }
 private void requireReconciled(UUID w,UUID pending){if(inspect(w,pending).status().equals("RECONCILIATION_REQUIRED"))throw new ApiProblem(409,"CAPACITY_RECONCILIATION_REQUIRED","Rentable inventory, allocation evidence and counters need review. No capacity change was committed.");}
 public void consume(UUID w,UUID resource){
  db.sql("SELECT used FROM workspace_grant WHERE workspace_id=:w AND resource='rentable_items' FOR UPDATE").param("w",w).query().singleRow();
  requireReconciled(w,resource);
  if(db.sql("UPDATE workspace_grant SET used=used+1 WHERE workspace_id=:w AND resource='rentable_items' AND used+reserved<allowance").param("w",w).update()!=1)throw new ApiProblem(409,"QUOTA_EXCEEDED","No rentable-item allowance remains. Review inventory and the subscribed allowance.");
  UUID id=UUID.randomUUID();db.sql("INSERT INTO rentable_allocation(id,workspace_id,resource_id,status) VALUES(:id,:w,:r,'committed')").param("id",id).param("w",w).param("r",resource).update();
  db.sql("INSERT INTO rentable_usage_event(id,workspace_id,allocation_id,quantity) VALUES(:e,:w,:id,1)").param("e",UUID.randomUUID()).param("w",w).param("id",id).update();
 }
 public void release(UUID w,UUID resource){
  db.sql("SELECT used FROM workspace_grant WHERE workspace_id=:w AND resource='rentable_items' FOR UPDATE").param("w",w).query().singleRow();
  requireReconciled(w,null);
  var rows=db.sql("UPDATE rentable_allocation SET status='released',released_at=clock_timestamp() WHERE workspace_id=:w AND resource_id=:r AND status='committed' RETURNING id").param("w",w).param("r",resource).query().listOfRows();
  if(rows.size()!=1)throw new ApiProblem(409,"CAPACITY_RECONCILIATION_REQUIRED","Rentable allocation evidence is missing. Archive was not committed.");
  if(db.sql("UPDATE workspace_grant SET used=used-1 WHERE workspace_id=:w AND resource='rentable_items' AND used>0").param("w",w).update()!=1)throw new ApiProblem(409,"CAPACITY_RECONCILIATION_REQUIRED","Rentable counters require review.");
  db.sql("INSERT INTO rentable_usage_event(id,workspace_id,allocation_id,quantity) VALUES(:e,:w,:id,-1)").param("e",UUID.randomUUID()).param("w",w).param("id",rows.getFirst().get("id")).update();
 }
}
