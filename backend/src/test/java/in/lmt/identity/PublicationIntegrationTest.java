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
class PublicationIntegrationTest {
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

 @Test void reviewedPublicationPreservesContractsAndSerializesCompetingEditors()throws Exception{
  Browser a=new Browser();a.login();Browser b=new Browser();b.login();String aa=parse(a.get("/auth/session")).get("account").get("id").asString(),ba=parse(b.get("/auth/session")).get("account").get("id").asString();
  String w=parse(a.post("/workspaces",body("Before publication",free),UUID.randomUUID().toString())).get("id").asString();
  for(String actor:List.of(aa,ba))sql("INSERT INTO commercial_review_grant(account_id,valid_until,reason,issued_by,can_create_draft) VALUES('"+actor+"',CURRENT_TIMESTAMP+interval '1 hour','Publication test authority','test',true)");
  String effective=java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(20).toString();
  String request=json.writeValueAsString(Map.of("name","Reviewed Free","effectiveAt",effective,"disclosure","Test editorial publication; no price or capacity changes","reason","Review editorial text only"));
  var created=a.send("POST","/admin/plans/"+free+"/versions",request,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000","\"plan-"+free+"-1\"");assertThat(created.statusCode()).as(created.body()).isEqualTo(201);String d=parse(created).get("id").asString();
  String other=parse(b.send("POST","/admin/plans/"+free+"/versions",request.replace(effective,java.time.OffsetDateTime.parse(effective).plusSeconds(1).toString()),UUID.randomUUID().toString(),b.csrf,"http://localhost:3000","\"plan-"+free+"-1\"")).get("id").asString();
  assertThat(a.get("/admin/plan-versions/"+d+"/activation-preview").statusCode()).isEqualTo(403);
  for(String actor:List.of(aa,ba))sql("UPDATE commercial_review_grant SET can_publish=true WHERE account_id='"+actor+"'");
  var p=parse(a.get("/admin/plan-versions/"+d+"/activation-preview"));String oldTag=p.get("draft").get("etag").asString();String oldReview=p.get("id").asString();String base="/admin/plan-versions/"+d+"/activate";
  String confirmation=json.writeValueAsString(Map.of("reviewId",oldReview,"reason","Approved editorial publication"));
  assertThat(a.post(base,confirmation,UUID.randomUUID().toString()).statusCode()).isEqualTo(428);
  assertThat(a.send("POST",base,confirmation,UUID.randomUUID().toString(),null,"http://localhost:3000",oldTag).statusCode()).isEqualTo(403);
  assertThat(b.send("POST",base,confirmation,UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",oldTag).statusCode()).isEqualTo(409);
  sql("UPDATE plan_activation_review SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+oldReview+"'");
  assertThat(a.send("POST",base,confirmation,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",oldTag).statusCode()).isEqualTo(409);
  p=parse(a.get("/admin/plan-versions/"+d+"/activation-preview"));confirmation=json.writeValueAsString(Map.of("reviewId",p.get("id").asString(),"reason","Approved editorial publication"));
  sql("UPDATE plan_version SET display_name='Revised reviewed Free' WHERE version_id='"+d+"'");
  assertThat(a.send("POST",base,confirmation,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",p.get("draft").get("etag").asString()).statusCode()).isEqualTo(412);
  var pa=parse(a.get("/admin/plan-versions/"+d+"/activation-preview"));var pb=parse(b.get("/admin/plan-versions/"+other+"/activation-preview"));
  String ca=json.writeValueAsString(Map.of("reviewId",pa.get("id").asString(),"reason","Approved editorial publication")),cb=json.writeValueAsString(Map.of("reviewId",pb.get("id").asString(),"reason","Approved competing publication"));String ka=UUID.randomUUID().toString(),kb=UUID.randomUUID().toString();
  sql("CREATE FUNCTION fail_publication_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='PLAN_VERSION_PUBLISHED' THEN RAISE EXCEPTION 'Injected audit failure'; END IF; RETURN NEW; END; $$");
  sql("CREATE TRIGGER publication_audit_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_publication_audit()");
  assertThat(a.send("POST",base,ca,ka,a.csrf,"http://localhost:3000",pa.get("draft").get("etag").asString()).statusCode()).isEqualTo(503);
  assertThat(count("SELECT count(*) FROM plan_version WHERE version_id='"+d+"' AND publication_state='draft'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM commercial_plan_capability WHERE plan_version_id='"+d+"'")).isZero();
  assertThat(count("SELECT count(*) FROM plan_activation_command WHERE request_key='"+ka+"'")).isZero();
  sql("DROP TRIGGER publication_audit_failure ON domain_audit");
  HttpResponse<String> ra,rb;
  try(var pool=Executors.newFixedThreadPool(2)){var one=pool.submit(()->a.send("POST",base,ca,ka,a.csrf,"http://localhost:3000",pa.get("draft").get("etag").asString()));var two=pool.submit(()->b.send("POST","/admin/plan-versions/"+other+"/activate",cb,kb,b.csrf,"http://localhost:3000",pb.get("draft").get("etag").asString()));ra=one.get();rb=two.get();}
  assertThat(List.of(ra.statusCode(),rb.statusCode())).containsExactlyInAnyOrder(200,409);
  Browser winner=ra.statusCode()==200?a:b;String id=ra.statusCode()==200?d:other,cmd=ra.statusCode()==200?ca:cb,key=ra.statusCode()==200?ka:kb,tag=ra.statusCode()==200?pa.get("draft").get("etag").asString():pb.get("draft").get("etag").asString();String endpoint="/admin/plan-versions/"+id+"/activate";
  sql("UPDATE plan_activation_review SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+(ra.statusCode()==200?pa:pb).get("id").asString()+"'");
  var replay=winner.send("POST",endpoint,cmd,key,winner.csrf,"http://localhost:3000",tag);assertThat(replay.statusCode()).isEqualTo(200);assertThat(replay.body()).isEqualTo(ra.statusCode()==200?ra.body():rb.body());
  assertThat(winner.send("POST",endpoint,cmd.replace("Approved","Different"),key,winner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
  assertThat(count("SELECT count(*) FROM commercial_plan_capability WHERE plan_version_id='"+id+"'")).isEqualTo(2);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE action='PLAN_VERSION_PUBLISHED' AND resource_id IN('"+d+"','"+other+"')")).isEqualTo(1);
  assertThat(parse(a.get("/public/plans/FREE")).get("versionId").asString()).isEqualTo(free);
  for(int n=0;n<130&&!parse(a.get("/public/plans/FREE")).get("versionId").asString().equals(id);n++)Thread.sleep(200);
  assertThat(parse(a.get("/public/plans/FREE")).get("versionId").asString()).isEqualTo(id);
  var newer=a.post("/workspaces",body("After publication",id),UUID.randomUUID().toString());assertThat(newer.statusCode()).as(newer.body()).isEqualTo(201);
  var eligibility=parse(a.get("/workspaces/"+parse(newer).get("id").asString()+"/entitlements"));assertThat(eligibility.get("capabilities").get(0).get("eligible").asBoolean()).isTrue();
  assertThat(count("SELECT count(*) FROM workspace_subscription WHERE workspace_id='"+w+"' AND plan_version_id='"+free+"'")).isEqualTo(1);
  assertThatThrownBy(()->sql("UPDATE plan_version SET display_name='rewrite' WHERE version_id='"+id+"'")).hasMessageContaining("immutable");
  sql("UPDATE commercial_review_grant SET revoked=true WHERE account_id='"+(ra.statusCode()==200?aa:ba)+"'");assertThat(winner.send("POST",endpoint,cmd,key,winner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
 }
}
