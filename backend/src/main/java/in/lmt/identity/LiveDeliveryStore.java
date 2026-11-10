package in.lmt.identity;
import in.lmt.shared.Audit;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class LiveDeliveryStore {
 public record Dispatch(UUID id,UUID token,String channel,String destination,int attempts){}
 private final JdbcClient db;private final Audit audit;
 public LiveDeliveryStore(JdbcClient db,Audit audit){this.db=db;this.audit=audit;}
 @Transactional public Optional<Dispatch> claim(){return claim("live");}
 @Transactional public Optional<Dispatch> claim(String mode){
  // A lost SMS submission is never blindly sent again. Email uses provider idempotency.
  var recovered=db.sql("UPDATE auth_delivery d SET status=CASE WHEN c.identity_mode='live' AND c.channel='email' AND d.attempts<3 THEN 'pending' ELSE 'unknown' END,last_code='PROVIDER_OUTCOME_UNKNOWN',next_attempt_at=clock_timestamp()+interval '10 seconds',dispatch_token=NULL FROM auth_challenge c WHERE c.id=d.challenge_id AND c.identity_mode=:mode AND d.status='dispatching' AND d.dispatched_at<clock_timestamp()-interval '30 seconds' RETURNING d.challenge_id,d.status").param("mode",mode).query().listOfRows();
  for(var row:recovered)audit.record(null,null,"identity.delivery_recovered_"+row.get("status"),(UUID)row.get("challenge_id"));
  db.sql("UPDATE auth_delivery d SET status='expired',dispatch_token=NULL FROM auth_challenge c WHERE c.id=d.challenge_id AND c.identity_mode=:mode AND d.status IN ('pending','unknown','accepted') AND (c.consumed OR c.expires_at<=clock_timestamp())").param("mode",mode).update();
  var rows=db.sql("SELECT c.id,c.channel,coalesce(c.email,c.mobile) AS destination,d.attempts FROM auth_delivery d JOIN auth_challenge c ON c.id=d.challenge_id WHERE c.identity_mode=:mode AND (c.identity_mode='sandbox' OR c.purpose='sign_in') AND NOT c.consumed AND c.expires_at>clock_timestamp() AND d.status='pending' AND d.next_attempt_at<=clock_timestamp() ORDER BY c.created_at LIMIT 1 FOR UPDATE OF d SKIP LOCKED").param("mode",mode).query().listOfRows();
  if(rows.isEmpty())return Optional.empty();var row=rows.getFirst();UUID id=(UUID)row.get("id"),token=UUID.randomUUID();
  db.sql("UPDATE auth_delivery SET status='dispatching',dispatch_token=:token,dispatched_at=clock_timestamp(),attempts=attempts+1 WHERE challenge_id=:id").param("token",token).param("id",id).update();audit.record(null,null,"identity.delivery_dispatched",id);
  return Optional.of(new Dispatch(id,token,(String)row.get("channel"),(String)row.get("destination"),((Number)row.get("attempts")).intValue()+1));
 }
 @Transactional public void finish(Dispatch work,LiveOtpTransport.Result result){
  String status=result.status();if(status.equals("retry"))status=work.attempts()<3?"pending":"unknown";
  int changed=db.sql("UPDATE auth_delivery SET status=:status,provider_reference=:reference,last_code=:code,dispatch_token=NULL,next_attempt_at=clock_timestamp()+(:delay * interval '1 second') WHERE challenge_id=:id AND status='dispatching' AND dispatch_token=:token")
   .param("status",status).param("reference",result.reference(),java.sql.Types.VARCHAR).param("code",result.code(),java.sql.Types.VARCHAR).param("delay",10*work.attempts()+java.util.concurrent.ThreadLocalRandom.current().nextInt(3)).param("id",work.id()).param("token",work.token()).update();
  if(changed>0)audit.record(null,null,"identity.delivery_"+status,work.id());
 }
}
