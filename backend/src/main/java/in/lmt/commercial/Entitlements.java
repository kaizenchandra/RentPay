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
 public record Contribution(UUID id,String kind,String effect,java.time.Instant startsAt,java.time.Instant expiresAt){}
 public record Decision(String code,String label,boolean eligible,String reason,int policyVersion,UUID planVersionId,String source,Integer overrideRevision,java.time.Instant overrideExpiresAt,boolean operational,List<Contribution> contributions){}
 public List<Decision> evaluate(UUID workspace){return preview(workspace,null);}
 public List<Decision> preview(UUID workspace,UUID targetPlan){return previewAt(workspace,targetPlan,java.time.Instant.now());}
 public List<Decision> previewAt(UUID workspace,UUID targetPlan,java.time.Instant at){
  var state=db.sql("SELECT w.status,w.mode,s.status AS subscription_status,s.plan_version_id FROM workspace w JOIN workspace_subscription s ON s.workspace_id=w.id WHERE w.id=:w")
   .param("w",workspace).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"WORKSPACE_NOT_FOUND","Workspace is unavailable."));
  if(targetPlan!=null)state.put("plan_version_id",targetPlan);
  var policy=db.sql("SELECT version FROM commercial_policy WHERE effective_at<=:at ORDER BY effective_at DESC,version DESC LIMIT 1").param("at",java.sql.Timestamp.from(at)).query(Integer.class).optional().orElseThrow(()->new ApiProblem(503,"ENTITLEMENT_CONFIGURATION_UNAVAILABLE","Commercial policy is unavailable. Retry later."));
  var inputs=db.sql("SELECT i.* FROM workspace_entitlement_input i WHERE i.workspace_id=:w AND i.starts_at<=:at AND i.expires_at>:at AND (i.plan_version_id IS NULL OR i.plan_version_id=:plan) AND NOT EXISTS(SELECT 1 FROM workspace_entitlement_revocation r WHERE r.input_id=i.id) ORDER BY i.id").param("w",workspace).param("at",java.sql.Timestamp.from(at)).param("plan",state.get("plan_version_id")).query().listOfRows();
  var decisions=db.sql("SELECT c.*,o.effect AS override_effect,o.revision AS override_revision,o.expires_at AS override_expiry,EXISTS(SELECT 1 FROM commercial_plan_capability p WHERE p.policy_version=c.policy_version AND p.capability=c.code AND p.plan_version_id=:plan) AS included FROM commercial_capability c LEFT JOIN workspace_capability_override o ON o.workspace_id=:workspace AND o.capability=c.code AND o.expires_at>:at WHERE c.policy_version=:v ORDER BY c.operational DESC,c.code").param("at",java.sql.Timestamp.from(at)).param("v",policy).param("workspace",workspace).param("plan",state.get("plan_version_id")).query((r,n)->{
   String reason="ELIGIBLE";String status=(String)state.get("status");
   if(!r.getBoolean("included"))reason="CAPABILITY_NOT_CONFIGURED";
   String code=r.getString("code"),source="plan";var contributions=inputs.stream().filter(i->i.get("capability").equals(code)&&i.get("mode").equals(state.get("mode"))).map(i->new Contribution((UUID)i.get("id"),(String)i.get("kind"),(String)i.get("effect"),((java.sql.Timestamp)i.get("starts_at")).toInstant(),((java.sql.Timestamp)i.get("expires_at")).toInstant())).toList();
   if(!contributions.isEmpty()){
    boolean deny=!r.getBoolean("protected_correction")&&contributions.stream().anyMatch(i->i.kind().equals("commercial_override")&&i.effect().equals("deny"));
    if(deny){reason="COMMERCIAL_OVERRIDE_DENIED";source="commercial_override";}
    else if(contributions.stream().anyMatch(i->i.kind().equals("commercial_override")&&i.effect().equals("allow"))){reason="ELIGIBLE";source="commercial_override";}
    else if(!r.getBoolean("included")&&contributions.stream().anyMatch(i->Set.of("add_on","timed_grant").contains(i.kind())&&i.effect().equals("allow"))){reason="ELIGIBLE";source=contributions.stream().anyMatch(i->i.kind().equals("add_on"))?"add_on":"timed_grant";}
   }
   String effect=r.getString("override_effect");if(effect!=null){if(effect.equals("deny")||!reason.equals("COMMERCIAL_OVERRIDE_DENIED")){reason=effect.equals("allow")?"ELIGIBLE":"COMMERCIAL_OVERRIDE_DENIED";source="temporary_override";}}
   if(!Set.of("active_free","active_paid").contains(state.get("subscription_status")))reason="SUBSCRIPTION_UNAVAILABLE";
   if(status.equals("restricted")&&!r.getBoolean("allow_restricted"))reason="WORKSPACE_RESTRICTED";
   if(status.equals("suspended")&&!r.getBoolean("allow_suspended"))reason="SECURITY_SUSPENDED";
   if(!r.getBoolean("operational"))reason="FEATURE_UNAVAILABLE";
   return new Decision(r.getString("code"),r.getString("label"),reason.equals("ELIGIBLE"),reason,policy,(UUID)state.get("plan_version_id"),source,(Integer)r.getObject("override_revision"),r.getTimestamp("override_expiry")==null?null:r.getTimestamp("override_expiry").toInstant(),r.getBoolean("operational"),contributions);
  }).list();
  // Product gating applies to discretionary property expansion, not required corrections/releases.
  var property=decisions.stream().filter(d->d.code().equals("PROPERTY_MANAGEMENT")).findFirst();
  return decisions.stream().map(d->{if(property.isPresent()&&!property.get().eligible()&&Set.of("PROPERTY_CREATE","RENTABLE_CREATE").contains(d.code())&&d.eligible())return new Decision(d.code(),d.label(),false,"PARENT_CAPABILITY_DENIED",d.policyVersion(),d.planVersionId(),d.source(),d.overrideRevision(),d.overrideExpiresAt(),d.operational(),d.contributions());return d;}).toList();
 }
 public Decision evaluate(UUID workspace,String capability){return evaluate(workspace).stream().filter(d->d.code().equals(capability)).findFirst().orElseThrow(()->new ApiProblem(422,"UNKNOWN_CAPABILITY","Select a configured capability.","capability"));}

 public void require(UUID workspace,String capability){
  var decision=evaluate(workspace).stream().filter(d->d.code().equals(capability)).findFirst().orElseThrow(()->new ApiProblem(503,"ENTITLEMENT_CONFIGURATION_UNAVAILABLE","Capability policy is unavailable."));
  if(!decision.eligible())throw new ApiProblem(409,decision.reason(),"This action is unavailable under the current workspace eligibility decision: "+decision.label()+".");
 }
}
