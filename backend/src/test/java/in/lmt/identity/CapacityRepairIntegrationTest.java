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
class CapacityRepairIntegrationTest {
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


 String workspace(Browser a)throws Exception{return parse(a.post("/workspaces",body("Override test workspace",free),UUID.randomUUID().toString())).get("id").asString();}
 String actor(Browser a)throws Exception{return parse(a.get("/auth/session")).get("account").get("id").asString();}
 String path(String w){return "/admin/workspaces/"+w+"/manager-capacity-repair";}
 void grant(Browser a,String w)throws Exception{sql("INSERT INTO capacity_repair_operator_grant(account_id,workspace_id,valid_until,reason,issued_by) VALUES('"+actor(a)+"','"+w+"',CURRENT_TIMESTAMP+interval '1 hour','Scoped test authority','test')");}

 final String input="{\"reason\":\"Investigated sandbox counter drift\"}";
 HttpResponse<String> repair(Browser a,String w,String tag,String key)throws Exception{return a.send("POST",path(w),input,key,a.csrf,"http://localhost:3000",tag);}
 String tag(Browser a,String w)throws Exception{var r=a.get(path(w));assertThat(r.statusCode()).as(r.body()).isEqualTo(200);return parse(r).get("etag").asString();}
 @Test void scopedRepairPreservesEvidenceAndReplaysOnlyWithCurrentAuthority()throws Exception{
  Browser a=new Browser(),other=new Browser();a.login();other.login();String w=workspace(a),otherW=workspace(a);
  assertThat(a.get(path(w)).statusCode()).isEqualTo(403);grant(other,w);assertThat(other.get(path(w)).statusCode()).isEqualTo(404);grant(a,w);assertThat(a.get(path(otherW)).statusCode()).isEqualTo(403);
  sql("UPDATE workspace_grant SET reserved=1 WHERE workspace_id='"+w+"' AND resource='managers'");
  String t=tag(a,w),k=UUID.randomUUID().toString();assertThat(repair(a,w,null,k).statusCode()).isEqualTo(428);
  var r=repair(a,w,t,k);assertThat(r.statusCode()).as(r.body()).isEqualTo(200);assertThat(parse(r).get("reserved").asInt()).isZero();assertThat(parse(r).get("revision").asInt()).isEqualTo(1);
  assertThat(repair(a,w,t,k).body()).isEqualTo(r.body());assertThat(repair(a,w,t,UUID.randomUUID().toString()).statusCode()).isEqualTo(412);
  assertThat(a.send("POST",path(w),"{\"reason\":\"Different repair reason\"}",k,a.csrf,"http://localhost:3000",t).statusCode()).isEqualTo(409);
  assertThat(count("SELECT count(*) FROM capacity_repair_command WHERE workspace_id='"+w+"'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+w+"' AND action='MANAGER_CAPACITY_REPAIRED'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM manager_usage_event WHERE workspace_id='"+w+"'")).isZero();
  sql("UPDATE capacity_repair_operator_grant SET revoked=true WHERE workspace_id='"+w+"' AND account_id='"+actor(a)+"'");assertThat(repair(a,w,t,k).statusCode()).isEqualTo(403);
 }
 @Test void concurrentRepairsAndAuditFailureCannotLoseEvidence()throws Exception{
  Browser a=new Browser();a.login();String w=workspace(a);grant(a,w);sql("UPDATE workspace_grant SET used=1 WHERE workspace_id='"+w+"' AND resource='managers'");String t=tag(a,w);
  sql("CREATE FUNCTION reject_capacity_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='MANAGER_CAPACITY_REPAIRED' THEN RAISE EXCEPTION 'isolated audit failure'; END IF; RETURN NEW; END $$; CREATE TRIGGER reject_capacity_audit BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION reject_capacity_audit()");
  try{assertThat(repair(a,w,t,UUID.randomUUID().toString()).statusCode()).isGreaterThanOrEqualTo(500);assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='managers'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM capacity_repair_command WHERE workspace_id='"+w+"'")).isZero();}finally{sql("DROP TRIGGER reject_capacity_audit ON domain_audit; DROP FUNCTION reject_capacity_audit()");}
  try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->repair(a,w,t,UUID.randomUUID().toString()).statusCode());var y=pool.submit(()->repair(a,w,t,UUID.randomUUID().toString()).statusCode());assertThat(List.of(x.get(),y.get())).containsExactlyInAnyOrder(200,412);}
 }
 @Test void mismatchedSourceMembershipCannotBeRepaired()throws Exception{
  Browser a=new Browser(),m=new Browser();a.login();m.login();String w=workspace(a);grant(a,w);
  sql("INSERT INTO workspace_member(workspace_id,account_id,role,active) VALUES('"+w+"','"+actor(m)+"','MANAGER',true)");
  var v=parse(a.get(path(w)));assertThat(v.get("repairable").asBoolean()).isFalse();assertThat(v.get("reason").asString()).isEqualTo("SOURCE_EVIDENCE_INCONSISTENT");
  var r=repair(a,w,v.get("etag").asString(),UUID.randomUUID().toString());assertThat(r.statusCode()).isEqualTo(409);assertThat(count("SELECT count(*) FROM capacity_repair_command WHERE workspace_id='"+w+"'")).isZero();
 }

 @Test void ownerRepairHistoryIsScopedPaginatedAndRetainedAfterGrantRevocation()throws Exception{
  Browser a=new Browser(),coOwner=new Browser(),outsider=new Browser();a.login();coOwner.login();outsider.login();String w=workspace(a),otherW=workspace(a);grant(a,w);
  String h="/workspaces/"+w+"/usage/repairs";
  assertThat(new Browser().get(h).statusCode()).isEqualTo(401);assertThat(outsider.get(h).statusCode()).isEqualTo(404);
  assertThat(parse(a.get(h)).get("totalMatching").asInt()).isZero();
  for(int n=0;n<27;n++){
   sql("UPDATE workspace_grant SET reserved=1 WHERE workspace_id='"+w+"' AND resource='managers'");
   var r=repair(a,w,tag(a,w),UUID.randomUUID().toString());assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
  }
  sql("INSERT INTO workspace_member(workspace_id,account_id,role,active) VALUES('"+w+"','"+actor(coOwner)+"','OWNER',true)");
  sql("UPDATE capacity_repair_operator_grant SET revoked=true WHERE workspace_id='"+w+"'");
  var first=a.get(h);assertThat(first.statusCode()).isEqualTo(200);assertThat(first.headers().firstValue("cache-control")).contains("no-store");var page=parse(first);
  assertThat(page.get("totalMatching").asInt()).isEqualTo(27);assertThat(page.get("items").size()).isEqualTo(25);assertThat(page.get("items").get(0).get("revision").asInt()).isEqualTo(27);
  assertThat(first.body()).doesNotContain("account_id","request_hash","request_key","before_state",actor(a));
  String cursor=URLEncoder.encode(page.get("page").get("nextCursor").asString(),java.nio.charset.StandardCharsets.UTF_8);
  var older=parse(a.get(h+"?cursor="+cursor));assertThat(older.get("items").size()).isEqualTo(2);assertThat(older.get("items").get(0).get("revision").asInt()).isEqualTo(2);assertThat(older.get("page").get("nextCursor").isNull()).isTrue();
  assertThat(parse(coOwner.get(h)).get("totalMatching").asInt()).isEqualTo(27);assertThat(coOwner.get(h+"?cursor="+cursor).statusCode()).isEqualTo(422);
  assertThat(a.get("/workspaces/"+otherW+"/usage/repairs?cursor="+cursor).statusCode()).isEqualTo(422);
  assertThat(a.get(h+"?from=2020-01-01&to=2020-01-02&cursor="+cursor).statusCode()).isEqualTo(422);
  assertThat(a.get(h+"?cursor=invalid").statusCode()).isEqualTo(422);assertThat(a.get(h+"?from=2020-01-02&to=2020-01-01").statusCode()).isEqualTo(422);
  // Isolated historical time fixture exercises inclusive India date boundaries.
  sql("UPDATE capacity_repair_command SET created_at='2020-01-01T18:29:59Z' WHERE workspace_id='"+w+"' AND (result->>'revision')::integer=1");
  sql("UPDATE capacity_repair_command SET created_at='2020-01-01T18:30:00Z' WHERE workspace_id='"+w+"' AND (result->>'revision')::integer=2");
  var day=parse(a.get(h+"?from=2020-01-02&to=2020-01-02"));assertThat(day.get("totalMatching").asInt()).isEqualTo(1);assertThat(day.get("items").get(0).get("revision").asInt()).isEqualTo(2);
  try(var connection=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var statement=connection.createStatement()){
   statement.execute("SELECT set_config('lmt.account_id','"+actor(coOwner)+"',false)");
   try(var rows=statement.executeQuery("SELECT count(*) FROM capacity_repair_command")){rows.next();assertThat(rows.getInt(1)).isEqualTo(27);}
   assertThatThrownBy(()->statement.execute("UPDATE capacity_repair_command SET reason='rewrite evidence' WHERE workspace_id='"+w+"'")).isInstanceOf(SQLException.class);
  }
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"' AND account_id='"+actor(coOwner)+"'");assertThat(coOwner.get(h).statusCode()).isEqualTo(404);
 }
}
