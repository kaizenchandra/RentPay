package in.lmt.workspace;
import static org.assertj.core.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT) @ActiveProfiles("local")
class OperationsIntegrationTest {
 static final PostgreSQLContainer db=new PostgreSQLContainer("postgres:17.11");static final AtomicInteger mobiles=new AtomicInteger(300000000);static final Path inbox;
 static{try{inbox=Files.createTempDirectory("lmt-operation-inbox-");db.start();try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");}}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 @DynamicPropertySource static void config(DynamicPropertyRegistry r){r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",()->"lmt_runtime");r.add("spring.datasource.password",()->"runtime_test");r.add("spring.flyway.user",db::getUsername);r.add("spring.flyway.password",db::getPassword);r.add("lmt.identity.sandbox-secret",()->"test-operation-secret-longer-than-32-characters");r.add("lmt.identity.inbox",inbox::toString);r.add("lmt.identity.delivery-delay-ms",()->"25");r.add("lmt.workspace.initial-delay-ms",()->"3600000");}
 @LocalServerPort int port;@Autowired in.lmt.identity.NotificationPreferences preferences;@Autowired WelcomeOperations operations;@Autowired WorkspaceWelcomeDelivery worker;
 final ObjectMapper json=new ObjectMapper();
 class Browser {
  final HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();String csrf;
  HttpResponse<String> send(String path,String body,String key,String tag)throws Exception{
   var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1"+path));
   if(body==null)b.GET();else{b.POST(HttpRequest.BodyPublishers.ofString(body)).header("Content-Type","application/json").header("Origin","http://localhost:3000").header("X-CSRF-Token",csrf);if(key!=null)b.header("Idempotency-Key",key);if(tag!=null)b.header("If-Match",tag);}
   return client.send(b.build(),HttpResponse.BodyHandlers.ofString());
  }
  JsonNode get(String path)throws Exception{var r=send(path,null,null,null);assertThat(r.statusCode()).as(r.body()).isEqualTo(200);return json.readTree(r.body());}
  void login()throws Exception{csrf=get("/auth/csrf").get("token").asString();var c=send("/auth/challenges",json.writeValueAsString(Map.of("channel","mobile","destination","+919"+mobiles.incrementAndGet(),"purpose","sign_in","displayName","Operation Owner")),UUID.randomUUID().toString(),null);assertThat(c.statusCode()).as(c.body()).isEqualTo(202);String id=json.readTree(c.body()).get("id").asString();String code=null;for(int n=0;n<100;n++){var f=inbox.resolve(id+".txt");if(Files.exists(f)&&Files.size(f)==6){code=Files.readString(f);break;}Thread.sleep(25);}assertThat(send("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",code)),null,null).statusCode()).isEqualTo(200);csrf=get("/auth/csrf").get("token").asString();}
  UUID provision()throws Exception{var plan=get("/public/plans/FREE");var r=send("/workspaces",json.writeValueAsString(Map.of("profile",Map.of("name","Operations test","customerType","individual","city","Pune","state","Maharashtra"),"selectedPlanVersionId",plan.get("versionId").asString(),"consentIds",List.of("sandbox-use-v1"))),UUID.randomUUID().toString(),null);assertThat(r.statusCode()).as(r.body()).isEqualTo(201);return UUID.fromString(json.readTree(r.body()).get("id").asString());}
 }
 void sql(String sql)throws Exception{try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute(sql);}}
 long count(String sql)throws Exception{try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getLong(1);}}
 UUID event(Browser b,UUID w)throws Exception{return UUID.fromString(b.get("/workspaces/"+w+"/operations").get("items").get(0).get("id").asString());}
 String etag(Browser b,UUID id)throws Exception{return b.get("/operations/"+id).get("operation").get("etag").asString();}
 @Test void claimsAreExclusiveAndCompletionIsDeduplicated()throws Exception{
  Browser b=new Browser(),other=new Browser();b.login();other.login();UUID w=b.provision(),id=event(b,w);List<WelcomeOperations.Claim> claims=new ArrayList<>();
  try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(()->operations.claim());var c=pool.submit(()->operations.claim());claims.addAll(a.get());claims.addAll(c.get());}
  assertThat(claims).hasSize(1);operations.complete(claims.getFirst());operations.complete(claims.getFirst());
  assertThat(b.get("/operations/"+id).get("operation").get("status").asString()).isEqualTo("delivered");assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+id+"'")).isEqualTo(1);
  var foreign=other.send("/operations/"+id,null,null,null);assertThat(foreign.statusCode()).isEqualTo(404);assertThat(json.readTree(foreign.body()).get("code").asString()).isEqualTo("OPERATION_NOT_FOUND");
  assertThat(json.readTree(other.send("/operations/"+UUID.randomUUID(),null,null,null).body()).get("code").asString()).isEqualTo("OPERATION_NOT_FOUND");
  assertThat(b.send("/operations/"+id+"/cancel-request","{\"reason\":\"Cancel completed delivery\"}",UUID.randomUUID().toString(),etag(b,id)).statusCode()).isEqualTo(409);
 }
 @Test void expiredLeaseAndRevokedAuthorityRequireExplicitRecovery()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision(),id=event(b,w);var old=operations.claim().getFirst();sql("UPDATE workspace_outbox SET lease_until=CURRENT_TIMESTAMP-interval '1 second' WHERE event_id='"+id+"'");var current=operations.claim().getFirst();operations.complete(old);assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+id+"'")).isZero();
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");operations.complete(current);assertThat(b.send("/operations/"+id,null,null,null).statusCode()).isEqualTo(404);assertThat(count("SELECT count(*) FROM workspace_outbox WHERE event_id='"+id+"' AND status='blocked'")).isEqualTo(1);
  sql("UPDATE workspace_member SET active=true WHERE workspace_id='"+w+"'");String tag=etag(b,id),key=UUID.randomUUID().toString(),body="{\"reason\":\"Authority restored and reviewed\"}",path="/operations/"+id+"/retry-failed";
  assertThat(b.send(path,body,key,null).statusCode()).isEqualTo(428);assertThat(b.send(path,body,key,"\"stale\"").statusCode()).isEqualTo(412);
  var accepted=b.send(path,body,key,tag);assertThat(accepted.statusCode()).as(accepted.body()).isEqualTo(202);assertThat(json.readTree(b.send(path,body,key,tag).body())).isEqualTo(json.readTree(accepted.body()));assertThat(b.send(path,body.replace("reviewed","confirmed"),key,tag).statusCode()).isEqualTo(409);
  worker.deliver();assertThat(b.get("/operations/"+id).get("operation").get("status").asString()).isEqualTo("delivered");assertThat(count("SELECT count(*) FROM operation_attempt WHERE event_id='"+id+"'")).isEqualTo(3);assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+id+"'")).isEqualTo(1);
 }
 @Test void boundedFailuresCanBeRetriedAndCancellationFencesWorker()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision(),id=event(b,w);
  sql("CREATE FUNCTION test_delivery_failure() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Injected test delivery failure'; END; $$");sql("CREATE TRIGGER test_delivery_failure BEFORE INSERT ON workspace_notification FOR EACH ROW EXECUTE FUNCTION test_delivery_failure()");
  try{for(int n=0;n<5;n++){worker.deliver();sql("UPDATE workspace_outbox SET next_attempt_at=CURRENT_TIMESTAMP WHERE event_id='"+id+"'");}}finally{sql("DROP TRIGGER test_delivery_failure ON workspace_notification");sql("DROP FUNCTION test_delivery_failure()");}
  var failed=b.get("/operations/"+id).get("operation");assertThat(failed.get("status").asString()).isEqualTo("failed");assertThat(failed.get("attempts").asInt()).isEqualTo(5);assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+id+"'")).isZero();
  assertThat(b.send("/operations/"+id+"/retry-failed","{\"reason\":\"Delivery dependency repaired\"}",UUID.randomUUID().toString(),failed.get("etag").asString()).statusCode()).isEqualTo(202);worker.deliver();assertThat(b.get("/operations/"+id).get("operation").get("attempts").asInt()).isEqualTo(6);
  UUID cancelledWorkspace=b.provision(),cancelled=event(b,cancelledWorkspace);var running=operations.claim().getFirst();String key=UUID.randomUUID().toString(),tag=etag(b,cancelled),body="{\"reason\":\"Welcome notice no longer required\"}",path="/operations/"+cancelled+"/cancel-request";
  assertThat(b.send(path,"{\"reason\":\"          x\"}",UUID.randomUUID().toString(),tag).statusCode()).isEqualTo(422);
  assertThat(b.send(path,body,key,tag).statusCode()).isEqualTo(200);assertThat(b.send(path,body,key,tag).statusCode()).isEqualTo(200);operations.complete(running);assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+cancelled+"'")).isZero();assertThat(b.get("/operations/"+cancelled).get("operation").get("status").asString()).isEqualTo("cancelled");
 }

 @Test void revokedJobsCannotStarveLaterAuthorizedWork()throws Exception{
  Browser b=new Browser();b.login();List<UUID> blocked=new ArrayList<>();for(int n=0;n<11;n++)blocked.add(b.provision());
  for(UUID w:blocked)sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");
  UUID available=b.provision(),id=event(b,available);worker.deliver();worker.deliver();
  assertThat(b.get("/operations/"+id).get("operation").get("status").asString()).isEqualTo("delivered");
  for(UUID w:blocked)assertThat(count("SELECT count(*) FROM workspace_outbox WHERE scope_id='"+w+"' AND status='blocked' AND last_code='AUTHORITY_REVOKED'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+id+"'")).isEqualTo(1);
 }

 @Test void optionalAccountCopyRechecksConsentAndNeverBackfills()throws Exception{
  Browser b=new Browser();b.login();UUID actor=UUID.fromString(b.get("/auth/session").get("account").get("id").asString());
  UUID old=b.provision(),oldEvent=event(b,old);var enabled=preferences.update(actor,UUID.randomUUID(),preferences.get(actor).etag(),true);
  var oldClaim=operations.claim().stream().filter(c->c.workspace().equals(old)).findFirst().orElseThrow();operations.complete(oldClaim);
  assertThat(count("SELECT count(*) FROM account_notification WHERE source_event_id='"+oldEvent+"'")).isZero();
  UUID ready=b.provision(),readyEvent=event(b,ready);var claim=operations.claim().stream().filter(c->c.workspace().equals(ready)).findFirst().orElseThrow();operations.complete(claim);operations.complete(claim);
  assertThat(count("SELECT count(*) FROM account_notification WHERE source_event_id='"+readyEvent+"' AND account_id='"+actor+"' AND category='workspace_setup'")).isEqualTo(1);
  UUID pending=b.provision(),pendingEvent=event(b,pending);var held=operations.claim().stream().filter(c->c.workspace().equals(pending)).findFirst().orElseThrow();preferences.update(actor,UUID.randomUUID(),enabled.etag(),false);operations.complete(held);
  assertThat(count("SELECT count(*) FROM account_notification WHERE source_event_id='"+pendingEvent+"'")).isZero();assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+pendingEvent+"'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='notification.workspace_setup_suppressed'")).isEqualTo(2);
 }

 @Test void personalDeliveryQueueIsRecipientScopedPagedAndConsentBound()throws Exception{
  Browser owner=new Browser(),other=new Browser(),anonymous=new Browser();owner.login();other.login();
  UUID actor=UUID.fromString(owner.get("/auth/session").get("account").get("id").asString());
  assertThat(anonymous.send("/me/notification-deliveries",null,null,null).statusCode()).isEqualTo(401);
  owner.provision();assertThat(owner.get("/me/notification-deliveries").get("totalMatching").asInt()).isZero();
  preferences.update(actor,UUID.randomUUID(),preferences.get(actor).etag(),true);
  UUID w=owner.provision(),id=event(owner,w);
  sql("INSERT INTO workspace_outbox(event_id,actor_id,event_type,aggregate_id,scope_id,correlation_id,causation_id) SELECT gen_random_uuid(),actor_id,event_type,aggregate_id,scope_id,gen_random_uuid(),gen_random_uuid() FROM workspace_outbox CROSS JOIN generate_series(1,25) WHERE event_id='"+id+"'");
  var first=owner.get("/me/notification-deliveries");assertThat(first.get("totalMatching").asInt()).isEqualTo(26);assertThat(first.get("items").size()).isEqualTo(25);
  String cursor=first.get("page").get("nextCursor").asString();var second=owner.get("/me/notification-deliveries?cursor="+cursor);assertThat(second.get("items").size()).isEqualTo(1);
  var ids=new HashSet<String>();first.get("items").forEach(n->ids.add(n.get("id").asString()));assertThat(ids.add(second.get("items").get(0).get("id").asString())).isTrue();
  assertThat(other.get("/me/notification-deliveries").get("totalMatching").asInt()).isZero();assertThat(other.send("/me/notification-deliveries?cursor="+cursor,null,null,null).statusCode()).isEqualTo(422);
  assertThat(owner.send("/me/notification-deliveries?cursor=bad",null,null,null).statusCode()).isEqualTo(422);
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");assertThat(owner.get("/me/notification-deliveries").get("totalMatching").asInt()).isZero();
  sql("UPDATE workspace_member SET active=true WHERE workspace_id='"+w+"'");
  sql("UPDATE workspace_outbox SET status='delivered' WHERE scope_id='"+w+"'");assertThat(owner.get("/me/notification-deliveries").get("totalMatching").asInt()).isZero();
  sql("UPDATE workspace_outbox SET status='failed' WHERE scope_id='"+w+"'");assertThat(owner.get("/me/notification-deliveries").get("totalMatching").asInt()).isEqualTo(26);
  preferences.update(actor,UUID.randomUUID(),preferences.get(actor).etag(),false);assertThat(owner.get("/me/notification-deliveries").get("totalMatching").asInt()).isZero();
 }
 @Test void quietDeferralsAreDurableAndDoNotExhaustFailureBudget()throws Exception{
  Browser b=new Browser();b.login();UUID actor=UUID.fromString(b.get("/auth/session").get("account").get("id").asString());
  var now=java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata"));var format=java.time.format.DateTimeFormatter.ofPattern("HH:mm");
  preferences.update(actor,UUID.randomUUID(),preferences.get(actor).etag(),true,true,now.minusHours(1).format(format),now.plusHours(1).format(format));
  UUID w=b.provision(),id=event(b,w);
  for(int i=0;i<7;i++){
   sql("UPDATE workspace_outbox SET next_attempt_at=CURRENT_TIMESTAMP WHERE event_id='"+id+"'");
   var claim=operations.claim().stream().filter(c->c.workspace().equals(w)).findFirst().orElseThrow();operations.complete(claim);
   assertThat(count("SELECT count(*) FROM workspace_outbox WHERE event_id='"+id+"' AND status='pending' AND last_code='QUIET_HOURS_DEFERRED' AND cycle_attempts=0 AND next_attempt_at>CURRENT_TIMESTAMP AND next_attempt_at<=CURRENT_TIMESTAMP+interval '61 seconds'")).isEqualTo(1);
  }
  assertThat(count("SELECT count(*) FROM workspace_notification WHERE event_id='"+id+"'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM account_notification WHERE source_event_id='"+id+"'")).isZero();
  assertThat(count("SELECT count(*) FROM operation_attempt WHERE event_id='"+id+"' AND outcome='quiet_hours_deferred'")).isEqualTo(7);
  assertThat(b.get("/me/notification-deliveries").get("items").get(0).get("lastCode").asString()).isEqualTo("QUIET_HOURS_DEFERRED");
  preferences.update(actor,UUID.randomUUID(),preferences.get(actor).etag(),true,false,"22:00","07:00");sql("UPDATE workspace_outbox SET next_attempt_at=CURRENT_TIMESTAMP WHERE event_id='"+id+"'");
  var ready=operations.claim().stream().filter(c->c.workspace().equals(w)).findFirst().orElseThrow();operations.complete(ready);operations.complete(ready);
  assertThat(count("SELECT count(*) FROM account_notification WHERE source_event_id='"+id+"'")).isEqualTo(1);assertThat(b.get("/operations/"+id).get("operation").get("status").asString()).isEqualTo("delivered");
 }
}
