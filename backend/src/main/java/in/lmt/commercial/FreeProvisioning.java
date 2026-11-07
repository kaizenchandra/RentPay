package in.lmt.commercial;
import in.lmt.shared.ApiProblem;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
@Service
public class FreeProvisioning {
 private final JdbcClient db;public FreeProvisioning(JdbcClient db){this.db=db;}
 public void provision(UUID workspace,UUID selected){
  var plan=db.sql("SELECT * FROM plan_version WHERE audience='workspace' AND code='FREE' AND publication_state='published' AND effective_at<=CURRENT_TIMESTAMP ORDER BY effective_at DESC LIMIT 1").query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(503,"PLAN_UNAVAILABLE","No Free plan is available."));
  if(!selected.equals(plan.get("version_id")))throw new ApiProblem(409,"PLAN_STALE","The selected Free plan changed. Reload the plan before confirming.");
  if(plan.get("rentable_limit")==null||plan.get("manager_limit")==null)throw new ApiProblem(503,"PLAN_UNAVAILABLE","Free allowances are not configured.");
  db.sql("INSERT INTO workspace_subscription(id,workspace_id,plan_version_id,status) VALUES(:id,:w,:p,'active_free')").param("id",UUID.randomUUID()).param("w",workspace).param("p",selected).update();
  for(String resource:new String[]{"rentable_items","managers"}){
   Object limit=plan.get(resource.equals("rentable_items")?"rentable_limit":"manager_limit");
   db.sql("INSERT INTO workspace_grant(workspace_id,resource,allowance) VALUES(:w,:r,:l)").param("w",workspace).param("r",resource).param("l",limit).update();
  }
 }
}
