package in.lmt.commercial;
import in.lmt.shared.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
@Service
public class CapacityRepairs {
 public record Input(@NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*")String reason){}
 public record View(UUID workspaceId,int used,int reserved,int allowance,long targetUsed,int targetReserved,boolean repairable,String reason,int revision,String etag){}
 private final JdbcClient db;private final Audit audit;private final ObjectMapper json;private final ManagerUsageEvidence evidence;private final boolean sandbox;
 public CapacityRepairs(JdbcClient db,Audit audit,ObjectMapper json,ManagerUsageEvidence evidence,@org.springframework.beans.factory.annotation.Value("${lmt.identity.mode:unavailable}")String mode){this.db=db;this.audit=audit;this.json=json;this.evidence=evidence;this.sandbox=mode.equals("sandbox");}
 private void lock(String key){db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k",key).query().singleRow();}
 private void authorize(UUID a,UUID w){
  if(!sandbox)throw new ApiProblem(503,"CAPACITY_REPAIR_UNAVAILABLE","Manager capacity repair is configured only for sandbox workspaces.");
  db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",a.toString()).query().singleRow();lock("capacity-repair-authority:"+a+":"+w);
  if(db.sql("SELECT account_id FROM capacity_repair_operator_grant WHERE account_id=:a AND workspace_id=:w AND NOT revoked AND valid_until>clock_timestamp()").param("a",a).param("w",w).query().listOfRows().isEmpty())throw new ApiProblem(403,"CAPACITY_REPAIR_AUTHORITY_REQUIRED","A separate active capacity-repair grant for this workspace is required.");
  if(!db.sql("SELECT lmt_is_workspace_owner(:w,:a) AND EXISTS(SELECT 1 FROM workspace WHERE id=:w AND mode='sandbox')").param("w",w).param("a",a).query(Boolean.class).single())throw new ApiProblem(404,"WORKSPACE_NOT_FOUND","Workspace is unavailable. This bounded repair workflow also requires active workspace ownership.");
 }
 private String hash(String value){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private View view(UUID w){
  var g=db.sql("SELECT used,reserved,allowance,repair_revision FROM workspace_grant WHERE workspace_id=:w AND resource='managers' FOR UPDATE").param("w",w).query().singleRow();
  int used=((Number)g.get("used")).intValue(),reserved=((Number)g.get("reserved")).intValue(),allowance=((Number)g.get("allowance")).intValue(),revision=((Number)g.get("repair_revision")).intValue();var e=evidence.read(w);
  // Check identity correspondence, not only equal member/allocation totals.
  long mismatch=db.sql("""
   SELECT (SELECT count(*) FROM workspace_member m WHERE m.workspace_id=:w AND m.active AND m.role='MANAGER' AND NOT EXISTS(SELECT 1 FROM manager_allocation a WHERE a.workspace_id=:w AND a.member_id=m.id AND a.status='committed'))
    +(SELECT count(*) FROM manager_allocation a WHERE a.workspace_id=:w AND a.status='committed' AND NOT EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=:w AND m.id=a.member_id AND m.active AND m.role='MANAGER'))
   """).param("w",w).query(Long.class).single();
  boolean sourceValid=mismatch==0&&e.inconsistentAllocations()==0&&e.unmatchedEvents()==0&&e.committedAllocations()==e.eventBalance()&&e.committedAllocations()<=allowance;
  String reason=!sourceValid?"SOURCE_EVIDENCE_INCONSISTENT":used==e.committedAllocations()&&reserved==0?"ALREADY_RECONCILED":"COUNTER_REPAIR_AVAILABLE";
  String tag="\"capacity-"+hash(w+":"+g+":"+e+":"+mismatch)+"\"";
  return new View(w,used,reserved,allowance,e.committedAllocations(),0,reason.equals("COUNTER_REPAIR_AVAILABLE"),reason,revision,tag);
 }
 @Transactional public View read(UUID a,UUID w){authorize(a,w);lock("membership:"+w);authorize(a,w);var v=view(w);audit.record(a,w,"CAPACITY_REPAIR_REVIEWED",w);return v;}
 @Transactional public View repair(UUID a,UUID w,UUID key,String expected,Input input){
  authorize(a,w);String reason=input.reason().strip();if(reason.length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Give a reason of at least ten characters.","reason");
  lock("capacity-repair-command:"+a+":"+key);String fingerprint=hash(w+":"+expected+":"+reason);
  var prior=db.sql("SELECT request_hash,result::text FROM capacity_repair_command WHERE account_id=:a AND request_key=:k").param("a",a).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){if(!fingerprint.equals(prior.getFirst().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different repair confirmation.");return json.readValue((String)prior.getFirst().get("result"),View.class);}
  lock("membership:"+w);authorize(a,w);var before=view(w);
  if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review current evidence before confirming.");
  if(!before.etag().equals(expected))throw new ApiProblem(412,"CAPACITY_REPAIR_STALE","Capacity evidence changed. Refresh and review again.");
  if(!before.repairable())throw new ApiProblem(409,before.reason(),"A counter-only repair is not available. Investigate source evidence without rewriting it.");
  db.sql("UPDATE workspace_grant SET used=:used,reserved=0,repair_revision=repair_revision+1 WHERE workspace_id=:w AND resource='managers'").param("used",before.targetUsed()).param("w",w).update();var result=view(w);
  db.sql("INSERT INTO capacity_repair_command(account_id,request_key,workspace_id,request_hash,reason,before_state,result) VALUES(:a,:k,:w,:h,:reason,CAST(:b AS jsonb),CAST(:r AS jsonb))").param("a",a).param("k",key).param("w",w).param("h",fingerprint).param("reason",reason).param("b",json.writeValueAsString(before)).param("r",json.writeValueAsString(result)).update();audit.record(a,w,"MANAGER_CAPACITY_REPAIRED",w);return result;
 }
}
