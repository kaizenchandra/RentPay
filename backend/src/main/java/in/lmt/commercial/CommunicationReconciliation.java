package in.lmt.commercial;
import in.lmt.identity.*;
import in.lmt.shared.*;
import in.lmt.workspace.WorkspaceCommands;
import java.util.*;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class CommunicationReconciliation {
 private final JdbcClient db;private final WorkspaceCommands scope;private final IdentityConfig config;private final Audit audit;
 public CommunicationReconciliation(JdbcClient db,WorkspaceCommands scope,IdentityConfig config,Audit audit){this.db=db;this.scope=scope;this.config=config;this.audit=audit;}
 public record Request(UUID id,UUID operationId,String status,Instant createdAt){}
 public record Claim(UUID id,UUID workspaceId,UUID actorId,UUID token){}
 public record Evidence(Claim claim,UUID operationId,String channel,String fingerprint){}
 private void lock(UUID w){db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k","communication:"+w).query().singleRow();}
 public Request request(UUID actor,UUID w,UUID operation,UUID key,String expected,String reason){
  scope.requireOwner(w,actor);lock(w);scope.requireOwner(w,actor);
  if(!config.sandbox)throw new ApiProblem(409,"RECONCILIATION_UNAVAILABLE","Local evidence reconciliation is unavailable.");
  String hash=ChallengeService.hash(operation+"\n"+expected+"\n"+reason);
  var prior=db.sql("SELECT * FROM communication_reconciliation WHERE workspace_id=:w AND actor_id=:a AND request_key=:k").param("w",w).param("a",actor).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){var p=prior.getFirst();if(!p.get("request_hash").equals(hash))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different reconciliation request.");return new Request((UUID)p.get("id"),(UUID)p.get("operation_id"),"queued",((java.sql.Timestamp)p.get("created_at")).toInstant());}
  var row=db.sql("SELECT o.*,w.mode FROM communication_operation o JOIN workspace w ON w.id=o.workspace_id WHERE o.workspace_id=:w AND o.id=:id").param("w",w).param("id",operation).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"OPERATION_NOT_FOUND","Communication operation is unavailable."));
  if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Refresh the operation before requesting reconciliation.");
  if(!expected.equals("\"communication-"+operation+"-v"+row.get("version")+"\""))throw new ApiProblem(412,"VERSION_STALE","The communication changed. Refresh and review its current outcome.");
  if(!row.get("mode").equals("sandbox")||!row.get("status").equals("unknown"))throw new ApiProblem(409,"RECONCILIATION_UNAVAILABLE","Only unknown local outcomes can be checked against inbox evidence.");
  if(reason.strip().length()<10)throw new ApiProblem(422,"REASON_REQUIRED","Give a reason of at least 10 characters.","reason");
  if(db.sql("SELECT EXISTS(SELECT 1 FROM communication_reconciliation WHERE operation_id=:id AND status IN('queued','checking'))").param("id",operation).query(Boolean.class).single())throw new ApiProblem(409,"RECONCILIATION_PENDING","An evidence check is already in progress.");
  UUID id=UUID.randomUUID();Instant created=db.sql("INSERT INTO communication_reconciliation(id,workspace_id,operation_id,actor_id,request_key,request_hash,reason,status) VALUES(:id,:w,:o,:a,:k,:h,:r,'queued') RETURNING created_at").param("id",id).param("w",w).param("o",operation).param("a",actor).param("k",key).param("h",hash).param("r",reason.strip()).query(java.sql.Timestamp.class).single().toInstant();audit.record(actor,w,"communication.reconciliation_requested",id);return new Request(id,operation,"queued",created);
 }
 public List<Claim> claim(){return db.sql("SELECT * FROM lmt_claim_communication_reconciliations()").query((r,n)->new Claim(r.getObject("id",UUID.class),r.getObject("workspace_id",UUID.class),r.getObject("actor_id",UUID.class),r.getObject("token",UUID.class))).list();}
 public Evidence prepare(Claim claim){scope.requireOwner(claim.workspaceId(),claim.actorId());var rows=db.sql("SELECT o.id,c.channel,o.recipient_fingerprint,w.mode FROM communication_reconciliation r JOIN communication_operation o ON o.id=r.operation_id JOIN communication_contract c ON c.id=o.contract_id JOIN workspace w ON w.id=o.workspace_id WHERE r.id=:id AND r.workspace_id=:w AND r.lease_token=:t AND r.status='checking' AND r.lease_until>clock_timestamp() AND o.status='unknown'").param("id",claim.id()).param("w",claim.workspaceId()).param("t",claim.token()).query().listOfRows();if(rows.isEmpty())throw new ApiProblem(409,"OPERATION_CHANGED","Operation is no longer awaiting evidence.");var row=rows.getFirst();if(!config.sandbox||!row.get("mode").equals("sandbox"))throw new ApiProblem(409,"MODE_UNAVAILABLE","Local reconciliation unavailable.");if(row.get("recipient_fingerprint")==null)throw new ApiProblem(409,"REFERENCE_UNAVAILABLE","This older operation has no retained recipient fingerprint; manual review is required.");return new Evidence(claim,(UUID)row.get("id"),(String)row.get("channel"),(String)row.get("recipient_fingerprint"));}
 public void finish(Claim claim,String code,String reference){db.sql("SELECT lmt_finish_communication_reconciliation(:id,:token,:code,:ref)").param("id",claim.id()).param("token",claim.token()).param("code",code).param("ref",reference,java.sql.Types.VARCHAR).query().singleRow();}
}
