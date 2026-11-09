package in.lmt.identity;
import in.lmt.shared.*;
import java.util.*;
import java.time.Instant;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SessionReauthentication {
 public record Result(String status,Instant authenticatedAt){}
 private final JdbcClient db;private final ChallengeService challenges;private final IdentityConfig config;private final Audit audit;
 public SessionReauthentication(JdbcClient db,ChallengeService challenges,IdentityConfig config,Audit audit){this.db=db;this.challenges=challenges;this.config=config;this.audit=audit;}
 private UUID session(UUID actor,String token){
  if(!config.sandbox)throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","Contact proof is not configured for this environment.");
  db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",actor.toString()).query().singleRow();
  return db.sql("SELECT id FROM account_session WHERE account_id=:a AND token_hash=:h AND revoked_at IS NULL FOR UPDATE").param("a",actor).param("h",ChallengeService.hash(token)).query(UUID.class).optional().orElseThrow(()->new ApiProblem(401,"UNAUTHENTICATED","Sign in again."));
 }
 private Map<String,Object> profile(UUID actor,String expected,boolean lock){
  var a=db.sql("SELECT * FROM user_account WHERE id=:a"+(lock?" FOR UPDATE":"")).param("a",actor).query().singleRow();
  if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review your current account before requesting proof.");
  if(!expected.equals("\"profile-"+actor+"-v"+a.get("version")+"\""))throw new ApiProblem(412,"VERSION_STALE","Your profile changed. Reload and request a new verification code.");
  if(!"sandbox".equals(a.get("identity_mode")))throw new ApiProblem(403,"IDENTITY_MODE_CONFLICT","This account cannot use sandbox proof.");return a;
 }
 @Transactional public ChallengeService.Challenge request(UUID actor,String token,UUID key,String expected,String channel){
  session(actor,token);var a=profile(actor,expected,false);
  if(!Set.of("email","mobile").contains(channel)||a.get(channel)==null)throw new ApiProblem(422,"CONTACT_UNAVAILABLE","Select a contact already linked to your account.","channel");
  var c=challenges.requestReauthentication(token,key,channel,(String)a.get(channel),(String)a.get("display_name"),actor,((Number)a.get("version")).intValue());audit.record(actor,null,"identity.reauthentication_requested",c.id());return c;
 }
 // Failed proofs commit attempt evidence; database/audit failures roll back success effects.
 @Transactional(noRollbackFor=ApiProblem.class) public Result verify(UUID actor,String token,String expected,UUID id,String code,Supplier<String> rotate){
  UUID registry=session(actor,token);var a=profile(actor,expected,true);
  var rows=db.sql("SELECT *,expires_at>clock_timestamp() AS valid FROM auth_challenge WHERE id=:id AND session_hash=:h AND link_account_id=:a AND purpose='reauthenticate' FOR UPDATE").param("id",id).param("h",ChallengeService.hash(token)).param("a",actor).query().listOfRows();
  if(rows.isEmpty())throw new ApiProblem(404,"CHALLENGE_NOT_FOUND","Reauthentication proof is unavailable. Reload to check your session or request a new proof.");var c=rows.getFirst();
  if(!a.get("version").equals(c.get("link_profile_version")))throw new ApiProblem(412,"VERSION_STALE","Request a new proof for your current account.");
  String channel=(String)c.get("channel");if(!Objects.equals(a.get(channel),c.get(channel)))throw new ApiProblem(409,"CONTACT_CHANGED","Request a new proof for your current contact.");
  if((Boolean)c.get("consumed")||!(Boolean)c.get("valid")||((Number)c.get("attempts")).intValue()>=5)throw new ApiProblem(409,"CHALLENGE_UNUSABLE","This code expired, was used or reached its attempt limit.");
  db.sql("UPDATE auth_challenge SET attempts=attempts+1 WHERE id=:id").param("id",id).update();
  if(!MessageDigest.isEqual(challenges.code(id).getBytes(StandardCharsets.UTF_8),code.getBytes(StandardCharsets.UTF_8))){audit.record(actor,null,"identity.reauthentication_rejected",id);throw new ApiProblem(422,"INVALID_CODE","The code is incorrect.","code");}
  String rotated=rotate.get();
  Instant at=db.sql("UPDATE account_session SET token_hash=:h,authenticated_at=clock_timestamp(),version=version+1 WHERE id=:id RETURNING authenticated_at").param("h",ChallengeService.hash(rotated)).param("id",registry).query(java.time.OffsetDateTime.class).single().toInstant();
  db.sql("UPDATE auth_challenge SET consumed=true WHERE id=:id").param("id",id).update();audit.record(actor,null,"identity.reauthenticated",id);
  db.sql("INSERT INTO account_notification(id,account_id,message) VALUES(:id,:a,'Your current browser session was verified again. Review account security if this was unexpected.')").param("id",UUID.randomUUID()).param("a",actor).update();return new Result("reauthenticated",at);
 }
}
