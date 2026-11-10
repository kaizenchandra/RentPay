package in.lmt.commercial;
import in.lmt.identity.*;
import in.lmt.shared.*;
import in.lmt.workspace.WorkspaceCommands;
import java.util.*;
import java.time.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
/** Explicit local callback adapter. This is not an authenticated live payment provider integration. */
@Service @Transactional
public class SubscriptionSettlements {
 private final JdbcClient db;private final WorkspaceCommands scope;private final IdentityConfig config;private final SubscriptionUpgrades upgrades;private final Audit audit;private final ObjectMapper json;
 public SubscriptionSettlements(JdbcClient db,WorkspaceCommands scope,IdentityConfig config,SubscriptionUpgrades upgrades,Audit audit,ObjectMapper json){this.db=db;this.scope=scope;this.config=config;this.upgrades=upgrades;this.audit=audit;this.json=json;}
 public record Claim(UUID id,UUID workspaceId,UUID actorId,UUID token){}
 private void lock(UUID actor,UUID w){scope.requireOwner(w,actor);db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k","membership:"+w).query().singleRow();db.sql("SELECT id FROM workspace_subscription WHERE workspace_id=:w FOR UPDATE").param("w",w).query(UUID.class).single();scope.requireOwner(w,actor);}
 private void sandbox(UUID w){if(!config.sandbox||!db.sql("SELECT mode='sandbox' FROM workspace WHERE id=:w").param("w",w).query(Boolean.class).single())throw new ApiProblem(409,"PAYMENT_PROVIDER_UNAVAILABLE","Only the explicitly local callback adapter is available.");}
 private void match(String expected,String actual){if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Refresh and review the current version.");if(!expected.equals(actual))throw new ApiProblem(412,"VERSION_STALE","The payment or receipt changed. Refresh before confirming.");}
 public SubscriptionUpgrades.Change callback(UUID actor,UUID w,UUID id,UUID event,String expected,String outcome){
  lock(actor,w);sandbox(w);var prior=db.sql("SELECT outcome,result::text FROM subscription_callback_event WHERE workspace_id=:w AND change_id=:id AND event_key=:e").param("w",w).param("id",id).param("e",event).query().listOfRows();
  if(!prior.isEmpty()){if(!prior.getFirst().get("outcome").equals(outcome))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This callback event has different evidence.");return json.readValue((String)prior.getFirst().get("result"),SubscriptionUpgrades.Change.class);}
  var c=upgrades.get(actor,w,id);match(expected,c.etag());if(!Set.of("pending","paid","reversed").contains(outcome))throw new ApiProblem(422,"INVALID_OUTCOME","Choose a local callback outcome.","outcome");
  if(outcome.equals("pending")&&c.settlement()==null&&!Set.of("applied","payment_failed").contains(c.status()))db.sql("UPDATE subscription_upgrade_change SET status='payment_unknown',version=version+1 WHERE id=:id").param("id",id).update();
  if(outcome.equals("paid")&&c.settlement()==null){
   if(!c.status().equals("applied")&&db.sql("SELECT count(*) FROM subscription_upgrade_change WHERE workspace_id=:w AND id<>:id AND status IN('awaiting_payment','payment_unknown','review_required')").param("w",w).param("id",id).query(Long.class).single()>0)throw new ApiProblem(409,"SUBSCRIPTION_CHANGE_PENDING","This local simulation cannot start another settlement while a competing upgrade is pending.");
   String status=c.status().equals("applied")?"applied":c.status().equals("payment_failed")||!Instant.now().isBefore(c.quote().expiresAt())?"review_required":"queued";
   if(!c.status().equals("applied"))db.sql("INSERT INTO subscription_sandbox_payment_event(id,workspace_id,change_id,outcome,amount,currency,purpose,attempt_number) VALUES(:e,:w,:id,'paid',:amount,'INR','workspace_saas_upgrade',:attempt)").param("e",UUID.randomUUID()).param("w",w).param("id",id).param("amount",new java.math.BigDecimal(c.quote().total())).param("attempt",c.retry().attemptNumber(),java.sql.Types.INTEGER).update();
   db.sql("INSERT INTO subscription_settlement(id,workspace_id,actor_id,status,received_at,applied_at,last_code) VALUES(:id,:w,:a,:s,(SELECT min(created_at) FROM subscription_sandbox_payment_event WHERE change_id=:id AND outcome='paid'),:applied,:code)").param("id",id).param("w",w).param("a",actor).param("s",status).param("applied",c.appliedAt()==null?null:OffsetDateTime.ofInstant(c.appliedAt(),ZoneOffset.UTC),java.sql.Types.TIMESTAMP_WITH_TIMEZONE).param("code",status.equals("review_required")?"PAYMENT_TERMS_REVIEW_REQUIRED":null,java.sql.Types.VARCHAR).update();
   if(!status.equals("applied"))db.sql("UPDATE subscription_upgrade_change SET status='review_required',version=version+1 WHERE id=:id").param("id",id).update();audit.record(actor,w,"subscription.payment_received",id);
  }
  if(outcome.equals("reversed")){
   if(c.settlement()==null)throw new ApiProblem(409,"SETTLEMENT_NOT_FOUND","Record the original received payment before a reversal; do not invent a receipt.");
   if(!c.settlement().status().equals("reversal_review")){db.sql("UPDATE subscription_settlement SET status='reversal_review',version=version+1,reversed_at=clock_timestamp(),lease_token=NULL,lease_until=NULL,last_code='PAYMENT_REVERSED' WHERE id=:id").param("id",id).update();audit.record(actor,w,"subscription.payment_reversed_review",id);}
  }
  var result=upgrades.get(actor,w,id);db.sql("INSERT INTO subscription_callback_event(id,workspace_id,change_id,actor_id,event_key,outcome,result) VALUES(:e,:w,:id,:a,:key,:o,CAST(:r AS jsonb))").param("e",UUID.randomUUID()).param("w",w).param("id",id).param("a",actor).param("key",event).param("o",outcome).param("r",json.writeValueAsString(result)).update();audit.record(actor,w,"subscription.sandbox_callback_"+outcome,id);return result;
 }
 public SubscriptionUpgrades.Change retry(UUID actor,UUID w,UUID id,UUID key,String expected){
  lock(actor,w);sandbox(w);String operation="settlement-retry:"+id,hash=ChallengeService.hash(json.writeValueAsString(Collections.singletonList(expected)));var old=db.sql("SELECT request_hash,result::text FROM subscription_upgrade_command WHERE workspace_id=:w AND actor_id=:a AND operation=:o AND request_key=:k").param("w",w).param("a",actor).param("o",operation).param("k",key).query().listOfRows();if(!old.isEmpty()){if(!old.getFirst().get("request_hash").equals(hash))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This retry key has different terms.");return json.readValue((String)old.getFirst().get("result"),SubscriptionUpgrades.Change.class);}
  var c=upgrades.get(actor,w,id);if(c.settlement()==null)throw new ApiProblem(404,"SETTLEMENT_NOT_FOUND","Receipt is unavailable.");match(expected,c.settlement().etag());if(!c.settlement().status().equals("review_required")||!Set.of("JOB_FAILED","AUTHORITY_REVOKED","SECURITY_SUSPENDED","MODE_UNAVAILABLE").contains(c.settlement().lastCode()))throw new ApiProblem(409,"ACTIVATION_REVIEW_REQUIRED","Only a resolved transient activation failure can be retried. Reversals and changed payment terms require operator reconciliation; no new payment is authorized.");
  if(!db.sql("SELECT lmt_is_workspace_owner(workspace_id,actor_id) FROM subscription_settlement WHERE id=:id").param("id",id).query(Boolean.class).single())throw new ApiProblem(409,"AUTHORITY_REVOKED","Restore valid original authority before retrying; the receipt is preserved.");
  db.sql("UPDATE subscription_settlement SET status='queued',version=version+1,attempts=0,next_attempt_at=clock_timestamp(),last_code=NULL,lease_token=NULL,lease_until=NULL WHERE id=:id").param("id",id).update();audit.record(actor,w,"subscription.activation_retry_requested",id);var result=upgrades.get(actor,w,id);db.sql("INSERT INTO subscription_upgrade_command(actor_id,workspace_id,operation,request_key,request_hash,result) VALUES(:a,:w,:o,:k,:h,CAST(:r AS jsonb))").param("a",actor).param("w",w).param("o",operation).param("k",key).param("h",hash).param("r",json.writeValueAsString(result)).update();return result;
 }
 public List<Claim> claim(){return db.sql("SELECT * FROM lmt_claim_subscription_settlements()").query((r,n)->new Claim(r.getObject("id",UUID.class),r.getObject("workspace_id",UUID.class),r.getObject("actor_id",UUID.class),r.getObject("token",UUID.class))).list();}
 public void fail(Claim c,String code){db.sql("SELECT lmt_fail_subscription_settlement(:id,:t,:code)").param("id",c.id()).param("t",c.token()).param("code",code).query().singleRow();}
 public void apply(Claim c){
  scope.scope(c.actorId());db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k","membership:"+c.workspaceId()).query().singleRow();if(!db.sql("SELECT lmt_is_workspace_owner(:w,:a)").param("w",c.workspaceId()).param("a",c.actorId()).query(Boolean.class).single()){fail(c,"AUTHORITY_REVOKED");return;}
  lock(c.actorId(),c.workspaceId());var row=db.sql("SELECT * FROM subscription_settlement WHERE id=:id FOR UPDATE").param("id",c.id()).query().singleRow();if(!row.get("status").equals("queued")||!c.token().equals(row.get("lease_token"))||((java.sql.Timestamp)row.get("lease_until")).toInstant().isBefore(Instant.now()))return;
  if(!config.sandbox||!db.sql("SELECT mode='sandbox' FROM workspace WHERE id=:w").param("w",c.workspaceId()).query(Boolean.class).single()){fail(c,"MODE_UNAVAILABLE");return;}if(db.sql("SELECT status='suspended' FROM workspace WHERE id=:w").param("w",c.workspaceId()).query(Boolean.class).single()){fail(c,"SECURITY_SUSPENDED");return;}
  upgrades.activateReceived(c.actorId(),c.workspaceId(),c.id(),c.token(),((java.sql.Timestamp)row.get("received_at")).toInstant());
  db.sql("UPDATE subscription_settlement SET status='applied',version=version+1,applied_at=clock_timestamp(),lease_token=NULL,lease_until=NULL,last_code=NULL WHERE id=:id").param("id",c.id()).update();audit.record(c.actorId(),c.workspaceId(),"subscription.activation_applied",c.id());
 }
}
