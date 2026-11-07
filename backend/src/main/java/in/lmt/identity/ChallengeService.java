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
 public static String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 public String code(UUID id){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(config.secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));byte[] b=mac.doFinal(id.toString().getBytes(StandardCharsets.UTF_8));long n=java.nio.ByteBuffer.wrap(b).getLong() & Long.MAX_VALUE;return String.format(Locale.ROOT,"%06d",n%1000000);}catch(Exception e){throw new IllegalStateException(e);}}
 private void lock(String key){db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key",key).query().singleRow();}
 @Transactional public Challenge request(String sessionId,UUID key,String mobile,String name){
   if(!config.sandbox)throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","Mobile verification is not configured. No code was sent.");
   var binding=hash(sessionId);lock("challenge:"+mobile);lock("challenge-session:"+binding);
   var prior=db.sql("SELECT * FROM auth_challenge WHERE session_hash=:s AND request_key=:k").param("s",binding).param("k",key).query().listOfRows().stream().findFirst();
   if(prior.isPresent()){
     var row=prior.get();if(!mobile.equals(row.get("mobile"))||!name.strip().equals(row.get("display_name")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This request key was already used with different details.");
     return view(row);
   }
   int count=db.sql("SELECT count(*) FROM auth_challenge WHERE (mobile=:m OR session_hash=:s) AND created_at>CURRENT_TIMESTAMP-interval '15 minutes'").param("m",mobile).param("s",binding).query(Integer.class).single();
   if(count>=5)throw new ApiProblem(429,"CHALLENGE_RATE_LIMIT","Too many code requests. Wait fifteen minutes before trying again.");
   UUID id=UUID.randomUUID();
   db.sql("INSERT INTO auth_challenge(id,session_hash,mobile,display_name,request_key,expires_at) VALUES(:id,:s,:m,:n,:k,CURRENT_TIMESTAMP+interval '5 minutes')").param("id",id).param("s",binding).param("m",mobile).param("n",name.strip()).param("k",key).update();
   db.sql("INSERT INTO auth_delivery(challenge_id) VALUES(:id)").param("id",id).update();
   audit.record(null,null,"identity.challenge_requested",id);
   return status(id,sessionId);
 }
 private Challenge view(Map<String,Object> r){
   Instant expiry=((java.sql.Timestamp)r.get("expires_at")).toInstant();
   String status=(Boolean)r.get("consumed")?"consumed":expiry.isBefore(Instant.now())?"expired":((Number)r.get("attempts")).intValue()>=5?"locked":"pending";
   var mobile=(String)r.get("mobile");return new Challenge((UUID)r.get("id"),status,"sandbox","+91••••••"+mobile.substring(mobile.length()-4),expiry);
 }
 @Transactional(readOnly=true) public Challenge status(UUID id,String sessionId){
   var row=db.sql("SELECT * FROM auth_challenge WHERE id=:id AND session_hash=:s").param("id",id).param("s",hash(sessionId)).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"CHALLENGE_NOT_FOUND","Challenge is unavailable. Request a new code."));
   var base=view(row);
   if(!base.status().equals("pending"))return base;
   String delivery=db.sql("SELECT status FROM auth_delivery WHERE challenge_id=:id").param("id",id).query(String.class).single();
   return new Challenge(base.id(),delivery.equals("delivered")?"ready":delivery,base.mode(),base.maskedDestination(),base.expiresAt());
 }
 // Expected verification failures commit attempt/consumption evidence; infrastructure failures roll back.
 @Transactional(noRollbackFor=ApiProblem.class) public Account verify(UUID id,String sessionId,String submitted){
   if(!config.sandbox)throw new ApiProblem(503,"IDENTITY_UNAVAILABLE","Mobile verification is not configured.");
   var row=db.sql("SELECT * FROM auth_challenge WHERE id=:id AND session_hash=:s FOR UPDATE").param("id",id).param("s",hash(sessionId)).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"CHALLENGE_NOT_FOUND","Challenge is unavailable."));
   if(!view(row).status().equals("pending"))throw new ApiProblem(409,"CHALLENGE_UNUSABLE","This code expired, was used, or reached its attempt limit. Request a new code.");
   db.sql("UPDATE auth_challenge SET attempts=attempts+1 WHERE id=:id").param("id",id).update();
   if(!MessageDigest.isEqual(code(id).getBytes(StandardCharsets.UTF_8),submitted.getBytes(StandardCharsets.UTF_8))){audit.record(null,null,"identity.challenge_rejected",id);throw new ApiProblem(422,"INVALID_CODE","The code is incorrect.","code");}
   db.sql("UPDATE auth_challenge SET consumed=true WHERE id=:id").param("id",id).update();
   var accountId=UUID.randomUUID();
   db.sql("INSERT INTO user_account(id,mobile,display_name,identity_mode) VALUES(:id,:m,:n,'sandbox') ON CONFLICT(mobile) DO NOTHING").param("id",accountId).param("m",row.get("mobile")).param("n",row.get("display_name")).update();
   var account=db.sql("SELECT id,display_name,identity_mode FROM user_account WHERE mobile=:m").param("m",row.get("mobile")).query((rs,n)->new Account(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3))).single();
   audit.record(account.id(),null,"identity.proof_consumed",id);return account;
 }
 @Transactional(readOnly=true) public Account account(UUID id){return db.sql("SELECT id,display_name,identity_mode FROM user_account WHERE id=:id").param("id",id).query((rs,n)->new Account(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3))).optional().orElseThrow(()->new ApiProblem(401,"UNAUTHENTICATED","Sign in again."));}
}
