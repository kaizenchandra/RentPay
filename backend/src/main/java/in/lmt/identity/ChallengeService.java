package in.lmt.identity;
import in.lmt.shared.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeService {
 private final JdbcClient db;private final IdentityConfig config;private final Audit audit;
 public ChallengeService(JdbcClient db,IdentityConfig config,Audit audit){this.db=db;this.config=config;this.audit=audit;}
 public record Challenge(UUID id,String status,String mode,String maskedDestination,Instant expiresAt){}
 public record Account(UUID id,String displayName,String mode){}
 public record VerifiedAccount(Account account,int generation){}
 public static String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 public String code(UUID id){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(config.secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));byte[] b=mac.doFinal(id.toString().getBytes(StandardCharsets.UTF_8));long n=java.nio.ByteBuffer.wrap(b).getLong() & Long.MAX_VALUE;return String.format(Locale.ROOT,"%06d",n%1000000);}catch(Exception e){throw new IllegalStateException(e);}}
 public static String normalize(String channel,String destination){
  if("mobile".equals(channel)&&destination!=null&&destination.matches("\\+91[6-9][0-9]{9}"))return destination;
  if("email".equals(channel)&&destination!=null&&destination.length()<=254){
   int at=destination.lastIndexOf('@');
   if(at>0&&at<=64){String local=destination.substring(0,at),domain=destination.substring(at+1).toLowerCase(Locale.ROOT);
    if(local.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+")&&!local.startsWith(".")&&!local.endsWith(".")&&!local.contains("..")&&domain.matches("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)+"))return local+"@"+domain;
   }
  }
  throw new ApiProblem(422,"INVALID_CONTACT","Enter a supported email address or an Indian mobile number for the selected channel.","destination");
 }
 public static String maskEmail(String email){return email.substring(0,1)+"•••@"+email.substring(email.lastIndexOf('@')+1);}
 public String proofFingerprint(String value){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(config.secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 private void lock(String key){db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key",key).query().singleRow();}
 @Transactional public Challenge request(String sessionId,UUID key,String channel,String destination,String name){
   return create(sessionId,key,channel,destination,name,"sign_in",null,null);
 }
 @Transactional public Challenge requestLink(String sessionId,UUID key,String channel,String destination,String name,UUID actor,int version){return create(sessionId,key,channel,destination,name,"link_"+channel,actor,version);}
 @Transactional public Challenge requestReauthentication(String sessionId,UUID key,String channel,String destination,String name,UUID actor,int version){return create(sessionId,key,channel,destination,name,"reauthenticate",actor,version);}
 public record RecoveryChallenge(UUID id,String status,String mode,String maskedDestination,Instant expiresAt,String outcomeToken,Instant outcomeExpiresAt){}
 public record RecoveryOutcome(UUID challengeId,String status,Instant completedAt){}
 @Transactional public RecoveryChallenge requestRecovery(String sessionId,UUID key,String channel,String destination){
  var c=create(sessionId,key,channel,destination,"Account recovery","recover_account",null,null);
  Instant expiry=db.sql("SELECT created_at+interval '15 minutes' FROM auth_challenge WHERE id=:id").param("id",c.id()).query(java.time.OffsetDateTime.class).single().toInstant();
  return new RecoveryChallenge(c.id(),c.status(),c.mode(),c.maskedDestination(),c.expiresAt(),proofFingerprint("recovery-outcome:"+c.id()+":"+hash(sessionId)),expiry);
 }
 @Transactional(readOnly=true) public RecoveryOutcome recoveryOutcome(UUID id,String token){
  if(!config.sandbox)throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","Recovery verification is not configured.");
  var rows=db.sql("SELECT *,created_at+interval '15 minutes'>clock_timestamp() AS grant_valid,expires_at>clock_timestamp() AS proof_valid FROM auth_challenge WHERE id=:id AND purpose='recover_account'").param("id",id).query().listOfRows();
  if(rows.isEmpty())throw new ApiProblem(404,"RECOVERY_OUTCOME_UNAVAILABLE","Recovery outcome is unavailable. Use ordinary sign-in with a new code.");var c=rows.getFirst();
  String expected=proofFingerprint("recovery-outcome:"+id+":"+c.get("session_hash"));
  if(!(Boolean)c.get("grant_valid")||token==null||!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),token.getBytes(StandardCharsets.UTF_8)))throw new ApiProblem(404,"RECOVERY_OUTCOME_UNAVAILABLE","Recovery outcome is unavailable. Use ordinary sign-in with a new code.");
  var completed=(java.sql.Timestamp)c.get("recovery_completed_at");
  String status=completed!=null?"completed":(Boolean)c.get("consumed")?"failed":!(Boolean)c.get("proof_valid")?"expired":((Number)c.get("attempts")).intValue()>=5?"locked":"not_completed";
  return new RecoveryOutcome(id,status,completed==null?null:completed.toInstant());
 }
 private Challenge create(String sessionId,UUID key,String channel,String destination,String name,String purpose,UUID actor,Integer version){
   String contact=normalize(channel,destination);boolean email=channel.equals("email");
   if(!config.sandbox&&!(config.live&&purpose.equals("sign_in")))throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","This verification flow is not configured. No code was sent.");
   if(config.live)lock("live-identity-request-budget");
   var binding=hash(sessionId);lock("challenge:"+channel+":"+contact);lock("challenge-session:"+binding);
   var prior=db.sql("SELECT * FROM auth_challenge WHERE session_hash=:s AND request_key=:k").param("s",binding).param("k",key).query().listOfRows().stream().findFirst();
   if(prior.isPresent()){
     var row=prior.get();if(!config.mode.equals(row.get("identity_mode"))||!purpose.equals(row.get("purpose"))||!Objects.equals(actor,row.get("link_account_id"))||!Objects.equals(version,row.get("link_profile_version"))||!channel.equals(row.get("channel"))||!contact.equals(row.get(email?"email":"mobile"))||!name.strip().equals(row.get("display_name")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This request key was already used with different details.");
     return view(row);
   }
   if(config.live&&db.sql("SELECT count(*) FROM auth_challenge WHERE identity_mode='live' AND created_at>clock_timestamp()-interval '1 hour'").query(Integer.class).single()>=config.hourlyLimit)throw new ApiProblem(429,"IDENTITY_BUDGET_EXCEEDED","Verification is temporarily at capacity. Try again later.");
   int count=db.sql("SELECT count(*) FROM auth_challenge WHERE ((channel=:channel AND (email=:m OR mobile=:m)) OR session_hash=:s) AND created_at>CURRENT_TIMESTAMP-interval '15 minutes'").param("m",contact).param("channel",channel).param("s",binding).query(Integer.class).single();
   if(count>=5)throw new ApiProblem(429,"CHALLENGE_RATE_LIMIT","Too many code requests. Wait fifteen minutes before trying again.");
   UUID id=UUID.randomUUID();
   db.sql("INSERT INTO auth_challenge(id,session_hash,channel,mobile,email,display_name,request_key,purpose,link_account_id,link_profile_version,expires_at,identity_mode) VALUES(:id,:s,:channel,:m,:email,:n,:k,:purpose,:actor,:version,CURRENT_TIMESTAMP+interval '5 minutes',:mode)").param("mode",config.mode).param("id",id).param("s",binding).param("channel",channel).param("m",email?null:contact,java.sql.Types.VARCHAR).param("email",email?contact:null,java.sql.Types.VARCHAR).param("n",name.strip()).param("k",key).param("purpose",purpose).param("actor",actor,java.sql.Types.OTHER).param("version",version,java.sql.Types.INTEGER).update();
   db.sql("INSERT INTO auth_delivery(challenge_id) VALUES(:id)").param("id",id).update();
   audit.record(null,null,"identity.challenge_requested",id);
   return status(id,sessionId);
 }
 private Challenge view(Map<String,Object> r){
   Instant expiry=((java.sql.Timestamp)r.get("expires_at")).toInstant();
   String status=(Boolean)r.get("consumed")?"consumed":expiry.isBefore(Instant.now())?"expired":((Number)r.get("attempts")).intValue()>=5?"locked":"pending";
   var mobile=(String)r.get("mobile");return new Challenge((UUID)r.get("id"),status,(String)r.get("identity_mode"),mobile==null?maskEmail((String)r.get("email")):"+91••••••"+mobile.substring(mobile.length()-4),expiry);
 }
 @Transactional(readOnly=true) public Challenge status(UUID id,String sessionId){
   var row=db.sql("SELECT * FROM auth_challenge WHERE id=:id AND session_hash=:s").param("id",id).param("s",hash(sessionId)).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"CHALLENGE_NOT_FOUND","Challenge is unavailable. Request a new code."));
   var base=view(row);
   if(!base.status().equals("pending"))return base;
   String delivery=db.sql("SELECT status FROM auth_delivery WHERE challenge_id=:id").param("id",id).query(String.class).single();
   return new Challenge(base.id(),delivery.equals("delivered")?"ready":delivery,base.mode(),base.maskedDestination(),base.expiresAt());
 }
 // Expected verification failures commit attempt/consumption evidence; infrastructure failures roll back.
 @Transactional(noRollbackFor=ApiProblem.class) public VerifiedAccount verify(UUID id,String sessionId,String submitted){
   if(!config.sandbox&&!config.live)throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","Contact verification is not configured.");
   var contact=db.sql("SELECT channel,coalesce(email,mobile) AS destination FROM auth_challenge WHERE id=:id AND session_hash=:s AND purpose='sign_in'").param("id",id).param("s",hash(sessionId)).query().listOfRows();
   if(contact.isEmpty())throw new ApiProblem(404,"CHALLENGE_NOT_FOUND","Sign-in challenge is unavailable.");
   lock("challenge:"+contact.getFirst().get("channel")+":"+contact.getFirst().get("destination"));
   var row=db.sql("SELECT * FROM auth_challenge WHERE id=:id AND session_hash=:s AND purpose='sign_in' FOR UPDATE").param("id",id).param("s",hash(sessionId)).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"CHALLENGE_NOT_FOUND","Challenge is unavailable."));
   if(!config.mode.equals(row.get("identity_mode")))throw new ApiProblem(403,"IDENTITY_MODE_CONFLICT","Request a new code in this environment.");
   if(!view(row).status().equals("pending"))throw new ApiProblem(409,"CHALLENGE_UNUSABLE","This code expired, was used, or reached its attempt limit. Request a new code.");
   db.sql("UPDATE auth_challenge SET attempts=attempts+1 WHERE id=:id").param("id",id).update();
   if(!MessageDigest.isEqual(code(id).getBytes(StandardCharsets.UTF_8),submitted.getBytes(StandardCharsets.UTF_8))){audit.record(null,null,"identity.challenge_rejected",id);throw new ApiProblem(422,"INVALID_CODE","The code is incorrect.","code");}
   db.sql("UPDATE auth_challenge SET consumed=true WHERE id=:id").param("id",id).update();
   var accountId=UUID.randomUUID();
   boolean email="email".equals(row.get("channel"));String column=email?"email":"mobile";
   // Column is selected exclusively by the stored allowlisted channel, never supplied SQL.
   db.sql("INSERT INTO user_account(id,"+column+",display_name,identity_mode) VALUES(:id,:contact,:n,:mode) ON CONFLICT("+column+") DO NOTHING").param("mode",config.mode).param("id",accountId).param("contact",row.get(column)).param("n",row.get("display_name")).update();
   var account=db.sql("SELECT id,display_name,identity_mode FROM user_account WHERE "+column+"=:contact").param("contact",row.get(column)).query((rs,n)->new Account(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3))).single();
   if(!account.mode().equals(config.mode))throw new ApiProblem(403,"IDENTITY_MODE_CONFLICT","This contact belongs to a different identity environment.");
   audit.record(account.id(),null,"identity.proof_consumed",id);lock("account-session-generation:"+account.id());int generation=db.sql("SELECT auth_generation FROM user_account WHERE id=:id AND (auth_recovered_at IS NULL OR auth_recovered_at<:issued)").param("id",account.id()).param("issued",row.get("created_at")).query(Integer.class).optional().orElseThrow(()->new ApiProblem(409,"AUTHENTICATION_STALE","Account recovery invalidated this code. Request a new code."));return new VerifiedAccount(account,generation);
 }
 @Transactional(readOnly=true) public Account account(UUID id){return db.sql("SELECT id,display_name,identity_mode FROM user_account WHERE id=:id").param("id",id).query((rs,n)->new Account(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3))).optional().orElseThrow(()->new ApiProblem(401,"UNAUTHENTICATED","Sign in again."));}
 @Transactional(noRollbackFor=ApiProblem.class) public VerifiedAccount recover(UUID id,String sessionId,String submitted){
  if(!config.sandbox)throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","Recovery verification is not configured.");
  var rows=db.sql("SELECT *,expires_at>clock_timestamp() AS valid FROM auth_challenge WHERE id=:id AND session_hash=:s AND purpose='recover_account' FOR UPDATE").param("id",id).param("s",hash(sessionId)).query().listOfRows();
  if(rows.isEmpty())throw new ApiProblem(404,"CHALLENGE_NOT_FOUND","Recovery challenge is unavailable.");var c=rows.getFirst();
  if((Boolean)c.get("consumed")||!(Boolean)c.get("valid")||((Number)c.get("attempts")).intValue()>=5)throw new ApiProblem(409,"CHALLENGE_UNUSABLE","This recovery code expired, was used or reached its attempt limit.");
  db.sql("UPDATE auth_challenge SET attempts=attempts+1 WHERE id=:id").param("id",id).update();
  if(!MessageDigest.isEqual(code(id).getBytes(StandardCharsets.UTF_8),submitted.getBytes(StandardCharsets.UTF_8))){audit.record(null,null,"identity.recovery_proof_rejected",id);throw new ApiProblem(422,"INVALID_CODE","The code is incorrect.","code");}
  String column="email".equals(c.get("channel"))?"email":"mobile";
  var accounts=db.sql("SELECT * FROM user_account WHERE "+column+"=:contact AND identity_mode='sandbox'").param("contact",c.get(column)).query().listOfRows();
  db.sql("UPDATE auth_challenge SET consumed=true WHERE id=:id").param("id",id).update();
  if(accounts.isEmpty())throw new ApiProblem(409,"RECOVERY_UNAVAILABLE","Recovery cannot be completed with this contact. Use another linked contact or the ordinary registration flow.");
  var a=accounts.getFirst();UUID actor=(UUID)a.get("id");
  // Same lock as session registration: a proof verified before recovery cannot register afterward.
  lock("account-session-generation:"+actor);
  if(!db.sql("SELECT auth_recovered_at IS NULL OR auth_recovered_at<:issued FROM user_account WHERE id=:a").param("issued",c.get("created_at")).param("a",actor).query(Boolean.class).single())throw new ApiProblem(409,"CHALLENGE_UNUSABLE","A newer account recovery invalidated this code. Request a new code.");
  db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",actor.toString()).query().singleRow();
  db.sql("SELECT id FROM account_session WHERE account_id=:a ORDER BY id FOR UPDATE").param("a",actor).query().listOfRows();
  db.sql("UPDATE account_session SET revoked_at=clock_timestamp(),version=version+1 WHERE account_id=:a AND revoked_at IS NULL").param("a",actor).update();
  db.sql("DELETE FROM spring_session WHERE principal_name=:a").param("a",actor.toString()).update();
  int generation=db.sql("UPDATE user_account SET auth_generation=auth_generation+1,auth_recovered_at=clock_timestamp(),version=version+1 WHERE id=:a RETURNING auth_generation").param("a",actor).query(Integer.class).single();
  db.sql("UPDATE auth_challenge SET recovery_completed_at=clock_timestamp() WHERE id=:id").param("id",id).update();
  audit.record(actor,null,"identity.account_recovered",id);
  db.sql("INSERT INTO account_notification(id,account_id,message) VALUES(:id,:a,'Account recovery through a linked contact ended existing browser sessions. Review account security if this was unexpected.')").param("id",UUID.randomUUID()).param("a",actor).update();
  return new VerifiedAccount(new Account(actor,(String)a.get("display_name"),"sandbox"),generation);
 }

}
