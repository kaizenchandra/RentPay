package in.lmt.orchestration;
import in.lmt.workspace.WorkspaceCommands;
import in.lmt.commercial.FreeProvisioning;
import in.lmt.identity.ChallengeService;
import in.lmt.shared.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class WorkspaceProvisioning {
 private final JdbcClient db;private final WorkspaceCommands workspaces;private final FreeProvisioning free;private final ChallengeService identity;private final Audit audit;
 public WorkspaceProvisioning(JdbcClient db,WorkspaceCommands workspaces,FreeProvisioning free,ChallengeService identity,Audit audit){this.db=db;this.workspaces=workspaces;this.free=free;this.identity=identity;this.audit=audit;}
 public record Profile(String name,String customerType,String city,String state){}
 @Transactional public UUID create(UUID actor,UUID key,Profile profile,UUID plan,List<String> consents){
  workspaces.scope(actor);
  var account=identity.account(actor);
  if(!account.mode().equals("sandbox")||!consents.equals(List.of("sandbox-use-v1")))throw new ApiProblem(422,"CONSENT_REQUIRED","Accept the local sandbox acknowledgement.","consentIds");
  // Serializes retries for one actor; different actors remain independent. Unique key is durable.
  db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key","provision:"+actor).query().singleRow();
  String hash=ChallengeService.hash(List.of(profile.name(),profile.customerType(),profile.city(),profile.state(),plan.toString(),"sandbox-use-v1").stream().map(s->s.length()+":"+s).reduce("",String::concat));
  var prior=db.sql("SELECT request_hash,workspace_id FROM workspace_command WHERE actor_id=:a AND request_key=:k").param("a",actor).param("k",key).query().listOfRows().stream().findFirst();
  if(prior.isPresent()){
   if(!hash.equals(prior.get().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This submission key already belongs to different details.");
   UUID id=(UUID)prior.get().get("workspace_id");workspaces.requireOwner(id,actor);return id;
  }
  UUID id=UUID.randomUUID();workspaces.create(id,actor,profile.name(),profile.customerType(),profile.city(),profile.state(),account.mode());free.provision(id,plan);
  db.sql("INSERT INTO workspace_command(actor_id,request_key,request_hash,workspace_id) VALUES(:a,:k,:h,:w)").param("a",actor).param("k",key).param("h",hash).param("w",id).update();
  audit.record(actor,id,"workspace.free_provisioned",id);
  db.sql("INSERT INTO workspace_outbox(event_id,actor_id,event_type,aggregate_id,scope_id,correlation_id,causation_id) VALUES(:e,:actor,'workspace.provisioned.v1',:w,:w,:c,:k)").param("actor",actor).param("e",UUID.randomUUID()).param("w",id).param("c",UUID.randomUUID()).param("k",key).update();
  return id;
 }
}
