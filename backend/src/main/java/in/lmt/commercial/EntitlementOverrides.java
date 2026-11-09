package in.lmt.commercial;
import in.lmt.shared.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
@Service
public class EntitlementOverrides {
 public record Input(@NotNull @Pattern(regexp="allow|deny|inherit")String effect,Instant expiresAt,@NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*")String reason){}
 public record View(UUID workspaceId,String capability,String effect,Instant expiresAt,boolean active,int revision,String etag){}
 private final JdbcClient db;private final Audit audit;private final ObjectMapper json;private final boolean sandbox;
 public EntitlementOverrides(JdbcClient db,Audit audit,ObjectMapper json,@org.springframework.beans.factory.annotation.Value("${lmt.identity.mode:unavailable}")String mode){this.db=db;this.audit=audit;this.json=json;this.sandbox=mode.equals("sandbox");}
 private void authorize(UUID a,UUID w){
  if(!sandbox)throw new ApiProblem(503,"ENTITLEMENT_POLICY_UNAVAILABLE","Temporary override administration is not configured for this environment.");
  db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",a.toString()).query().singleRow();
  db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key","entitlement-authority:"+a+":"+w).query().singleRow();
  if(db.sql("SELECT account_id FROM entitlement_operator_grant WHERE account_id=:a AND workspace_id=:w AND NOT revoked AND valid_until>clock_timestamp()").param("a",a).param("w",w).query().listOfRows().isEmpty())throw new ApiProblem(403,"ENTITLEMENT_AUTHORITY_REQUIRED","An active operator grant for this workspace is required. Workspace ownership and plan-review authority do not grant this access.");
 }
 private View view(UUID w){return db.sql("SELECT *,expires_at>clock_timestamp() AS active FROM workspace_capability_override WHERE workspace_id=:w").param("w",w).query((r,n)->{boolean active=r.getBoolean("active");int version=r.getInt("revision");return new View(w,r.getString("capability"),r.getString("effect"),r.getTimestamp("expires_at")==null?null:r.getTimestamp("expires_at").toInstant(),active,version,tag(w,version,active));}).optional().orElse(new View(w,"WORKSPACE_MEMBER_ADD","inherit",null,false,0,tag(w,0,false)));}
 private String hash(String value){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private String tag(UUID w,int version,boolean active){return "\"override-"+w+"-"+version+"-"+active+"\"";}
 @Transactional public View read(UUID a,UUID w){authorize(a,w);audit.record(a,w,"ENTITLEMENT_OVERRIDE_REVIEWED",w);return view(w);}
 @Transactional public View change(UUID a,UUID w,UUID key,String expected,Input input){
  authorize(a,w);String reason=input.reason().strip();if(reason.length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Give a reason of at least ten characters.","reason");
  db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key","override-command:"+a+":"+key).query().singleRow();
  String fingerprint=hash(w+":"+expected+":"+input.effect()+":"+input.expiresAt()+":"+reason);
  var prior=db.sql("SELECT request_hash,result::text FROM entitlement_override_command WHERE account_id=:a AND request_key=:k").param("a",a).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){if(!fingerprint.equals(prior.getFirst().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different override confirmation.");return json.readValue((String)prior.getFirst().get("result"),View.class);}
  // Same lock as membership invitation/acceptance: decisions and override changes serialize.
  db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key","membership:"+w).query().singleRow();authorize(a,w);
  var before=view(w);if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review the current override before confirming.");if(!before.etag().equals(expected))throw new ApiProblem(412,"ENTITLEMENT_OVERRIDE_STALE","The override changed or expired. Refresh and review it again.");
  Instant now=db.sql("SELECT clock_timestamp()").query(java.time.OffsetDateTime.class).single().toInstant();
  if(input.effect().equals("inherit")){if(input.expiresAt()!=null)throw new ApiProblem(422,"VALIDATION_FAILED","Inherit must not include an expiry.","expiresAt");}
  else if(input.expiresAt()==null||!input.expiresAt().isAfter(now)||input.expiresAt().isAfter(now.plusSeconds(86400)))throw new ApiProblem(422,"VALIDATION_FAILED","Choose an expiry in the next 24 hours for this sandbox override.","expiresAt");
  db.sql("INSERT INTO workspace_capability_override(workspace_id,capability,effect,expires_at,revision) VALUES(:w,'WORKSPACE_MEMBER_ADD',:e,:x,1) ON CONFLICT(workspace_id) DO UPDATE SET effect=EXCLUDED.effect,expires_at=EXCLUDED.expires_at,revision=workspace_capability_override.revision+1,updated_at=CURRENT_TIMESTAMP")
   .param("w",w).param("e",input.effect()).param("x",input.expiresAt()==null?null:java.sql.Timestamp.from(input.expiresAt()),java.sql.Types.TIMESTAMP).update();var result=view(w);
  db.sql("INSERT INTO entitlement_override_command(account_id,request_key,workspace_id,request_hash,reason,before_state,result) VALUES(:a,:k,:w,:h,:reason,CAST(:b AS jsonb),CAST(:r AS jsonb))").param("a",a).param("k",key).param("w",w).param("h",fingerprint).param("reason",reason).param("b",json.writeValueAsString(before)).param("r",json.writeValueAsString(result)).update();audit.record(a,w,"ENTITLEMENT_OVERRIDE_CHANGED",w);return result;
 }
}
