package in.lmt.commercial;
import in.lmt.identity.IdentityConfig;
import in.lmt.shared.*;
import in.lmt.workspace.WorkspaceCommands;
import java.util.*;
import java.time.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service @Transactional
public class CommunicationAllowances {
 private final in.lmt.identity.ChallengeService challenges;private final JdbcClient db;private final WorkspaceCommands scope;private final IdentityConfig config;private final boolean localEnabled;private final Audit audit;
 public CommunicationAllowances(JdbcClient db,WorkspaceCommands scope,IdentityConfig config,Audit audit,@org.springframework.beans.factory.annotation.Value("${lmt.communication.local-enabled:${lmt.identity.local-containers:false}}")boolean localEnabled,in.lmt.identity.ChallengeService challenges){this.challenges=challenges;this.localEnabled=localEnabled;this.db=db;this.scope=scope;this.config=config;this.audit=audit;}
 public record Channel(String channel,UUID contractId,String metric,Integer allowance,Integer used,Integer reserved,Integer remaining,Instant startsAt,Instant endsAt,boolean available,String reason,String recipient){}
 public record Operation(UUID id,UUID contractId,String channel,String status,String code,Instant createdAt,int version,String etag){}
 public record Snapshot(UUID workspaceId,int contactVersion,List<Channel> channels,List<Operation> operations,List<Recovery> recoveries,Instant asOf,String disclosure){}
 public record Recovery(UUID id,UUID operationId,String status,String code,Instant createdAt){}
 public record Claim(UUID id,UUID workspaceId,UUID actorId,UUID token){}
 public record Dispatch(Claim claim,String channel,String destination){}
 private Instant instant(Object v){return ((java.sql.Timestamp)v).toInstant();}
 private void lock(UUID w){db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k","communication:"+w).query().singleRow();}
 private Map<String,Object> account(UUID actor){return db.sql("SELECT version,email,mobile FROM user_account WHERE id=:a").param("a",actor).query().singleRow();}
 private boolean permitted(UUID w,UUID plan){return config.sandbox&&localEnabled&&db.sql("SELECT EXISTS(SELECT 1 FROM workspace w JOIN workspace_subscription s ON s.workspace_id=w.id WHERE w.id=:w AND w.mode='sandbox' AND w.status='active' AND s.status IN('active_free','active_paid') AND s.plan_version_id=:p)").param("w",w).param("p",plan).query(Boolean.class).single();}
 private Map<String,Object> contract(UUID w,UUID id){return db.sql("SELECT * FROM communication_contract WHERE workspace_id=:w AND id=:id AND starts_at<=clock_timestamp() AND ends_at>clock_timestamp()").param("w",w).param("id",id).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(409,"COMMUNICATION_CONTRACT_UNAVAILABLE","Refresh the configured communication period."));}
 private int used(UUID id){return db.sql("SELECT count(*) FROM communication_usage_event e JOIN communication_contract c ON c.id=e.contract_id WHERE c.id=:id AND e.metric=c.metric").param("id",id).query(Integer.class).single();}
 private int reserved(UUID id){return db.sql("SELECT count(*) FROM communication_operation WHERE contract_id=:id AND status IN('queued','dispatching','unknown')").param("id",id).query(Integer.class).single();}
 private Operation operation(UUID w,UUID id){return db.sql("SELECT o.*,c.channel FROM communication_operation o JOIN communication_contract c ON c.id=o.contract_id WHERE o.workspace_id=:w AND o.id=:id").param("w",w).param("id",id).query((r,n)->new Operation(r.getObject("id",UUID.class),r.getObject("contract_id",UUID.class),r.getString("channel"),r.getString("status"),r.getString("last_code"),r.getTimestamp("created_at").toInstant(),r.getInt("version"),"\"communication-"+r.getObject("id")+"-v"+r.getInt("version")+"\"")).optional().orElseThrow(()->new ApiProblem(404,"OPERATION_NOT_FOUND","Communication operation is unavailable."));}
 @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
 public Snapshot read(UUID actor,UUID w){scope.requireOwner(w,actor);var a=account(actor);var channels=new ArrayList<Channel>();
  for(String channel:List.of("email","sms","whatsapp","other")){
   var rows=db.sql("SELECT * FROM communication_contract WHERE workspace_id=:w AND channel=:c AND starts_at<=CURRENT_TIMESTAMP AND ends_at>CURRENT_TIMESTAMP").param("w",w).param("c",channel).query().listOfRows();
   if(rows.isEmpty()){channels.add(new Channel(channel,null,null,null,null,null,null,null,null,false,"No active contract or supported adapter is configured.",null));continue;}
   var c=rows.getFirst();UUID id=(UUID)c.get("id");int used=used(id),reserved=reserved(id),allowance=((Number)c.get("allowance")).intValue();String contact=(String)a.get(channel.equals("email")?"email":"mobile");boolean available=permitted(w,(UUID)c.get("plan_version_id"))&&contact!=null;
   String masked=contact==null?null:channel.equals("email")?contact.substring(0,1)+"***"+contact.substring(contact.indexOf('@')):"••••"+contact.substring(Math.max(0,contact.length()-4));
   channels.add(new Channel(channel,id,(String)c.get("metric"),allowance,used,reserved,Math.max(0,allowance-used-reserved),instant(c.get("starts_at")),instant(c.get("ends_at")),available,available?"One local inbox acceptance consumes one unit. Delivery/read/charges are not inferred.":"Local containers, matching subscription, active workspace and a linked test contact are required.",masked));
  }
  var ids=db.sql("SELECT id FROM communication_operation WHERE workspace_id=:w ORDER BY created_at DESC,id DESC LIMIT 25").param("w",w).query(UUID.class).list();
  return new Snapshot(w,((Number)a.get("version")).intValue(),channels,ids.stream().map(id->operation(w,id)).toList(),db.sql("SELECT * FROM communication_reconciliation WHERE workspace_id=:w ORDER BY created_at DESC,id DESC LIMIT 25").param("w",w).query((r,n)->new Recovery(r.getObject("id",UUID.class),r.getObject("operation_id",UUID.class),r.getString("status"),r.getString("last_code"),r.getTimestamp("created_at").toInstant())).list(),Instant.now(),"Sandbox workspace metering only; no carrier SMS, external email or payment. Sign-in traffic is excluded. Periods and allowances are explicit local configuration, not approved plan benefits. Latest 25 operations shown; unknown outcomes hold their reservation until evidence is reconciled.");
 }
 private Operation queuedResult(UUID w,UUID id){var o=operation(w,id);return new Operation(o.id(),o.contractId(),o.channel(),"queued",null,o.createdAt(),1,"\"communication-"+o.id()+"-v1\"");}
 public Operation enqueue(UUID actor,UUID w,UUID key,UUID contractId,int contactVersion){scope.requireOwner(w,actor);lock(w);scope.requireOwner(w,actor);
  var prior=db.sql("SELECT * FROM communication_operation WHERE workspace_id=:w AND actor_id=:a AND request_key=:k").param("w",w).param("a",actor).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){var p=prior.getFirst();if(!p.get("contract_id").equals(contractId)||((Number)p.get("contact_version")).intValue()!=contactVersion)throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to another communication confirmation.");return queuedResult(w,(UUID)p.get("id"));}
  var c=contract(w,contractId);if(!permitted(w,(UUID)c.get("plan_version_id")))throw new ApiProblem(409,"COMMUNICATION_UNAVAILABLE","This local communication contract is not currently eligible.");
  var a=account(actor);if(((Number)a.get("version")).intValue()!=contactVersion)throw new ApiProblem(412,"VERSION_STALE","Your contact/profile changed. Refresh and confirm again.");if(a.get(c.get("channel").equals("email")?"email":"mobile")==null)throw new ApiProblem(409,"CONTACT_UNAVAILABLE","Link a test contact before requesting this channel.");
  if(used(contractId)+reserved(contractId)>=((Number)c.get("allowance")).intValue())throw new ApiProblem(409,"QUOTA_EXCEEDED","No communication allowance remains in this configured period.");
  UUID id=UUID.randomUUID();db.sql("INSERT INTO communication_operation(id,workspace_id,contract_id,actor_id,request_key,contact_version,status,recipient_fingerprint) VALUES(:id,:w,:c,:a,:k,:v,'queued',:fingerprint)").param("id",id).param("w",w).param("c",contractId).param("a",actor).param("k",key).param("v",contactVersion).param("fingerprint",challenges.proofFingerprint("communication-contact:"+a.get(c.get("channel").equals("email")?"email":"mobile"))).update();audit.record(actor,w,"communication.queued",id);return queuedResult(w,id);
 }
 public List<Claim> claim(){return db.sql("SELECT * FROM lmt_claim_communication()").query((r,n)->new Claim(r.getObject("id",UUID.class),r.getObject("workspace_id",UUID.class),r.getObject("actor_id",UUID.class),r.getObject("token",UUID.class))).list();}
 public Dispatch prepare(Claim claim){scope.requireOwner(claim.workspaceId(),claim.actorId());lock(claim.workspaceId());scope.requireOwner(claim.workspaceId(),claim.actorId());
  var rows=db.sql("SELECT * FROM communication_operation WHERE id=:id AND lease_token=:t AND status='dispatching' AND lease_until>clock_timestamp()").param("id",claim.id()).param("t",claim.token()).query().listOfRows();if(rows.isEmpty())throw new ApiProblem(409,"OPERATION_STALE","Dispatch lease is no longer active.");var o=rows.getFirst();var c=contract(claim.workspaceId(),(UUID)o.get("contract_id"));if(!permitted(claim.workspaceId(),(UUID)c.get("plan_version_id")))throw new ApiProblem(409,"COMMUNICATION_UNAVAILABLE","Contract unavailable.");var a=account(claim.actorId());String channel=(String)c.get("channel"),destination=(String)a.get(channel.equals("email")?"email":"mobile");if(destination==null||!a.get("version").equals(o.get("contact_version")))throw new ApiProblem(409,"CONTACT_UNAVAILABLE","Contact changed.");
  db.sql("INSERT INTO communication_usage_event(id,workspace_id,operation_id,contract_id,metric,quantity) VALUES(:id,:w,:o,:c,'dispatch_authorized',1) ON CONFLICT(operation_id,metric) DO NOTHING").param("id",UUID.randomUUID()).param("w",claim.workspaceId()).param("o",claim.id()).param("c",o.get("contract_id")).update();audit.record(claim.actorId(),claim.workspaceId(),"communication.dispatch_authorized",claim.id());return new Dispatch(claim,channel,destination);
 }
 public void finish(Claim claim,String status,String reference,String code){db.sql("SELECT lmt_finish_communication(:id,:token,:status,:reference,:code)").param("id",claim.id()).param("token",claim.token()).param("status",status).param("reference",reference,java.sql.Types.VARCHAR).param("code",code,java.sql.Types.VARCHAR).query().singleRow();}
}
