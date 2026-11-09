package in.lmt.identity;
import static org.assertj.core.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class OfferingAvailabilityIntegrationTest {
 static final PostgreSQLContainer db=new PostgreSQLContainer("postgres:17.11");
 static final Path inbox;
 static final AtomicInteger mobiles=new AtomicInteger(100000000);
 static final String free="00000000-0000-4000-8000-000000000006";
 static {try{inbox=Files.createTempDirectory("lmt-test-inbox-");db.start();try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");}}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 @DynamicPropertySource static void properties(DynamicPropertyRegistry r){
  r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",()->"lmt_runtime");r.add("spring.datasource.password",()->"runtime_test");r.add("spring.flyway.user",db::getUsername);r.add("spring.flyway.password",db::getPassword);
  r.add("lmt.identity.sandbox-secret",()->"test-only-secret-with-more-than-32-characters");r.add("lmt.identity.inbox",inbox::toString);r.add("lmt.identity.delivery-delay-ms",()->"50");r.add("lmt.workspace.delivery-delay-ms",()->"50");
 }
 @LocalServerPort int port;
 final ObjectMapper json=new ObjectMapper();
 class Browser {
  final CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);
  final HttpClient http=HttpClient.newBuilder().cookieHandler(cookies).build();String csrf;
  HttpResponse<String> send(String method,String path,String body,String key,String token,String origin)throws Exception {
   return send(method,path,body,key,token,origin,null);
  }
  HttpResponse<String> send(String method,String path,String body,String key,String token,String origin,String etag)throws Exception {
   var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1"+path)).header("Content-Type","application/json");
   if(etag!=null)b.header("If-Match",etag);
   if(origin!=null)b.header("Origin",origin);if(key!=null)b.header("Idempotency-Key",key);if(token!=null)b.header("X-CSRF-Token",token);
   return http.send(b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
  }
  HttpResponse<String> get(String path)throws Exception{return send("GET",path,null,null,null,null);}
  HttpResponse<String> post(String path,String body,String key)throws Exception{return send("POST",path,body,key,csrf,"http://localhost:3000");}
  void token()throws Exception {var r=get("/auth/csrf");assertThat(r.statusCode()).isEqualTo(200);csrf=parse(r).get("token").asString();}
  String challenge(String mobile)throws Exception {token();var r=post("/auth/challenges",json.writeValueAsString(Map.of("channel","mobile","destination",mobile,"purpose","sign_in","displayName","Test Owner")),UUID.randomUUID().toString());assertThat(r.statusCode()).as(r.body()).isEqualTo(202);return parse(r).get("id").asString();}
  void login()throws Exception{login("+919"+mobiles.incrementAndGet());}
  void login(String mobile)throws Exception{var id=challenge(mobile);var r=post("/auth/challenges/"+id+"/verify","{\"code\":\""+otp(id)+"\"}",null);assertThat(r.statusCode()).as(r.body()).isEqualTo(200);token();}
 }
 JsonNode parse(HttpResponse<String> r){return json.readTree(r.body());}
 String otp(String id)throws Exception{var file=inbox.resolve(id+".txt");for(int n=0;n<100;n++){if(Files.exists(file)&&Files.size(file)==6)return Files.readString(file);Thread.sleep(50);}throw new AssertionError("Sandbox delivery did not complete");}
 String body(String name,String plan){return json.writeValueAsString(Map.of("profile",Map.of("name",name,"customerType","individual","city","Pune","state","Maharashtra"),"selectedPlanVersionId",plan,"consentIds",List.of("sandbox-use-v1")));}
 void sql(String sql)throws Exception{try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute(sql);}}
 long count(String sql)throws Exception{try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getLong(1);}}

 @Test void availabilityIsScopedAuditedAndNeverFallsBackOrChangesContracts()throws Exception{
  Browser a=new Browser();a.login();Browser b=new Browser();b.login();String aa=parse(a.get("/auth/session")).get("account").get("id").asString(),ba=parse(b.get("/auth/session")).get("account").get("id").asString();String silver="00000000-0000-4000-8000-000000000002",path="/admin/plan-versions/"+silver+"/availability";
  String workspace=parse(a.post("/workspaces",body("Existing contract",free),UUID.randomUUID().toString())).get("id").asString();assertThat(a.get(path).statusCode()).isEqualTo(403);
  for(String actor:List.of(aa,ba))sql("INSERT INTO commercial_review_grant(account_id,valid_until,reason,issued_by,can_create_draft,can_publish) VALUES('"+actor+"',CURRENT_TIMESTAMP+interval '1 hour','Offering availability test','test',true,true)");
  sql("INSERT INTO plan_version(version_id,audience,code,version_number,display_name,publication_state,effective_at,baseline_only,currency,commercial_disclosure) VALUES(gen_random_uuid(),'consumer','SILVER',1,'Consumer isolation fixture','published','2020-01-01',true,'INR','Isolated test offering, not a live membership')");
  var before=parse(a.get(path));String tag=before.get("etag").asString(),withdraw="{\"available\":false,\"reason\":\"Withdraw sandbox offering for review\"}",restore="{\"available\":true,\"reason\":\"Restore reviewed sandbox offering\"}",ka=UUID.randomUUID().toString(),kb=UUID.randomUUID().toString();assertThat(before.get("canChange").asBoolean()).isFalse();assertThat(a.send("POST",path,withdraw,ka,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
  for(String actor:List.of(aa,ba))sql("UPDATE commercial_review_grant SET can_manage_offering=true WHERE account_id='"+actor+"'");
  assertThat(a.post(path,withdraw,ka).statusCode()).isEqualTo(428);assertThat(a.send("POST",path,withdraw,ka,null,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
  sql("CREATE FUNCTION fail_offering_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='PLAN_OFFERING_WITHDRAWN' THEN RAISE EXCEPTION 'Injected offering failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER offering_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_offering_audit()");assertThat(a.send("POST",path,withdraw,ka,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(503);assertThat(parse(a.get(path)).get("available").asBoolean()).isTrue();assertThat(count("SELECT count(*) FROM plan_offering_command WHERE actor_id IN ('"+aa+"','"+ba+"')")).isZero();sql("DROP TRIGGER offering_failure ON domain_audit");
  HttpResponse<String> ra,rb;try(var pool=Executors.newFixedThreadPool(2)){var one=pool.submit(()->a.send("POST",path,withdraw,ka,a.csrf,"http://localhost:3000",tag));var two=pool.submit(()->b.send("POST",path,withdraw,kb,b.csrf,"http://localhost:3000",tag));ra=one.get();rb=two.get();}assertThat(List.of(ra.statusCode(),rb.statusCode())).containsExactlyInAnyOrder(200,412);
  Browser winner=ra.statusCode()==200?a:b;String key=ra.statusCode()==200?ka:kb;assertThat(winner.send("POST",path,withdraw,key,winner.csrf,"http://localhost:3000",tag).body()).isEqualTo(ra.statusCode()==200?ra.body():rb.body());assertThat(winner.send("POST",path,withdraw.replace("review","evaluation"),key,winner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
  assertThat(a.get("/public/plans/SILVER").statusCode()).isEqualTo(404);assertThat(a.get("/public/plans/SILVER?audience=consumer").statusCode()).isEqualTo(200);assertThat(a.get("/public/plans?audience=workspace").body()).doesNotContain("Silver");assertThat(count("SELECT count(*) FROM plan_offering_command WHERE actor_id IN ('"+aa+"','"+ba+"')")).isEqualTo(1);
  assertThat(a.send("POST",path,restore,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(412);
  String proposed=json.writeValueAsString(Map.of("name","New hidden Silver","effectiveAt",java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(10).toString(),"disclosure","Editorial terms remain unavailable until offering restoration","reason","Verify future publication isolation"));String draft=parse(a.send("POST","/admin/plans/"+silver+"/versions",proposed,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000","\"plan-"+silver+"-1\"")).get("id").asString();var review=parse(a.get("/admin/plan-versions/"+draft+"/activation-preview"));String confirmation=json.writeValueAsString(Map.of("reviewId",review.get("id").asString(),"reason","Publish unchanged hidden offering"));assertThat(a.send("POST","/admin/plan-versions/"+draft+"/activate",confirmation,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",review.get("draft").get("etag").asString()).statusCode()).isEqualTo(200);
  for(int n=0;n<70&&count("SELECT count(*) FROM plan_version WHERE version_id='"+draft+"' AND effective_at<=CURRENT_TIMESTAMP")==0;n++)Thread.sleep(200);
  assertThat(a.get("/public/plans/SILVER").statusCode()).isEqualTo(404);assertThat(parse(a.get("/admin/plan-versions/"+draft+"/availability")).get("available").asBoolean()).isFalse();
  var hidden=parse(a.get(path));assertThat(a.send("POST",path,restore,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",hidden.get("etag").asString()).statusCode()).isEqualTo(200);assertThat(parse(a.get("/public/plans/SILVER")).get("versionId").asString()).isEqualTo(draft);
  assertThat(count("SELECT count(*) FROM workspace_subscription WHERE workspace_id='"+workspace+"' AND plan_version_id='"+free+"'")).isEqualTo(1);assertThat(parse(a.get("/workspaces/"+workspace+"/entitlements")).get("capabilities").get(0).get("eligible").asBoolean()).isTrue();
  var freeState=parse(a.get("/admin/plan-versions/"+free+"/availability"));assertThat(a.send("POST","/admin/plan-versions/"+free+"/availability",withdraw,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",freeState.get("etag").asString()).statusCode()).isEqualTo(409);assertThatThrownBy(()->sql("INSERT INTO plan_offering_availability VALUES('workspace','FREE','"+free+"',false,1,CURRENT_TIMESTAMP)")).hasMessageContaining("check constraint");
  sql("UPDATE commercial_review_grant SET revoked=true WHERE account_id='"+(ra.statusCode()==200?aa:ba)+"'");assertThat(winner.send("POST",path,withdraw,key,winner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
 }
 @Test void publicationReviewBindsTheOfferingAvailabilityRevision()throws Exception{
  Browser a=new Browser();a.login();String actor=parse(a.get("/auth/session")).get("account").get("id").asString(),gold="00000000-0000-4000-8000-000000000003";
  sql("INSERT INTO commercial_review_grant(account_id,valid_until,reason,issued_by,can_create_draft,can_publish,can_manage_offering) VALUES('"+actor+"',CURRENT_TIMESTAMP+interval '1 hour','Availability bound review test','test',true,true,true)");
  String body=json.writeValueAsString(Map.of("name","Availability-bound Gold","effectiveAt","2099-01-01T00:00:00Z","disclosure","Isolated publication review test","reason","Verify offering approval binding"));
  String draft=parse(a.send("POST","/admin/plans/"+gold+"/versions",body,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000","\"plan-"+gold+"-1\"")).get("id").asString();String base="/admin/plan-versions/"+draft;var review=parse(a.get(base+"/activation-preview"));assertThat(review.get("offeringAvailable").asBoolean()).isTrue();
  var availability=parse(a.get(base+"/availability"));assertThat(availability.get("code").asString()).isEqualTo("PLAN_NOT_FOUND");
  String path="/admin/plan-versions/"+gold+"/availability";var current=parse(a.get(path));assertThat(a.send("POST",path,"{\"available\":false,\"reason\":\"Withdraw between review and confirmation\"}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",current.get("etag").asString()).statusCode()).isEqualTo(200);
  String confirm=json.writeValueAsString(Map.of("reviewId",review.get("id").asString(),"reason","Confirm originally reviewed terms"));assertThat(a.send("POST",base+"/activate",confirm,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",review.get("draft").get("etag").asString()).statusCode()).isEqualTo(409);
  var refreshed=parse(a.get(base+"/activation-preview"));assertThat(refreshed.get("offeringAvailable").asBoolean()).isFalse();String next=json.writeValueAsString(Map.of("reviewId",refreshed.get("id").asString(),"reason","Confirm withdrawn publication explicitly"));assertThat(a.send("POST",base+"/activate",next,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",refreshed.get("draft").get("etag").asString()).statusCode()).isEqualTo(200);
 }

}
