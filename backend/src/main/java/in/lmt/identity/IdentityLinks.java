package in.lmt.identity;
import in.lmt.shared.*;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
@Service
public class IdentityLinks {
 private final JdbcClient db;private final ChallengeService challenges;private final AccountControls accounts;private final IdentityConfig config;private final Audit audit;private final ObjectMapper json;private final long recentSeconds;
 public IdentityLinks(JdbcClient db,ChallengeService challenges,AccountControls accounts,IdentityConfig config,Audit audit,ObjectMapper json,@org.springframework.beans.factory.annotation.Value("${lmt.security.reauthentication-seconds:300}")long recentSeconds){this.db=db;this.challenges=challenges;this.accounts=accounts;this.config=config;this.audit=audit;this.json=json;this.recentSeconds=recentSeconds;}
 private void authorize(UUID actor,String session){
  if(!config.sandbox)throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","Contact linking is available only in the local sandbox.");
  db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",actor.toString()).query().singleRow();
  if(db.sql("SELECT id FROM account_session WHERE account_id=:a AND token_hash=:h AND revoked_at IS NULL AND authenticated_at>clock_timestamp()-(:seconds * interval '1 second') FOR SHARE").param("a",actor).param("h",ChallengeService.hash(session)).param("seconds",recentSeconds).query().listOfRows().isEmpty())throw new ApiProblem(403,"REAUTHENTICATION_REQUIRED","Sign in again before linking a contact.");
 }
 private void lock(String key){db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k",key).query().singleRow();}
 private Map<String,Object> account(UUID actor,String expected,String channel){
  var a=db.sql("SELECT * FROM user_account WHERE id=:a FOR UPDATE").param("a",actor).query().singleRow();
  if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review your current profile before linking.");
  if(!expected.equals("\"profile-"+actor+"-v"+a.get("version")+"\""))throw new ApiProblem(412,"VERSION_STALE","Your profile changed. Reload and request a new linking code.");
  if(!a.get("identity_mode").equals("sandbox")||a.get(channel)!=null||a.get(channel.equals("mobile")?"email":"mobile")==null)throw new ApiProblem(409,"CONTACT_LINK_UNAVAILABLE","This flow only adds an unused alternate contact to your account. Contact replacement and account merging are unavailable.");return a;
 }
 @Transactional public ChallengeService.Challenge request(UUID actor,String session,UUID key,String expected,String channel,String destination){
  authorize(actor,session);if(channel==null)channel="email";String contact=ChallengeService.normalize(channel,destination);lock("challenge:"+channel+":"+contact);var a=account(actor,expected,channel);
  return challenges.requestLink(session,key,channel,contact,(String)a.get("display_name"),actor,((Number)a.get("version")).intValue());
 }
 @Transactional(noRollbackFor=ApiProblem.class) public AccountControls.Profile confirm(UUID actor,String session,UUID key,String expected,UUID challenge,String code){
  authorize(actor,session);lock("identity-link-command:"+actor+":"+key);String fingerprint=challenges.proofFingerprint(challenge+":"+expected+":"+code);
  var prior=db.sql("SELECT request_hash,result::text FROM identity_command WHERE actor_id=:a AND operation IN ('identity.email_linked','identity.mobile_linked') AND request_key=:k").param("a",actor).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){if(!fingerprint.equals(prior.getFirst().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to another linking confirmation.");return json.readValue((String)prior.getFirst().get("result"),AccountControls.Profile.class);}
  var selected=db.sql("SELECT channel,coalesce(email,mobile) AS destination FROM auth_challenge WHERE id=:id AND session_hash=:s AND purpose IN ('link_email','link_mobile') AND link_account_id=:a").param("id",challenge).param("s",ChallengeService.hash(session)).param("a",actor).query().listOfRows();
  if(selected.isEmpty())throw new ApiProblem(404,"CHALLENGE_NOT_FOUND","Linking challenge is unavailable.");
  String channel=selected.getFirst().get("channel").equals("mobile")?"mobile":"email",selector=(String)selected.getFirst().get("destination"),operation="identity."+channel+"_linked";
  lock("challenge:"+channel+":"+selector);var a=account(actor,expected,channel);
  var c=db.sql("SELECT *,expires_at>clock_timestamp() AS valid FROM auth_challenge WHERE id=:id FOR UPDATE").param("id",challenge).query().singleRow();
  if(!a.get("version").equals(c.get("link_profile_version")))throw new ApiProblem(412,"VERSION_STALE","Request a new code for your current profile.");
  if((Boolean)c.get("consumed")||!(Boolean)c.get("valid")||((Number)c.get("attempts")).intValue()>=5)throw new ApiProblem(409,"CHALLENGE_UNUSABLE","This linking code expired, was used or reached its attempt limit.");
  db.sql("UPDATE auth_challenge SET attempts=attempts+1 WHERE id=:id").param("id",challenge).update();
  if(!MessageDigest.isEqual(challenges.code(challenge).getBytes(StandardCharsets.UTF_8),code.getBytes(StandardCharsets.UTF_8))){audit.record(actor,null,"identity.link_proof_rejected",challenge);throw new ApiProblem(422,"INVALID_CODE","The code is incorrect.","code");}
  db.sql("UPDATE auth_challenge SET consumed=true WHERE id=:id").param("id",challenge).update();
  if(db.sql("SELECT EXISTS(SELECT 1 FROM user_account WHERE "+channel+"=:contact)").param("contact",selector).query(Boolean.class).single()){audit.record(actor,null,"identity."+channel+"_link_conflict",challenge);throw new ApiProblem(409,"CONTACT_LINK_UNAVAILABLE","This contact cannot be linked. Existing accounts are not merged by this flow.");}
  db.sql("UPDATE user_account SET "+channel+"=:contact,version=version+1 WHERE id=:a").param("contact",selector).param("a",actor).update();var result=accounts.profile(actor);
  db.sql("INSERT INTO identity_command(actor_id,operation,request_key,request_hash,result) VALUES(:a,:operation,:k,:h,CAST(:r AS jsonb))").param("a",actor).param("operation",operation).param("k",key).param("h",fingerprint).param("r",json.writeValueAsString(result)).update();
  audit.record(actor,null,operation,challenge);db.sql("INSERT INTO account_notification(id,account_id,message) VALUES(:id,:a,:message)").param("id",UUID.randomUUID()).param("a",actor).param("message","A"+(channel.equals("email")?"n email":" mobile")+" sign-in contact was linked to your account. Review account security if this was unexpected.").update();return result;
 }
}
