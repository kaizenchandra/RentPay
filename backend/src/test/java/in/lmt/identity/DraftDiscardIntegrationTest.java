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
class DraftDiscardIntegrationTest {
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

 @Test void discardPreservesEvidenceAndCannotRaceIntoPublication()throws Exception{
  Browser a=new Browser();a.login();Browser b=new Browser();b.login();String aa=parse(a.get("/auth/session")).get("account").get("id").asString(),ba=parse(b.get("/auth/session")).get("account").get("id").asString();
  for(String actor:List.of(aa,ba))sql("INSERT INTO commercial_review_grant(account_id,valid_until,reason,issued_by,can_create_draft,can_publish,can_edit_draft) VALUES('"+actor+"',CURRENT_TIMESTAMP+interval '1 hour','Draft discard integration test','test',true,true,true)");
  String create=json.writeValueAsString(Map.of("name","Retained discarded draft","effectiveAt","2098-01-01T00:00:00Z","disclosure","Sandbox discarded editorial evidence","reason","Prepare discard verification"));
  String id=parse(a.send("POST","/admin/plans/"+free+"/versions",create,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000","\"plan-"+free+"-1\"")).get("id").asString(),base="/admin/plan-versions/"+id,tag="\"plan-"+id+"-1\"",key=UUID.randomUUID().toString();String request="{\"reason\":\"Retire the unused editorial proposal\"}";
  var review=parse(b.get(base+"/activation-preview"));
  assertThat(a.send("POST",base+"/discard",request,key,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
  sql("UPDATE commercial_review_grant SET can_discard_draft=true WHERE account_id='"+aa+"'");
  assertThat(a.post(base+"/discard",request,key).statusCode()).isEqualTo(428);assertThat(a.send("POST",base+"/discard",request,key,null,"http://localhost:3000",tag).statusCode()).isEqualTo(403);assertThat(a.send("POST",base+"/discard",request,key,a.csrf,"http://localhost:3000","\"stale\"").statusCode()).isEqualTo(412);
  sql("CREATE FUNCTION fail_discard_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='PLAN_DRAFT_DISCARDED' THEN RAISE EXCEPTION 'Injected discard audit failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER discard_audit_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_discard_audit()");
  assertThat(a.send("POST",base+"/discard",request,key,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(503);assertThat(count("SELECT count(*) FROM plan_version WHERE version_id='"+id+"' AND publication_state='draft' AND row_revision=1")).isEqualTo(1);assertThat(count("SELECT count(*) FROM plan_draft_discard_command WHERE draft_id='"+id+"'")).isZero();sql("DROP TRIGGER discard_audit_failure ON domain_audit");
  try(var pool=Executors.newFixedThreadPool(2)){var one=pool.submit(()->a.send("POST",base+"/discard",request,key,a.csrf,"http://localhost:3000",tag));var two=pool.submit(()->a.send("POST",base+"/discard",request,key,a.csrf,"http://localhost:3000",tag));var x=one.get();var y=two.get();assertThat(x.statusCode()).as(x.body()).isEqualTo(200);assertThat(y.statusCode()).isEqualTo(200);assertThat(x.body()).isEqualTo(y.body());}
  assertThat(a.send("POST",base+"/discard",request.replace("unused","superseded"),key,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
  assertThat(count("SELECT count(*) FROM plan_draft_discard_command WHERE draft_id='"+id+"' AND before_terms->>'name'='Retained discarded draft'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+id+"' AND action='PLAN_DRAFT_DISCARDED'")).isEqualTo(1);
  assertThat(a.send("PATCH",base,create,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
  String approval=json.writeValueAsString(Map.of("reviewId",review.get("id").asString(),"reason","Attempt reviewed publication"));assertThat(b.send("POST",base+"/activate",approval,UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);assertThat(b.get(base+"/activation-preview").statusCode()).isEqualTo(409);
  assertThat(a.get("/public/plans").body()).doesNotContain("Retained discarded draft");assertThat(a.get("/admin/plans").body()).contains("Retained discarded draft","discarded");
  assertThatThrownBy(()->sql("UPDATE plan_version SET publication_state='draft' WHERE version_id='"+id+"'")).hasMessageContaining("immutable");assertThatThrownBy(()->sql("DELETE FROM plan_version WHERE version_id='"+id+"'")).hasMessageContaining("immutable");
  String racing=parse(a.send("POST","/admin/plans/"+free+"/versions",create.replace("2098-01-01","2098-01-02"),UUID.randomUUID().toString(),a.csrf,"http://localhost:3000","\"plan-"+free+"-1\"")).get("id").asString();String raceBase="/admin/plan-versions/"+racing;var raceReview=parse(b.get(raceBase+"/activation-preview"));String raceTag=raceReview.get("draft").get("etag").asString(),raceApproval=json.writeValueAsString(Map.of("reviewId",raceReview.get("id").asString(),"reason","Competing publication review"));
  try(var pool=Executors.newFixedThreadPool(2)){var one=pool.submit(()->a.send("POST",raceBase+"/discard",request,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",raceTag));var two=pool.submit(()->b.send("POST",raceBase+"/activate",raceApproval,UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",raceTag));assertThat(List.of(one.get().statusCode(),two.get().statusCode())).containsExactlyInAnyOrder(200,409);}
  assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+racing+"' AND action IN('PLAN_DRAFT_DISCARDED','PLAN_VERSION_PUBLISHED')")).isEqualTo(1);
  sql("UPDATE commercial_review_grant SET revoked=true WHERE account_id='"+aa+"'");assertThat(a.send("POST",base+"/discard",request,key,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
 }
}
