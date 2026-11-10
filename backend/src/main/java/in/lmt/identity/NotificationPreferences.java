package in.lmt.identity;
import in.lmt.shared.*;
import java.util.*;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class NotificationPreferences {
 public record View(boolean workspaceSetupEnabled,Instant enabledAt,int version,String etag,boolean quietHoursEnabled,String quietStart,String quietEnd){public View{if(quietStart==null)quietStart="22:00";if(quietEnd==null)quietEnd="07:00";}}
 private final JdbcClient db;private final Audit audit;private final ObjectMapper json;
 public NotificationPreferences(JdbcClient db,Audit audit,ObjectMapper json){this.db=db;this.audit=audit;this.json=json;}
 private void scope(UUID actor){db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",actor.toString()).query().singleRow();}
 private void lock(UUID actor){scope(actor);db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k","notification-preferences:"+actor).query().singleRow();}
 private String tag(UUID actor,int version){return "\"notification-preferences-"+actor+"-v"+version+"\"";}
 @Transactional(readOnly=true) public View get(UUID actor){scope(actor);return db.sql("SELECT * FROM account_notification_preference WHERE account_id=:a").param("a",actor).query((r,n)->new View(r.getBoolean("workspace_setup_enabled"),r.getTimestamp("enabled_at")==null?null:r.getTimestamp("enabled_at").toInstant(),r.getInt("version"),tag(actor,r.getInt("version")),r.getBoolean("quiet_hours_enabled"),r.getString("quiet_start"),r.getString("quiet_end"))).optional().orElse(new View(false,null,0,tag(actor,0),false,"22:00","07:00"));}
 @Transactional public View update(UUID actor,UUID key,String expected,boolean enabled){return update(actor,key,expected,enabled,null,null,null);}
 @Transactional public View update(UUID actor,UUID key,String expected,boolean enabled,Boolean quiet,String start,String end){
  boolean specified=quiet!=null||start!=null||end!=null;
  if(specified&&(quiet==null||start==null||end==null||!start.matches("([01][0-9]|2[0-3]):[0-5][0-9]")||!end.matches("([01][0-9]|2[0-3]):[0-5][0-9]")||start.equals(end)))throw new ApiProblem(422,"INVALID_QUIET_HOURS","Provide quiet hours with distinct start and end times in HH:mm format.","quietStart");
  lock(actor);String hash=ChallengeService.hash(expected+":"+enabled+(specified?":"+quiet+":"+start+":"+end:"")),operation="identity.notification_preferences_updated";
  var prior=db.sql("SELECT request_hash,result::text FROM identity_command WHERE actor_id=:a AND operation=:o AND request_key=:k").param("a",actor).param("o",operation).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){if(!hash.equals(prior.getFirst().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to another preference change.");return json.readValue((String)prior.getFirst().get("result"),View.class);}
  if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Load current preferences before saving.");
  if(!get(actor).etag().equals(expected))throw new ApiProblem(412,"VERSION_STALE","Preferences changed. Reload and review your selection.");
  db.sql("INSERT INTO account_notification_preference(account_id,workspace_setup_enabled,enabled_at,version) VALUES(:a,:enabled,CASE WHEN :enabled THEN clock_timestamp() ELSE NULL END,1) ON CONFLICT(account_id) DO UPDATE SET workspace_setup_enabled=:enabled,enabled_at=CASE WHEN :enabled THEN coalesce(account_notification_preference.enabled_at,clock_timestamp()) ELSE NULL END,version=account_notification_preference.version+1").param("a",actor).param("enabled",enabled).update();
  if(specified)db.sql("UPDATE account_notification_preference SET quiet_hours_enabled=:quiet,quiet_start=:start,quiet_end=:end WHERE account_id=:a").param("quiet",quiet).param("start",start).param("end",end).param("a",actor).update();
  var result=get(actor);
  db.sql("INSERT INTO identity_command(actor_id,operation,request_key,request_hash,result) VALUES(:a,:o,:k,:h,CAST(:r AS jsonb))").param("a",actor).param("o",operation).param("k",key).param("h",hash).param("r",json.writeValueAsString(result)).update();audit.record(actor,null,operation,actor);
  db.sql("INSERT INTO account_notification(id,account_id,message) VALUES(:id,:a,'Your optional workspace setup notification preference was changed. Essential security notices remain enabled.')").param("id",UUID.randomUUID()).param("a",actor).update();return result;
 }
 /** Called only by the authorized workspace outbox handler, in its transaction. */
 @Transactional public Instant workspaceReady(UUID actor,UUID event,Instant occurredAt){
  lock(actor);var preference=get(actor);
  if(!preference.workspaceSetupEnabled()||occurredAt.isBefore(preference.enabledAt())){audit.record(actor,null,"notification.workspace_setup_suppressed",event);return null;}
  Instant now=db.sql("SELECT clock_timestamp()").query(java.time.OffsetDateTime.class).single().toInstant();
  Instant resume=QuietHours.resumeAt(now,preference.quietHoursEnabled(),preference.quietStart(),preference.quietEnd());
  if(resume!=null)return resume.isBefore(now.plusSeconds(60))?resume:now.plusSeconds(60);
  int inserted=db.sql("INSERT INTO account_notification(id,account_id,message,category,source_event_id) VALUES(:id,:a,'A workspace you created is ready. Open Your workspaces to review its setup.','workspace_setup',:event) ON CONFLICT DO NOTHING").param("id",UUID.randomUUID()).param("a",actor).param("event",event).update();
  if(inserted>0)audit.record(actor,null,"notification.workspace_setup_delivered",event);
  return null;
 }
}
