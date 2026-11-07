package in.lmt.workspace;

import in.lmt.identity.ChallengeService;
import in.lmt.shared.*;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Durable scheduling and recovery for the existing in-app welcome handler only. */
@Service
public class WelcomeOperations {
 private final JdbcClient db;private final WorkspaceCommands authority;private final Audit audit;private final ObjectMapper json;private final int leaseSeconds;
 public WelcomeOperations(JdbcClient db,WorkspaceCommands authority,Audit audit,ObjectMapper json,@Value("${lmt.workspace.lease-seconds:30}")int leaseSeconds){
  this.db=db;this.authority=authority;this.audit=audit;this.json=json;if(leaseSeconds<1||leaseSeconds>300)throw new IllegalArgumentException("Lease seconds must be 1–300");this.leaseSeconds=leaseSeconds;
 }
 public record Claim(UUID id,UUID actor,UUID workspace,UUID token,String type){}
 public record Operation(UUID id,UUID workspaceId,String type,String status,int attempts,int cycleAttempts,int maxAttempts,int version,String etag,Instant createdAt,Instant updatedAt,Instant nextAttemptAt,Instant leaseUntil,String lastCode){}
 public record Page(int limit,String nextCursor){}
 public record Operations(UUID workspaceId,String workspaceName,List<Operation> items,Page page,int totalMatching,Instant asOf){}
 public record Attempt(int attempt,Instant startedAt,Instant finishedAt,String outcome){}
 public record Detail(Operation operation,List<Attempt> recentAttempts){}
 private static String tag(UUID id,int v){return "\"operation-"+id+"-v"+v+"\"";}
 private static Instant time(java.sql.ResultSet r,String field)throws java.sql.SQLException{var t=r.getTimestamp(field);return t==null?null:t.toInstant();}
 private Operation projection(java.sql.ResultSet r)throws java.sql.SQLException{
  UUID id=r.getObject("event_id",UUID.class);int v=r.getInt("version");return new Operation(id,r.getObject("scope_id",UUID.class),r.getString("event_type"),r.getString("status"),r.getInt("attempts"),r.getInt("cycle_attempts"),5,v,tag(id,v),time(r,"occurred_at"),time(r,"updated_at"),time(r,"next_attempt_at"),time(r,"lease_until"),r.getString("last_code"));
 }
 private Operation find(UUID id){return db.sql("SELECT * FROM workspace_outbox WHERE event_id=:id").param("id",id).query((r,n)->projection(r)).optional().orElseThrow(()->new ApiProblem(404,"OPERATION_NOT_FOUND","Operation is unavailable."));}
 private void authorize(UUID actor,UUID id){var row=find(id);authority.requireOwner(row.workspaceId(),actor);}
 @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
 public Detail detail(UUID actor,UUID id){authorize(actor,id);var op=find(id);var attempts=db.sql("SELECT * FROM operation_attempt WHERE event_id=:id ORDER BY attempt DESC LIMIT 20").param("id",id).query((r,n)->new Attempt(r.getInt("attempt"),time(r,"started_at"),time(r,"finished_at"),r.getString("outcome"))).list();return new Detail(op,attempts);}
 @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
 public Operations list(UUID actor,UUID w,String cursor){
  authority.requireOwner(w,actor);UUID after=new UUID(0,0);String context=actor+":"+w;
  if(cursor!=null)try{String[] p=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8).split("/");if(p.length!=2||!p[0].equals(context))throw new IllegalArgumentException();after=UUID.fromString(p[1]);}catch(RuntimeException e){throw new ApiProblem(422,"INVALID_CURSOR","Reload the operations list.","cursor");}
  var rows=db.sql("SELECT * FROM workspace_outbox WHERE scope_id=:w AND event_id>:after ORDER BY event_id LIMIT 26").param("w",w).param("after",after).query((r,n)->projection(r)).list();boolean more=rows.size()>25;var items=more?rows.subList(0,25):rows;
  String next=more?Base64.getUrlEncoder().withoutPadding().encodeToString((context+"/"+items.getLast().id()).getBytes(java.nio.charset.StandardCharsets.UTF_8)):null;
  int total=db.sql("SELECT count(*) FROM workspace_outbox WHERE scope_id=:w").param("w",w).query(Integer.class).single();String name=db.sql("SELECT name FROM workspace WHERE id=:w").param("w",w).query(String.class).single();return new Operations(w,name,items,new Page(25,next),total,Instant.now());
 }
 @Transactional public Operation recover(UUID actor,UUID id,UUID key,String expected,String reason,boolean cancel){
  authorize(actor,id);if(reason.strip().length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Explain the recovery reason in at least 10 characters.","reason");String op=cancel?"cancel":"retry";String hash=ChallengeService.hash(expected+":"+reason.strip());
  db.sql("SELECT event_id FROM workspace_outbox WHERE event_id=:id FOR UPDATE").param("id",id).query().singleRow();
  if(!lockOwner(currentWorkspace(id),actor))throw new ApiProblem(404,"OPERATION_NOT_FOUND","Operation is unavailable.");
  var prior=db.sql("SELECT request_hash,result::text FROM operation_command WHERE actor_id=:a AND event_id=:id AND operation=:o AND request_key=:k").param("a",actor).param("id",id).param("o",op).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){if(!hash.equals(prior.getFirst().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different recovery confirmation.");return json.readValue((String)prior.getFirst().get("result"),Operation.class);}
  var current=find(id);if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Reload the operation before confirming.");if(!current.etag().equals(expected))throw new ApiProblem(412,"VERSION_STALE","This operation changed. Reload and review it.");
  if(cancel){if(Set.of("delivered","cancelled").contains(current.status()))throw new ApiProblem(409,"OPERATION_TERMINAL","Completed delivery cannot be recalled or cancelled.");}
  else{
   if(!Set.of("failed","blocked").contains(current.status()))throw new ApiProblem(409,"OPERATION_NOT_RETRYABLE","Only failed or blocked operations can be retried manually.");
   UUID creator=db.sql("SELECT actor_id FROM workspace_outbox WHERE event_id=:id").param("id",id).query(UUID.class).single();
   if(!db.sql("SELECT lmt_is_workspace_owner(:w,:a)").param("w",current.workspaceId()).param("a",creator).query(Boolean.class).single())throw new ApiProblem(409,"AUTHORITY_REVOKED","The original creator must regain valid owner authority before retry. Do not transfer their authority implicitly.");
   if(!current.type().equals("workspace.provisioned.v1"))throw new ApiProblem(409,"HANDLER_UNAVAILABLE","This operation has no available handler.");
  }
  db.sql("UPDATE operation_attempt SET outcome='cancelled',finished_at=CURRENT_TIMESTAMP WHERE event_id=:id AND outcome='running'").param("id",id).update();
  db.sql("UPDATE workspace_outbox SET status=:s,cycle_attempts=CASE WHEN :cancel THEN cycle_attempts ELSE 0 END,version=version+1,lease_token=NULL,lease_until=NULL,last_code=NULL,next_attempt_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE event_id=:id")
   .param("s",cancel?"cancelled":"pending").param("cancel",cancel).param("id",id).update();
  var result=find(id);db.sql("INSERT INTO operation_command(actor_id,event_id,operation,request_key,request_hash,reason,result) VALUES(:a,:id,:o,:k,:h,:reason,CAST(:r AS jsonb))")
   .param("a",actor).param("id",id).param("o",op).param("k",key).param("h",hash).param("reason",reason.strip()).param("r",json.writeValueAsString(result)).update();audit.record(actor,current.workspaceId(),"operation."+op+"_requested",id);return result;
 }
 private UUID currentWorkspace(UUID id){return db.sql("SELECT scope_id FROM workspace_outbox WHERE event_id=:id").param("id",id).query(UUID.class).single();}
 private boolean lockOwner(UUID w,UUID actor){authority.scope(actor);return !db.sql("SELECT account_id FROM workspace_member WHERE workspace_id=:w AND account_id=:a AND active AND role='OWNER' FOR SHARE").param("w",w).param("a",actor).query().listOfRows().isEmpty();}
 @Transactional public List<Claim> claim(){
  var rows=db.sql("SELECT * FROM workspace_outbox WHERE (status='pending' AND next_attempt_at<=CURRENT_TIMESTAMP) OR (status='running' AND lease_until<=CURRENT_TIMESTAMP) ORDER BY next_attempt_at,event_id LIMIT 10 FOR UPDATE SKIP LOCKED").query().listOfRows();List<Claim> claims=new ArrayList<>();
  for(var row:rows){UUID id=(UUID)row.get("event_id"),actor=(UUID)row.get("actor_id"),w=(UUID)row.get("scope_id");
   db.sql("UPDATE operation_attempt SET outcome='lease_expired',finished_at=CURRENT_TIMESTAMP WHERE event_id=:id AND outcome='running'").param("id",id).update();
   if(((Number)row.get("cycle_attempts")).intValue()>=5){db.sql("UPDATE workspace_outbox SET status='failed',lease_token=NULL,lease_until=NULL,last_code='RETRY_EXHAUSTED',version=version+1,updated_at=CURRENT_TIMESTAMP WHERE event_id=:id").param("id",id).update();audit.record(actor,w,"operation.exhausted",id);continue;}
   UUID token=UUID.randomUUID();int attempt=((Number)row.get("attempts")).intValue()+1;
   db.sql("UPDATE workspace_outbox SET status='running',attempts=attempts+1,cycle_attempts=cycle_attempts+1,lease_token=:token,lease_until=CURRENT_TIMESTAMP+(:seconds * interval '1 second'),version=version+1,updated_at=CURRENT_TIMESTAMP WHERE event_id=:id").param("token",token).param("seconds",leaseSeconds).param("id",id).update();
   db.sql("INSERT INTO operation_attempt(event_id,attempt,lease_token) VALUES(:id,:attempt,:token)").param("id",id).param("attempt",attempt).param("token",token).update();claims.add(new Claim(id,actor,w,token,(String)row.get("event_type")));
  }
  return claims;
 }
 private boolean fence(Claim c){return !db.sql("SELECT event_id FROM workspace_outbox WHERE event_id=:id AND status='running' AND lease_token=:token AND lease_until>clock_timestamp() FOR UPDATE").param("id",c.id()).param("token",c.token()).query().listOfRows().isEmpty();}
 private void terminal(Claim c,String status,String code,String outcome){
  db.sql("UPDATE workspace_outbox SET status=:status,last_code=:code,lease_token=NULL,lease_until=NULL,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE event_id=:id").param("status",status).param("code",code,java.sql.Types.VARCHAR).param("id",c.id()).update();
  db.sql("UPDATE operation_attempt SET outcome=:outcome,finished_at=CURRENT_TIMESTAMP WHERE lease_token=:token").param("outcome",outcome).param("token",c.token()).update();audit.record(c.actor(),c.workspace(),"operation."+status,c.id());
 }
 @Transactional public void complete(Claim c){
  if(!fence(c))return;
  boolean authorized=lockOwner(c.workspace(),c.actor());
  if(!authorized){terminal(c,"blocked","AUTHORITY_REVOKED","authority_revoked");return;}
  if(!c.type().equals("workspace.provisioned.v1")){terminal(c,"blocked","HANDLER_UNAVAILABLE","handler_unavailable");return;}
  db.sql("INSERT INTO workspace_notification(event_id,workspace_id,message) VALUES(:e,:w,'Your sandbox workspace is ready. Property and rental operations are not available yet.') ON CONFLICT(event_id) DO NOTHING").param("e",c.id()).param("w",c.workspace()).update();
  terminal(c,"delivered",null,"completed");
 }
 @Transactional public void failed(Claim c){
  if(!fence(c))return;int cycle=db.sql("SELECT cycle_attempts FROM workspace_outbox WHERE event_id=:id").param("id",c.id()).query(Integer.class).single();
  if(cycle>=5){terminal(c,"failed","RETRY_EXHAUSTED","transient_failure");return;}
  long seconds=(1L<<cycle)+java.util.concurrent.ThreadLocalRandom.current().nextLong(3);
  db.sql("UPDATE workspace_outbox SET status='pending',lease_token=NULL,lease_until=NULL,last_code='DELIVERY_RETRY_SCHEDULED',next_attempt_at=CURRENT_TIMESTAMP+(:seconds * interval '1 second'),version=version+1,updated_at=CURRENT_TIMESTAMP WHERE event_id=:id").param("seconds",seconds).param("id",c.id()).update();
  db.sql("UPDATE operation_attempt SET outcome='transient_failure',finished_at=CURRENT_TIMESTAMP WHERE lease_token=:token").param("token",c.token()).update();audit.record(c.actor(),c.workspace(),"operation.retry_scheduled",c.id());
 }
}
