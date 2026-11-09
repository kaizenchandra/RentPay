package in.lmt.commercial;

import in.lmt.shared.ApiProblem;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Plan eligibility only: callers must separately authorize the actor/resource operation. */
@Service
@Transactional(propagation=Propagation.MANDATORY)
public class Entitlements {
 private final JdbcClient db;
 public Entitlements(JdbcClient db){this.db=db;}
 public record Decision(String code,String label,boolean eligible,String reason,int policyVersion,UUID planVersionId,String source,Integer overrideRevision,java.time.Instant overrideExpiresAt){}
 public List<Decision> evaluate(UUID workspace){
  var state=db.sql("SELECT w.status,s.status AS subscription_status,s.plan_version_id FROM workspace w JOIN workspace_subscription s ON s.workspace_id=w.id WHERE w.id=:w")
   .param("w",workspace).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"WORKSPACE_NOT_FOUND","Workspace is unavailable."));
  var policy=db.sql("SELECT version FROM commercial_policy WHERE effective_at<=CURRENT_TIMESTAMP ORDER BY effective_at DESC,version DESC LIMIT 1").query(Integer.class).optional().orElseThrow(()->new ApiProblem(503,"ENTITLEMENT_CONFIGURATION_UNAVAILABLE","Commercial policy is unavailable. Retry later."));
  return db.sql("SELECT c.*,o.effect AS override_effect,o.revision AS override_revision,o.expires_at AS override_expiry,EXISTS(SELECT 1 FROM commercial_plan_capability p WHERE p.policy_version=c.policy_version AND p.capability=c.code AND p.plan_version_id=:plan) AS included FROM commercial_capability c LEFT JOIN workspace_capability_override o ON o.workspace_id=:workspace AND o.capability=c.code AND o.expires_at>clock_timestamp() WHERE c.policy_version=:v ORDER BY c.code").param("v",policy).param("workspace",workspace).param("plan",state.get("plan_version_id")).query((r,n)->{
   String reason="ELIGIBLE";String status=(String)state.get("status");
   if(!r.getBoolean("included"))reason="CAPABILITY_NOT_CONFIGURED";
   String effect=r.getString("override_effect");if(effect!=null)reason=effect.equals("allow")?"ELIGIBLE":"COMMERCIAL_OVERRIDE_DENIED";
   if(!state.get("subscription_status").equals("active_free"))reason="SUBSCRIPTION_UNAVAILABLE";
   if(status.equals("restricted")&&!r.getBoolean("allow_restricted"))reason="WORKSPACE_RESTRICTED";
   if(status.equals("suspended")&&!r.getBoolean("allow_suspended"))reason="SECURITY_SUSPENDED";
   return new Decision(r.getString("code"),r.getString("label"),reason.equals("ELIGIBLE"),reason,policy,(UUID)state.get("plan_version_id"),effect==null?"plan":"temporary_override",(Integer)r.getObject("override_revision"),r.getTimestamp("override_expiry")==null?null:r.getTimestamp("override_expiry").toInstant());
  }).list();
 }
 public void require(UUID workspace,String capability){
  var decision=evaluate(workspace).stream().filter(d->d.code().equals(capability)).findFirst().orElseThrow(()->new ApiProblem(503,"ENTITLEMENT_CONFIGURATION_UNAVAILABLE","Capability policy is unavailable."));
  if(!decision.eligible())throw new ApiProblem(409,decision.reason(),"This workspace cannot add manager membership under its current eligibility decision. Existing access can still be revoked.");
 }
}
