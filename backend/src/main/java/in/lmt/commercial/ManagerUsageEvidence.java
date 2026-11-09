package in.lmt.commercial;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Commercial evidence only; membership visibility remains the workspace module's responsibility. */
@Service
@Transactional(propagation=Propagation.MANDATORY)
public class ManagerUsageEvidence {
 private final JdbcClient db;
 public ManagerUsageEvidence(JdbcClient db){this.db=db;}
 public record Evidence(long committedAllocations,long eventBalance,long inconsistentAllocations,long unmatchedEvents){
  public boolean matches(int used,int reserved){return used==committedAllocations&&used==eventBalance&&inconsistentAllocations==0&&unmatchedEvents==0&&reserved==0;}
 }
 public Evidence read(UUID workspace){
  return db.sql("""
   SELECT
    (SELECT count(*) FROM manager_allocation WHERE workspace_id=:w AND status='committed') AS allocations,
    (SELECT coalesce(sum(quantity),0) FROM manager_usage_event WHERE workspace_id=:w) AS balance,
    (SELECT count(*) FROM manager_allocation a WHERE a.workspace_id=:w AND (
      NOT EXISTS(SELECT 1 FROM manager_usage_event e WHERE e.allocation_id=a.id AND e.workspace_id=a.workspace_id AND e.kind='committed')
      OR (a.status='released') <> EXISTS(SELECT 1 FROM manager_usage_event e WHERE e.allocation_id=a.id AND e.workspace_id=a.workspace_id AND e.kind='released')
    )) AS inconsistent,
    (SELECT count(*) FROM manager_usage_event e WHERE e.workspace_id=:w AND NOT EXISTS(
      SELECT 1 FROM manager_allocation a WHERE a.id=e.allocation_id AND a.workspace_id=e.workspace_id
    )) AS unmatched
   """).param("w",workspace).query((r,n)->new Evidence(r.getLong("allocations"),r.getLong("balance"),r.getLong("inconsistent"),r.getLong("unmatched"))).single();
 }
}
