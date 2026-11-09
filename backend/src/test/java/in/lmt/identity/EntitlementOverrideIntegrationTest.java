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
class EntitlementOverrideIntegrationTest {
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
 String path(String w){return "/admin/workspaces/"+w+"/member-entitlement";}
 void grant(Browser a,String w)throws Exception{sql("INSERT INTO entitlement_operator_grant(account_id,workspace_id,valid_until,reason,issued_by) VALUES('"+actor(a)+"','"+w+"',CURRENT_TIMESTAMP+interval '1 hour','Scoped test authority','test')");}
 String input(String effect,String expiry){return json.writeValueAsString(new java.util.HashMap<String,Object>(){{put("effect",effect);put("expiresAt",expiry);put("reason","Review sandbox capability eligibility");}});}
 HttpResponse<String> change(Browser a,String w,String tag,String key,String body)throws Exception{return a.send("POST",path(w),body,key,a.csrf,"http://localhost:3000",tag);}
 JsonNode capability(Browser a,String w,String code)throws Exception{for(var c:parse(a.get("/workspaces/"+w+"/entitlements")).get("capabilities"))if(c.get("code").asString().equals(code))return c;throw new AssertionError("Missing capability");}
 @Test void overrideAffectsRealOnboardingWithoutGrantingAuthorityOrBypassingRestrictions()throws Exception{
  Browser owner=new Browser();owner.login();Browser operator=new Browser();operator.login();String w=workspace(owner),other=workspace(owner);assertThat(owner.get(path(w)).statusCode()).isEqualTo(403);grant(operator,w);assertThat(operator.get(path(other)).statusCode()).isEqualTo(403);assertThat(operator.get("/workspaces/"+w+"/entitlements").statusCode()).isEqualTo(404);
  String mobile="+919"+mobiles.incrementAndGet();Browser recipient=new Browser();recipient.login(mobile);var invite=owner.post("/workspaces/"+w+"/invitations",json.writeValueAsString(Map.of("mobile",mobile)),UUID.randomUUID().toString());assertThat(invite.statusCode()).isEqualTo(201);String iid=parse(invite).get("resourceId").asString();var invitation=parse(recipient.get("/me/invitations")).get("items").get(0);
  String expiry=java.time.Instant.now().plusSeconds(3600).toString();var deny=change(operator,w,parse(operator.get(path(w))).get("etag").asString(),UUID.randomUUID().toString(),input("deny",expiry));assertThat(deny.statusCode()).as(deny.body()).isEqualTo(200);assertThat(capability(owner,w,"WORKSPACE_MEMBER_ADD").get("eligible").asBoolean()).isFalse();assertThat(capability(owner,w,"WORKSPACE_MEMBER_REVOKE").get("eligible").asBoolean()).isTrue();
  var blocked=owner.post("/workspaces/"+w+"/invitations","{\"mobile\":\"+919123456789\"}",UUID.randomUUID().toString());assertThat(parse(blocked).get("code").asString()).isEqualTo("COMMERCIAL_OVERRIDE_DENIED");var accepted=recipient.send("POST","/me/invitations/"+iid+"/accept","{}",UUID.randomUUID().toString(),recipient.csrf,"http://localhost:3000",invitation.get("etag").asString());assertThat(accepted.statusCode()).isEqualTo(409);assertThat(count("SELECT count(*) FROM workspace_member WHERE workspace_id='"+w+"' AND role='MANAGER'")).isZero();
  var allow=change(operator,w,parse(deny).get("etag").asString(),UUID.randomUUID().toString(),input("allow",expiry));assertThat(allow.statusCode()).isEqualTo(200);sql("UPDATE workspace SET status='restricted' WHERE id='"+w+"'");assertThat(capability(owner,w,"WORKSPACE_MEMBER_ADD").get("reason").asString()).isEqualTo("WORKSPACE_RESTRICTED");sql("UPDATE workspace SET status='suspended' WHERE id='"+w+"'");assertThat(capability(owner,w,"WORKSPACE_MEMBER_ADD").get("reason").asString()).isEqualTo("SECURITY_SUSPENDED");sql("UPDATE workspace SET status='active' WHERE id='"+w+"'");assertThat(recipient.send("POST","/me/invitations/"+iid+"/accept","{}",UUID.randomUUID().toString(),recipient.csrf,"http://localhost:3000",invitation.get("etag").asString()).statusCode()).isEqualTo(200);
  String secondMobile="+919"+mobiles.incrementAndGet();Browser second=new Browser();second.login(secondMobile);assertThat(owner.post("/workspaces/"+w+"/invitations",json.writeValueAsString(Map.of("mobile",secondMobile)),UUID.randomUUID().toString()).statusCode()).isEqualTo(201);var secondInvite=parse(second.get("/me/invitations")).get("items").get(0);var overQuota=second.send("POST","/me/invitations/"+secondInvite.get("id").asString()+"/accept","{}",UUID.randomUUID().toString(),second.csrf,"http://localhost:3000",secondInvite.get("etag").asString());assertThat(parse(overQuota).get("code").asString()).isEqualTo("QUOTA_EXCEEDED");
  var deniedAgain=change(operator,w,parse(allow).get("etag").asString(),UUID.randomUUID().toString(),input("deny",expiry));assertThat(deniedAgain.statusCode()).isEqualTo(200);JsonNode manager=null;for(var m:parse(owner.get("/workspaces/"+w+"/members")).get("items"))if(m.get("role").asString().equals("MANAGER"))manager=m;assertThat(manager).isNotNull();assertThat(owner.send("POST","/workspaces/"+w+"/members/"+manager.get("id").asString()+"/revoke","{}",UUID.randomUUID().toString(),owner.csrf,"http://localhost:3000",manager.get("etag").asString()).statusCode()).isEqualTo(200);
  var cleared=change(operator,w,parse(deniedAgain).get("etag").asString(),UUID.randomUUID().toString(),input("inherit",null));assertThat(cleared.statusCode()).isEqualTo(200);assertThat(capability(owner,w,"WORKSPACE_MEMBER_ADD").get("source").asString()).isEqualTo("plan");assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='managers'")).isZero();
 }
 @Test void expiryConcurrencyAndRollbackPreserveReviewAndCommandEvidence()throws Exception{
  Browser a=new Browser();a.login();String w=workspace(a);grant(a,w);String tag=parse(a.get(path(w))).get("etag").asString(),expiry=java.time.Instant.now().plusSeconds(3600).toString(),body=input("deny",expiry),key=UUID.randomUUID().toString();assertThat(change(a,w,null,key,body).statusCode()).isEqualTo(428);assertThat(a.send("POST",path(w),body,key,null,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
  sql("CREATE FUNCTION fail_override_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='ENTITLEMENT_OVERRIDE_CHANGED' THEN RAISE EXCEPTION 'Injected failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER override_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_override_audit()");assertThat(change(a,w,tag,key,body).statusCode()).isEqualTo(503);assertThat(parse(a.get(path(w))).get("revision").asInt()).isZero();sql("DROP TRIGGER override_failure ON domain_audit");
  HttpResponse<String> one,two;try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->change(a,w,tag,key,body));var y=pool.submit(()->change(a,w,tag,UUID.randomUUID().toString(),body));one=x.get();two=y.get();}assertThat(List.of(one.statusCode(),two.statusCode())).containsExactlyInAnyOrder(200,412);var state=parse(a.get(path(w)));String activeTag=state.get("etag").asString();
  String stableKey=UUID.randomUUID().toString();var same=change(a,w,activeTag,stableKey,input("allow",expiry));assertThat(change(a,w,activeTag,stableKey,input("allow",expiry)).body()).isEqualTo(same.body());assertThat(change(a,w,activeTag,stableKey,input("deny",expiry)).statusCode()).isEqualTo(409);sql("UPDATE workspace_capability_override SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE workspace_id='"+w+"'");assertThat(capability(a,w,"WORKSPACE_MEMBER_ADD").get("source").asString()).isEqualTo("plan");assertThat(parse(a.get(path(w))).get("active").asBoolean()).isFalse();assertThat(change(a,w,parse(same).get("etag").asString(),UUID.randomUUID().toString(),input("inherit",null)).statusCode()).isEqualTo(412);
  sql("UPDATE entitlement_operator_grant SET revoked=true WHERE account_id='"+actor(a)+"' AND workspace_id='"+w+"'");assertThat(change(a,w,activeTag,stableKey,input("allow",expiry)).statusCode()).isEqualTo(403);assertThatThrownBy(()->sql("UPDATE workspace_capability_override SET capability='WORKSPACE_MEMBER_REVOKE' WHERE workspace_id='"+w+"'")).hasMessageContaining("check constraint");
 }
}
