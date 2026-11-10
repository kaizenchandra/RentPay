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
class FoundationIntegrationTest {
 static final PostgreSQLContainer db=new PostgreSQLContainer("postgres:17.11");
 static final Path inbox;
 static final AtomicInteger mobiles=new AtomicInteger(100000000);
 static final String free="00000000-0000-4000-8000-000000000006";
 static {try{inbox=Files.createTempDirectory("lmt-test-inbox-");db.start();try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");}}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 @DynamicPropertySource static void properties(DynamicPropertyRegistry r){
  r.add("lmt.workspace.expiry-initial-delay-ms",()->"3600000");
  r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",()->"lmt_runtime");r.add("spring.datasource.password",()->"runtime_test");r.add("spring.flyway.user",db::getUsername);r.add("spring.flyway.password",db::getPassword);
  r.add("lmt.identity.sandbox-secret",()->"test-only-secret-with-more-than-32-characters");r.add("lmt.identity.inbox",inbox::toString);r.add("lmt.identity.delivery-delay-ms",()->"50");r.add("lmt.workspace.delivery-delay-ms",()->"50");
 }
 @org.springframework.beans.factory.annotation.Autowired in.lmt.workspace.InvitationExpiry expiry;
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
 @Test void commercialReviewRequiresExplicitLiveGrantAndAuditsReads()throws Exception{
  Browser b=new Browser();assertThat(b.get("/admin/plans").statusCode()).isEqualTo(401);b.login();
  b.post("/workspaces",body("Review owner",free),UUID.randomUUID().toString());
  String actor=parse(b.get("/auth/session")).get("account").get("id").asString();
  assertThat(b.get("/admin/plans").statusCode()).isEqualTo(403);
  sql("INSERT INTO commercial_review_grant VALUES('"+actor+"',CURRENT_TIMESTAMP+interval '1 hour',false,'Isolated review test','test',CURRENT_TIMESTAMP)");
  var response=b.get("/admin/plans");assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
  assertThat(response.headers().firstValue("cache-control")).contains("no-store");
  assertThat(parse(response).get("items").size()).isEqualTo(6);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='COMMERCIAL_WORKSPACE_VERSIONS_REVIEWED'")).isEqualTo(1);
  assertThat(parse(b.get("/admin/plans?audience=consumer")).get("items").size()).isZero();
  assertThat(b.get("/admin/plans?audience=invalid").statusCode()).isEqualTo(422);
  assertThat(b.get("/admin/plans?cursor=invalid").statusCode()).isEqualTo(400);
  assertThat(b.post("/admin/plans","{}",UUID.randomUUID().toString()).statusCode()).isEqualTo(403);
  sql("UPDATE commercial_review_grant SET revoked=true WHERE account_id='"+actor+"'");
  assertThat(b.get("/admin/plans").statusCode()).isEqualTo(403);
  sql("UPDATE commercial_review_grant SET revoked=false,valid_until=CURRENT_TIMESTAMP-interval '1 second' WHERE account_id='"+actor+"'");
  assertThat(b.get("/admin/plans").statusCode()).isEqualTo(403);
  assertThat(count("SELECT count(*) FROM commercial_review_grant_audit WHERE account_id='"+actor+"'")).isEqualTo(3);
  try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var q=c.createStatement()){
   assertThatThrownBy(()->q.execute("UPDATE commercial_review_grant SET revoked=false")).isInstanceOf(SQLException.class).hasMessageContaining("permission denied");
   assertThatThrownBy(()->q.execute("DELETE FROM commercial_review_grant_audit")).isInstanceOf(SQLException.class).hasMessageContaining("permission denied");
  }
 }
 @Test void commercialReviewPaginationIncludesDraftsWithoutPublishingThem()throws Exception{
  Browser b=new Browser();b.login();String actor=parse(b.get("/auth/session")).get("account").get("id").asString();
  sql("INSERT INTO commercial_review_grant VALUES('"+actor+"',CURRENT_TIMESTAMP+interval '1 hour',false,'Isolated pagination test','test',CURRENT_TIMESTAMP)");
  // Use consumer drafts, clean them afterwards so unrelated foundation tests see the baseline.
  try{
   for(int i=1;i<=27;i++)sql("INSERT INTO plan_version SELECT gen_random_uuid(),'consumer','REVIEW_TEST',"+i+",'Private test draft','draft',CURRENT_TIMESTAMP+interval '"+i+" day',true,NULL,NULL,NULL,NULL,currency,commercial_disclosure FROM plan_version WHERE version_id='"+free+"'");
   var first=parse(b.get("/admin/plans?audience=consumer"));assertThat(first.get("items").size()).isEqualTo(25);assertThat(first.get("totalMatching").asInt()).isEqualTo(27);
   var second=parse(b.get("/admin/plans?audience=consumer&cursor="+first.get("page").get("nextCursor").asString()));assertThat(second.get("items").size()).isEqualTo(2);assertThat(second.get("page").get("nextCursor").isNull()).isTrue();
   assertThat(b.get("/public/plans?audience=consumer").body()).doesNotContain("Private test draft");
   sql("UPDATE commercial_review_grant SET revoked=true WHERE account_id='"+actor+"'");assertThat(b.get("/admin/plans?audience=consumer&cursor="+first.get("page").get("nextCursor").asString()).statusCode()).isEqualTo(403);
  }finally{sql("DELETE FROM plan_version WHERE audience='consumer' AND code='REVIEW_TEST'");}
 }
 @Test void commercialDraftCreationIsAuthorizedIdempotentAndUnpublished()throws Exception{
  Browser b=new Browser();b.login();String actor=parse(b.get("/auth/session")).get("account").get("id").asString();
  Browser other=new Browser();other.login();String otherActor=parse(other.get("/auth/session")).get("account").get("id").asString();
  sql("INSERT INTO commercial_review_grant(account_id,valid_until,reason,issued_by,can_create_draft) VALUES('"+otherActor+"',CURRENT_TIMESTAMP+interval '1 hour','Concurrent editor test','test',true)");
  String path="/admin/plans/"+free+"/versions",tag="\"plan-"+free+"-1\"";
  String payload=json.writeValueAsString(Map.of("name","Proposed Free","effectiveAt","2090-01-01T00:00:00Z","disclosure","Test draft only; no new public terms","reason","Review proposed wording"));
  String key=UUID.randomUUID().toString();
  sql("INSERT INTO commercial_review_grant(account_id,valid_until,reason,issued_by) VALUES('"+actor+"',CURRENT_TIMESTAMP+interval '1 hour','Draft permission test','test')");
  assertThat(b.send("POST",path,payload,key,b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
  sql("UPDATE commercial_review_grant SET can_create_draft=true WHERE account_id='"+actor+"'");
  try{
   assertThat(b.post(path,payload,key).statusCode()).isEqualTo(428);
   assertThat(b.send("POST",path,payload,key,b.csrf,"http://localhost:3000","\"wrong\"").statusCode()).isEqualTo(412);
   assertThat(b.send("POST",path,payload,key,null,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
   try(var pool=Executors.newFixedThreadPool(2)){
    var one=pool.submit(()->b.send("POST",path,payload,key,b.csrf,"http://localhost:3000",tag));var two=pool.submit(()->b.send("POST",path,payload,key,b.csrf,"http://localhost:3000",tag));
    var first=one.get();var second=two.get();assertThat(first.statusCode()).as(first.body()).isEqualTo(201);assertThat(second.statusCode()).as(second.body()).isEqualTo(201);assertThat(first.body()).isEqualTo(second.body());
   }
   assertThat(b.send("POST",path,payload.replace("Proposed Free","Different proposal"),key,b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
   assertThat(b.send("POST",path,payload,UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
   assertThat(b.send("POST",path,payload.replace("2090-01-01","2020-01-01"),UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(422);
   try(var pool=Executors.newFixedThreadPool(2)){
    var one=pool.submit(()->b.send("POST",path,payload.replace("2090-01-01","2090-01-02"),UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag));var two=pool.submit(()->other.send("POST",path,payload.replace("2090-01-01","2090-01-03"),UUID.randomUUID().toString(),other.csrf,"http://localhost:3000",tag));
    assertThat(one.get().statusCode()).isEqualTo(201);assertThat(two.get().statusCode()).isEqualTo(201);
   }
   assertThat(count("SELECT count(DISTINCT p.version_number) FROM plan_draft_command c JOIN plan_version p ON p.version_id=c.draft_id WHERE c.actor_id IN('"+actor+"','"+otherActor+"') AND p.publication_state='draft' AND p.monthly_amount=0 AND p.manager_limit=1")).isEqualTo(3);
   assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id IN('"+actor+"','"+otherActor+"') AND action='PLAN_DRAFT_CREATED'")).isEqualTo(3);
   assertThat(b.get("/public/plans").body()).doesNotContain("Proposed Free");assertThat(parse(b.get("/public/plans/FREE")).get("versionId").asString()).isEqualTo(free);
   sql("UPDATE commercial_review_grant SET revoked=true WHERE account_id='"+actor+"'");assertThat(b.send("POST",path,payload,key,b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
  }finally{sql("WITH removed AS (DELETE FROM plan_draft_command WHERE actor_id IN('"+actor+"','"+otherActor+"') RETURNING draft_id) DELETE FROM plan_version WHERE version_id IN(SELECT draft_id FROM removed)");}
 }
 @Test void csrfOriginRotationLogoutAndReplay()throws Exception{
  Browser b=new Browser();b.token();String before=b.cookies.getCookieStore().getCookies().getFirst().getValue();
  var bad=b.send("POST","/auth/challenges","{}",UUID.randomUUID().toString(),null,"http://localhost:3000");assertThat(bad.statusCode()).isEqualTo(403);
  assertThat(b.send("POST","/auth/challenges","{}",UUID.randomUUID().toString(),b.csrf,"https://evil.example").statusCode()).isEqualTo(403);
  assertThat(b.send("POST","/auth/challenges","{}",UUID.randomUUID().toString(),b.csrf,null).statusCode()).isEqualTo(403);
  String id=b.challenge("+919"+mobiles.incrementAndGet());String code=otp(id);String oldToken=b.csrf;
  var verified=b.post("/auth/challenges/"+id+"/verify","{\"code\":\""+code+"\"}",null);assertThat(verified.statusCode()).as(verified.body()).isEqualTo(200);
  assertThat(b.cookies.getCookieStore().getCookies().getFirst().getValue()).isNotEqualTo(before);
  assertThat(b.post("/workspaces",body("Old token",free),UUID.randomUUID().toString()).statusCode()).isEqualTo(403);
  b.token();assertThat(b.csrf).isNotEqualTo(oldToken);
  assertThat(b.post("/auth/challenges/"+id+"/verify","{\"code\":\""+code+"\"}",null).statusCode()).isIn(404,409);
  assertThat(b.get("/me/profile").statusCode()).isEqualTo(200);
  assertThat(b.post("/auth/logout","{}",null).statusCode()).isEqualTo(204);
  assertThat(b.get("/me/profile").statusCode()).isEqualTo(401);
  assertThat(parse(b.get("/auth/session")).get("authenticated").asBoolean()).isFalse();
 }
 @Test void provisioningConcurrencyIsolationRevocationAndRls()throws Exception{
  Browser a=new Browser(),b=new Browser();a.login();b.login();String key=UUID.randomUUID().toString();String request=body("Concurrent workspace",free);
  try(var pool=Executors.newFixedThreadPool(2)){
   var one=pool.submit(()->a.post("/workspaces",request,key));var two=pool.submit(()->a.post("/workspaces",request,key));
   var r1=one.get();var r2=two.get();assertThat(r1.statusCode()).as(r1.body()).isEqualTo(201);assertThat(r2.statusCode()).as(r2.body()).isEqualTo(201);
   String id=parse(r1).get("id").asString();assertThat(parse(r2).get("id").asString()).isEqualTo(id);
   assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+id+"' AND action='workspace.free_provisioned'")).isEqualTo(1);
   assertThat(count("SELECT count(*) FROM workspace_subscription WHERE workspace_id='"+id+"'")).isEqualTo(1);
   assertThat(count("SELECT count(*) FROM workspace_grant WHERE workspace_id='"+id+"'")).isEqualTo(2);
   assertThat(a.post("/workspaces",body("Changed",free),key).statusCode()).isEqualTo(409);
   for(int n=0;n<8;n++){assertThat(a.get("/workspaces/"+id+"/setup").statusCode()).isEqualTo(200);assertThat(b.get("/workspaces/"+id+"/setup").statusCode()).isEqualTo(404);}
   assertThat(parse(b.get("/me/contexts")).get("items").size()).isZero();
   try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var s=c.createStatement();var r=s.executeQuery("SELECT count(*) FROM workspace")){r.next();assertThat(r.getInt(1)).isZero();}
   sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+id+"'");assertThat(a.get("/workspaces/"+id+"/setup").statusCode()).isEqualTo(404);
   assertThat(a.post("/workspaces",request,key).statusCode()).isEqualTo(404);
  }
 }
 @Test void invalidPlanRollsBackWholeProvisioning()throws Exception{
  Browser b=new Browser();b.login();var before=parse(b.get("/me/contexts")).get("items").size();
  var r=b.post("/workspaces",body("Must roll back",UUID.randomUUID().toString()),UUID.randomUUID().toString());assertThat(r.statusCode()).as(r.body()).isEqualTo(409);
  assertThat(parse(b.get("/me/contexts")).get("items").size()).isEqualTo(before);
  assertThat(count("SELECT count(*) FROM workspace WHERE name='Must roll back'")).isZero();
 }
 @Test void wrongAttemptsExpiryAndChallengeBinding()throws Exception{
  Browser a=new Browser(),b=new Browser();String id=a.challenge("+919"+mobiles.incrementAndGet());b.token();
  assertThat(b.get("/auth/challenges/"+id).statusCode()).isEqualTo(404);
  String wrong=otp(id).equals("000000")?"000001":"000000";
  for(int n=0;n<5;n++)assertThat(a.post("/auth/challenges/"+id+"/verify","{\"code\":\""+wrong+"\"}",null).statusCode()).isEqualTo(422);
  assertThat(a.post("/auth/challenges/"+id+"/verify","{\"code\":\""+otp(id)+"\"}",null).statusCode()).isEqualTo(409);
  assertThat(count("SELECT attempts FROM auth_challenge WHERE id='"+id+"'")).isEqualTo(5);
  String expired=a.challenge("+919"+mobiles.incrementAndGet());sql("UPDATE auth_challenge SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+expired+"'");
  assertThat(a.post("/auth/challenges/"+expired+"/verify","{\"code\":\"123456\"}",null).statusCode()).isEqualTo(409);
 }
 @Test void loginRecoveryUsesSameAccountAndServerRevocationTakesEffect()throws Exception{
  String mobile="+919"+mobiles.incrementAndGet();Browser a=new Browser(),b=new Browser();a.login(mobile);b.login(mobile);
  assertThat(parse(a.get("/me/profile")).get("id").asString()).isEqualTo(parse(b.get("/me/profile")).get("id").asString());
  String account=parse(a.get("/me/profile")).get("id").asString();sql("DELETE FROM spring_session WHERE principal_name='"+account+"'");
  assertThat(a.get("/me/profile").statusCode()).isEqualTo(401);assertThat(b.get("/me/profile").statusCode()).isEqualTo(401);
 }
 @Test void challengeRetryRateLimitsAndUnknownFields()throws Exception{
  Browser b=new Browser();b.token();String mobile="+919"+mobiles.incrementAndGet();String key=UUID.randomUUID().toString();
  String body=json.writeValueAsString(Map.of("channel","mobile","destination",mobile,"purpose","sign_in","displayName","Rate Test"));
  var first=b.post("/auth/challenges",body,key);var replay=b.post("/auth/challenges",body,key);
  assertThat(first.statusCode()).isEqualTo(202);assertThat(parse(first).get("id").asString()).isEqualTo(parse(replay).get("id").asString());
  assertThat(b.post("/auth/challenges",body.replace("Rate Test","Changed"),key).statusCode()).isEqualTo(409);
  for(int n=0;n<4;n++)assertThat(b.post("/auth/challenges",body,UUID.randomUUID().toString()).statusCode()).isEqualTo(202);
  assertThat(b.post("/auth/challenges",body,UUID.randomUUID().toString()).statusCode()).isEqualTo(429);
  assertThat(b.post("/auth/challenges",body.substring(0,body.length()-1)+",\"role\":\"ADMIN\"}",UUID.randomUUID().toString()).statusCode()).isEqualTo(400);
 }
 @Test void welcomeOutboxIsIdempotentAndSessionExpiryIsEnforced()throws Exception{
  Browser b=new Browser();b.login();var r=b.post("/workspaces",body("Welcome delivery",free),UUID.randomUUID().toString());assertThat(r.statusCode()).isEqualTo(201);String id=parse(r).get("id").asString();
  for(int n=0;n<100 && count("SELECT count(*) FROM workspace_notification WHERE workspace_id='"+id+"'")==0;n++)Thread.sleep(50);
  assertThat(count("SELECT count(*) FROM workspace_notification WHERE workspace_id='"+id+"'")).isEqualTo(1);
  sql("UPDATE workspace_outbox SET status='pending' WHERE scope_id='"+id+"'");Thread.sleep(150);
  assertThat(count("SELECT count(*) FROM workspace_notification WHERE workspace_id='"+id+"'")).isEqualTo(1);
  assertThat(parse(b.get("/workspaces/"+id+"/setup")).get("notifications").size()).isEqualTo(1);
  // Simulate database session expiry; Spring Session must reject the cookie, not silently extend it.
  String account=parse(b.get("/me/profile")).get("id").asString();sql("UPDATE spring_session SET expiry_time=0,last_access_time=0,max_inactive_interval=1 WHERE principal_name='"+account+"'");
  assertThat(b.get("/me/profile").statusCode()).isEqualTo(401);
 }


 @Test void profileVersionConcurrencyReplayAndIsolation()throws Exception {
  Browser a=new Browser(),b=new Browser();a.login();b.login();
  var initial=a.get("/me/profile");String tag=initial.headers().firstValue("ETag").orElseThrow();
  String actor=parse(initial).get("id").asString(),key=UUID.randomUUID().toString();
  String body="{\"displayName\":\"Updated Owner\"}";
  assertThat(a.send("PATCH","/me/profile",body,key,a.csrf,"http://localhost:3000").statusCode()).isEqualTo(428);
  var updated=a.send("PATCH","/me/profile",body,key,a.csrf,"http://localhost:3000",tag);
  assertThat(updated.statusCode()).as(updated.body()).isEqualTo(200);
  assertThat(parse(updated).get("version").asInt()).isEqualTo(2);
  assertThat(parse(a.send("PATCH","/me/profile",body,key,a.csrf,"http://localhost:3000",tag))).isEqualTo(parse(updated));
  assertThat(a.send("PATCH","/me/profile",body.replace("Updated","Changed"),key,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
  assertThat(a.send("PATCH","/me/profile",body,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(412);
  assertThat(parse(b.get("/me/profile")).get("displayName").asString()).isEqualTo("Test Owner");
  assertThat(a.send("PATCH","/me/profile","{\"displayName\":\"Valid\",\"role\":\"ADMIN\"}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(400);
  String latest=parse(updated).get("etag").asString();
  try(var pool=Executors.newFixedThreadPool(2)) {
   var one=pool.submit(()->a.send("PATCH","/me/profile",body,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",latest));
   var two=pool.submit(()->a.send("PATCH","/me/profile",body,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",latest));
   assertThat(List.of(one.get().statusCode(),two.get().statusCode())).containsExactlyInAnyOrder(200,412);
  }
  assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='identity.profile_updated'")).isEqualTo(2);
  assertThat(parse(a.get("/me/security-notifications")).get("totalMatching").asInt()).isEqualTo(2);
  assertThat(parse(b.get("/me/security-notifications")).get("totalMatching").asInt()).isZero();
 }
 @Test void remoteSessionRevocationIsScopedVersionedAndDurable()throws Exception {
  Browser a=new Browser(),b=new Browser(),stranger=new Browser();String mobile="+919"+mobiles.incrementAndGet();a.login(mobile);b.login(mobile);stranger.login();
  var listing=a.get("/me/sessions");assertThat(listing.statusCode()).as(listing.body()).isEqualTo(200);
  assertThat(parse(listing).get("totalMatching").asInt()).isEqualTo(2);
  assertThat(listing.body()).doesNotContain("token_hash","session_id");
  JsonNode target=null,current=null;for(var item:parse(listing).get("items")){if(item.get("current").asBoolean())current=item;else target=item;}
  assertThat(target).isNotNull();assertThat(current).isNotNull();
  String id=target.get("id").asString(),path="/me/sessions/"+id+"/revoke",tag=target.get("etag").asString(),key=UUID.randomUUID().toString();
  assertThat(stranger.send("POST",path,"{}",key,stranger.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(404);
  assertThat(a.send("POST",path,"{}",key,a.csrf,"http://localhost:3000").statusCode()).isEqualTo(428);
  assertThat(a.send("POST",path,"{}",key,a.csrf,"http://localhost:3000","\"stale\"").statusCode()).isEqualTo(412);
  assertThat(a.send("POST","/me/sessions/"+current.get("id").asString()+"/revoke","{}",key,a.csrf,"http://localhost:3000",current.get("etag").asString()).statusCode()).isEqualTo(409);
  var result=a.send("POST",path,"{}",key,a.csrf,"http://localhost:3000",tag);assertThat(result.statusCode()).as(result.body()).isEqualTo(200);
  assertThat(parse(a.send("POST",path,"{}",key,a.csrf,"http://localhost:3000",tag))).isEqualTo(parse(result));
  assertThat(b.get("/me/profile").statusCode()).isEqualTo(401);assertThat(a.get("/me/profile").statusCode()).isEqualTo(200);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+id+"' AND action='identity.session_revoked'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM account_session WHERE id='"+id+"' AND revoked_at IS NOT NULL")).isEqualTo(1);
  assertThat(parse(a.get("/me/sessions")).get("totalMatching").asInt()).isEqualTo(1);
 }
 @Test void remoteRevocationRequiresRecentAuthentication()throws Exception {
  Browser a=new Browser(),b=new Browser();String mobile="+919"+mobiles.incrementAndGet();a.login(mobile);b.login(mobile);
  var items=parse(a.get("/me/sessions")).get("items");JsonNode target=null,current=null;
  for(var item:items){if(item.get("current").asBoolean())current=item;else target=item;}
  sql("UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP-interval '10 minutes' WHERE id='"+current.get("id").asString()+"'");
  var r=a.send("POST","/me/sessions/"+target.get("id").asString()+"/revoke","{}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",target.get("etag").asString());
  assertThat(r.statusCode()).as(r.body()).isEqualTo(403);assertThat(parse(r).get("code").asString()).isEqualTo("REAUTHENTICATION_REQUIRED");
  assertThat(b.get("/me/profile").statusCode()).isEqualTo(200);
 }

 String workspace(Browser b)throws Exception{return parse(b.post("/workspaces",body("Membership test",free),UUID.randomUUID().toString())).get("id").asString();}
 JsonNode invite(Browser owner,String w,String mobile)throws Exception{
  var r=owner.post("/workspaces/"+w+"/invitations",json.writeValueAsString(Map.of("mobile",mobile)),UUID.randomUUID().toString());assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
  assertThat(owner.get(r.headers().firstValue("Location").orElseThrow().replace("/api/v1", "")).statusCode()).isEqualTo(200);
  String id=parse(r).get("resourceId").asString();for(var row:parse(owner.get("/workspaces/"+w+"/invitations")).get("items"))if(row.get("id").asString().equals(id))return row;throw new AssertionError("Missing invite");
 }
 HttpResponse<String> respond(Browser b,JsonNode invite,String action,String key)throws Exception{
  return b.send("POST","/me/invitations/"+invite.get("id").asString()+"/"+action,"{}",key,b.csrf,"http://localhost:3000",invite.get("etag").asString());
 }
 @Test void invitationContactIsolationQuotaRaceAndRevocation()throws Exception{
  Browser owner=new Browser(),one=new Browser(),two=new Browser(),other=new Browser();owner.login();other.login();
  String m1="+919"+mobiles.incrementAndGet(),m2="+919"+mobiles.incrementAndGet();one.login(m1);two.login(m2);String w=workspace(owner);
  JsonNode i1=invite(owner,w,m1),i2=invite(owner,w,m2);
  assertThat(parse(other.get("/me/invitations")).get("items").size()).isZero();
  assertThat(respond(other,i1,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(404);
  assertThat(one.get("/workspaces/"+w+"/members").statusCode()).isEqualTo(404);
  assertThat(parse(one.get("/me/contexts")).get("items").size()).isZero();
  String k1=UUID.randomUUID().toString(),k2=UUID.randomUUID().toString();Browser winner,loser;JsonNode winning,losing;String winningKey;
  try(var pool=Executors.newFixedThreadPool(2)){
   var a=pool.submit(()->respond(one,i1,"accept",k1));var b=pool.submit(()->respond(two,i2,"accept",k2));var ra=a.get();var rb=b.get();
   assertThat(List.of(ra.statusCode(),rb.statusCode())).as(ra.body()+rb.body()).containsExactlyInAnyOrder(200,409);
   boolean first=ra.statusCode()==200;winner=first?one:two;loser=first?two:one;winning=first?i1:i2;losing=first?i2:i1;winningKey=first?k1:k2;
  }
  assertThat(respond(winner,winning,"accept",winningKey).statusCode()).isEqualTo(200);
  assertThat(parse(loser.get("/me/contexts")).get("items").size()).isZero();
  assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='managers'")).isEqualTo(1);
  assertThat(count("SELECT reserved FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='managers'")).isZero();
  assertThat(winner.get("/workspaces/"+w+"/setup").statusCode()).isEqualTo(404);
  assertThat(winner.post("/workspaces/"+w+"/invitations","{\"mobile\":\"+919123456789\"}",UUID.randomUUID().toString()).statusCode()).isEqualTo(404);
  JsonNode manager=null;for(var m:parse(owner.get("/workspaces/"+w+"/members")).get("items"))if(m.get("role").asString().equals("MANAGER"))manager=m;
  assertThat(manager).isNotNull();String path="/workspaces/"+w+"/members/"+manager.get("id").asString()+"/revoke",key=UUID.randomUUID().toString(),tag=manager.get("etag").asString();
  assertThat(other.send("POST",path,"{}",key,other.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(404);
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000","\"stale\"").statusCode()).isEqualTo(412);
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(200);
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(200);
  assertThat(parse(winner.get("/me/contexts")).get("items").size()).isZero();
  assertThat(respond(winner,winning,"accept",winningKey).statusCode()).isEqualTo(404);
  assertThat(respond(loser,losing,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);
  assertThat(count("SELECT count(*) FROM manager_allocation WHERE workspace_id='"+w+"' AND status='committed'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+w+"' AND action='workspace.member_revoked'")).isEqualTo(1);
 }
 @Test void invitationExpiryDiagnosticsAreScopedAndReconcileToStoredState()throws Exception{
  Browser owner=new Browser(),recipient=new Browser(),other=new Browser(),anonymous=new Browser();owner.login();other.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner),path="/workspaces/"+w+"/invitation-expiry";
  assertThat(anonymous.get(path).statusCode()).isEqualTo(401);assertThat(other.get(path).statusCode()).isEqualTo(404);assertThat(recipient.get(path).statusCode()).isEqualTo(404);
  var empty=owner.get(path);assertThat(empty.statusCode()).isEqualTo(200);assertThat(empty.headers().firstValue("Cache-Control").orElse("")).contains("no-store");assertThat(parse(empty).get("totalInvitations").asInt()).isZero();assertThat(parse(empty).get("oldestOverdueAt").isNull()).isTrue();
  var invitation=invite(owner,w,mobile);String id=invitation.get("id").asString();var open=parse(owner.get(path));assertThat(open.get("totalInvitations").asInt()).isEqualTo(1);assertThat(open.get("openInvitations").asInt()).isEqualTo(1);assertThat(open.get("awaitingExpiry").asInt()).isZero();
  sql("UPDATE workspace_invitation SET created_at=CURRENT_TIMESTAMP-interval '2 days',expires_at=CURRENT_TIMESTAMP-interval '1 day' WHERE id='"+id+"'");String otherW=workspace(other);invite(other,otherW,"+919"+mobiles.incrementAndGet());
  var due=parse(owner.get(path));assertThat(due.get("totalInvitations").asInt()).isEqualTo(1);assertThat(due.get("openInvitations").asInt()).isZero();assertThat(due.get("awaitingExpiry").asInt()).isEqualTo(1);assertThat(due.get("recordedExpired").asInt()).isZero();assertThat(due.get("oldestOverdueSeconds").asLong()).isGreaterThanOrEqualTo(86400);assertThat(due.get("oldestOverdueAt").isNull()).isFalse();
  expiry.expireDue();var recorded=parse(owner.get(path));assertThat(recorded.get("awaitingExpiry").asInt()).isZero();assertThat(recorded.get("recordedExpired").asInt()).isEqualTo(1);assertThat(recorded.get("oldestOverdueAt").isNull()).isTrue();assertThat(recorded.get("oldestOverdueSeconds").asLong()).isZero();
  var accepted=invite(owner,w,mobile);assertThat(respond(recipient,accepted,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);var finalState=parse(owner.get(path));assertThat(finalState.get("totalInvitations").asInt()).isEqualTo(2);assertThat(finalState.get("openInvitations").asInt()).isZero();assertThat(finalState.get("recordedExpired").asInt()).isEqualTo(1);assertThat(recipient.get(path).statusCode()).isEqualTo(404);
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"' AND role='OWNER'");assertThat(owner.get(path).statusCode()).isEqualTo(404);
 }
 @Test void expiredInvitationReplacementIsVersionedScopedAndSingleUse()throws Exception{
  Browser owner=new Browser(),other=new Browser(),recipient=new Browser();owner.login();other.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner);var old=invite(owner,w,mobile);String id=old.get("id").asString(),tag=old.get("etag").asString(),key=UUID.randomUUID().toString(),path="/workspaces/"+w+"/invitations/"+id+"/replace-expired";
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
  sql("UPDATE workspace_invitation SET created_at=CURRENT_TIMESTAMP-interval '2 days',expires_at=CURRENT_TIMESTAMP-interval '1 day' WHERE id='"+id+"'");
  assertThat(other.send("POST",path,"{}",key,other.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(404);assertThat(recipient.send("POST",path,"{}",key,recipient.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(404);
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000").statusCode()).isEqualTo(428);assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000","\"stale\"").statusCode()).isEqualTo(412);
  try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(()->owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag));var b=pool.submit(()->owner.send("POST",path,"{}",UUID.randomUUID().toString(),owner.csrf,"http://localhost:3000",tag));assertThat(List.of(a.get().statusCode(),b.get().statusCode())).containsExactlyInAnyOrder(201,409);}
  assertThat(count("SELECT count(*) FROM workspace_invitation WHERE replaces_invitation_id='"+id+"'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM manager_allocation WHERE workspace_id='"+w+"'")).isZero();
  JsonNode replacement=null;for(var i:parse(recipient.get("/me/invitations")).get("items"))if(!i.get("replacesInvitationId").isNull())replacement=i;assertThat(replacement).isNotNull();assertThat(replacement.get("maskedDestination")).isEqualTo(old.get("maskedDestination"));assertThat(respond(recipient,replacement,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);assertThat(respond(recipient,old,"accept",UUID.randomUUID().toString()).statusCode()).isIn(409,412);
  Browser emailOwner=new Browser();emailOwner.login();String w2=workspace(emailOwner);var emailOld=emailInvite(emailOwner,w2,"replacement@example.test",UUID.randomUUID().toString());String emailId=emailOld.get("id").asString(),emailTag=emailOld.get("etag").asString(),emailPath="/workspaces/"+w2+"/invitations/"+emailId+"/replace-expired",emailKey=UUID.randomUUID().toString();sql("UPDATE workspace_invitation SET created_at=CURRENT_TIMESTAMP-interval '2 days',expires_at=CURRENT_TIMESTAMP-interval '1 day' WHERE id='"+emailId+"'");
  var first=emailOwner.send("POST",emailPath,"{}",emailKey,emailOwner.csrf,"http://localhost:3000",emailTag);assertThat(first.statusCode()).isEqualTo(201);assertThat(parse(emailOwner.send("POST",emailPath,"{}",emailKey,emailOwner.csrf,"http://localhost:3000",emailTag))).isEqualTo(parse(first));assertThat(emailOwner.send("POST",emailPath,"{}",emailKey,emailOwner.csrf,"http://localhost:3000","\"changed\"").statusCode()).isEqualTo(409);assertThat(count("SELECT count(*) FROM workspace_invitation WHERE replaces_invitation_id='"+emailId+"' AND email='replacement@example.test'")).isEqualTo(1);
 }
 @Test void durableInvitationExpiryIsAtomicConcurrentAndRecipientSafe()throws Exception{
  Browser owner=new Browser(),recipient=new Browser();owner.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner),ownerId=parse(owner.get("/auth/session")).get("account").get("id").asString();var invitation=invite(owner,w,mobile);String id=invitation.get("id").asString();
  sql("UPDATE workspace_invitation SET created_at=CURRENT_TIMESTAMP-interval '2 days',expires_at=CURRENT_TIMESTAMP-interval '1 day' WHERE id='"+id+"'");
  assertThat(parse(recipient.get("/me/invitations")).get("items").get(0).get("status").asString()).isEqualTo("expired");
  sql("CREATE FUNCTION reject_expiry_notice() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.message LIKE '%"+id+"%' THEN RAISE EXCEPTION 'expiry test failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER reject_expiry_notice BEFORE INSERT ON account_notification FOR EACH ROW EXECUTE FUNCTION reject_expiry_notice()");
  try{assertThatThrownBy(()->expiry.expireDue()).isInstanceOf(RuntimeException.class);}finally{sql("DROP TRIGGER reject_expiry_notice ON account_notification");sql("DROP FUNCTION reject_expiry_notice()");}
  assertThat(count("SELECT count(*) FROM workspace_invitation WHERE id='"+id+"' AND status='pending' AND version=1")).isEqualTo(1);assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+id+"' AND action='workspace.invitation_expired'")).isZero();
  try(var pool=Executors.newFixedThreadPool(3)){var a=pool.submit(()->expiry.expireDue());var b=pool.submit(()->expiry.expireDue());var c=pool.submit(()->respond(recipient,invitation,"accept",UUID.randomUUID().toString()));a.get();b.get();assertThat(c.get().statusCode()).isIn(409,412);}
  expiry.expireDue();assertThat(count("SELECT count(*) FROM workspace_invitation WHERE id='"+id+"' AND status='expired' AND version=2")).isEqualTo(1);assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+id+"' AND action='workspace.invitation_expired'")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+ownerId+"' AND message LIKE '%"+id+"%'")).isEqualTo(1);assertThat(recipient.get("/me/notifications").body()).doesNotContain("expired without acceptance");assertThat(count("SELECT count(*) FROM manager_allocation WHERE workspace_id='"+w+"'")).isZero();
  var replacement=invite(owner,w,mobile);assertThat(respond(recipient,replacement,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);expiry.expireDue();assertThat(recipient.get("/workspaces/"+w+"/membership").statusCode()).isEqualTo(200);
  String w2=workspace(owner);var revoked=invite(owner,w2,mobile);String revokedId=revoked.get("id").asString();sql("UPDATE workspace_invitation SET created_at=CURRENT_TIMESTAMP-interval '2 days',expires_at=CURRENT_TIMESTAMP-interval '1 day' WHERE id='"+revokedId+"'");sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w2+"' AND role='OWNER'");expiry.expireDue();
  assertThat(count("SELECT count(*) FROM workspace_invitation WHERE id='"+revokedId+"' AND status='expired'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+revokedId+"' AND action='notification.invitation_expiry_suppressed'")).isEqualTo(1);assertThat(owner.get("/me/notifications").body()).doesNotContain(revokedId);
 }
 @Test void invitationOutcomesNotifyOnlyCurrentInviterAndRollbackWithResponse()throws Exception{
  Browser owner=new Browser(),recipient=new Browser(),other=new Browser();owner.login();other.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner);
  String ownerId=parse(owner.get("/auth/session")).get("account").get("id").asString(),recipientId=parse(recipient.get("/auth/session")).get("account").get("id").asString();
  JsonNode decline=invite(owner,w,mobile);String declineKey=UUID.randomUUID().toString();assertThat(respond(other,decline,"decline",UUID.randomUUID().toString()).statusCode()).isEqualTo(404);assertThat(respond(recipient,decline,"decline",declineKey).statusCode()).isEqualTo(200);assertThat(respond(recipient,decline,"decline",declineKey).statusCode()).isEqualTo(200);
  assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+ownerId+"' AND category='workspace_invitation'")).isEqualTo(1);assertThat(owner.get("/me/notifications").body()).contains("was declined").doesNotContain(mobile);
  assertThat(recipient.get("/me/notifications").body()).doesNotContain("was declined");assertThat(other.get("/me/notifications").body()).doesNotContain("was declined");
  JsonNode accepted=invite(owner,w,mobile);String key=UUID.randomUUID().toString();
  sql("CREATE FUNCTION reject_invitation_notice() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.category='workspace_invitation' THEN RAISE EXCEPTION 'test notification failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER reject_invitation_notice BEFORE INSERT ON account_notification FOR EACH ROW EXECUTE FUNCTION reject_invitation_notice()");
  try{assertThat(respond(recipient,accepted,"accept",key).statusCode()).isEqualTo(503);}finally{sql("DROP TRIGGER reject_invitation_notice ON account_notification");sql("DROP FUNCTION reject_invitation_notice()");}
  assertThat(recipient.get("/workspaces/"+w+"/membership").statusCode()).isEqualTo(404);assertThat(count("SELECT count(*) FROM manager_allocation WHERE workspace_id='"+w+"' AND status='committed'")).isZero();assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+recipientId+"' AND category='workspace_access'")).isZero();
  assertThat(respond(recipient,accepted,"accept",key).statusCode()).isEqualTo(200);assertThat(respond(recipient,accepted,"accept",key).statusCode()).isEqualTo(200);assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+ownerId+"' AND category='workspace_invitation'")).isEqualTo(2);
  String w2=workspace(owner);JsonNode revoked=invite(owner,w2,mobile);String revokedKey=UUID.randomUUID().toString();sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w2+"' AND role='OWNER'");assertThat(respond(recipient,revoked,"decline",revokedKey).statusCode()).isEqualTo(200);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+w2+"' AND action='notification.invitation_outcome_suppressed'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+ownerId+"' AND category='workspace_invitation'")).isEqualTo(2);
  sql("UPDATE workspace_member SET active=true WHERE workspace_id='"+w2+"' AND role='OWNER'");assertThat(respond(recipient,revoked,"decline",revokedKey).statusCode()).isEqualTo(200);assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+ownerId+"' AND category='workspace_invitation'")).isEqualTo(2);
 }
 @Test void essentialMembershipNoticesAreAtomicPrivateAndReplaySafe()throws Exception{
  Browser owner=new Browser(),recipient=new Browser(),other=new Browser();owner.login();other.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner);var invitation=invite(owner,w,mobile);
  String actor=parse(recipient.get("/auth/session")).get("account").get("id").asString();
  var pref=parse(recipient.get("/me/notification-preferences"));assertThat(recipient.send("PATCH","/me/notification-preferences","{\"workspaceSetupEnabled\":false,\"quietHoursEnabled\":true,\"quietStart\":\"00:00\",\"quietEnd\":\"23:59\"}",UUID.randomUUID().toString(),recipient.csrf,"http://localhost:3000",pref.get("etag").asString()).statusCode()).isEqualTo(200);
  String acceptKey=UUID.randomUUID().toString();assertThat(respond(recipient,invitation,"accept",acceptKey).statusCode()).isEqualTo(200);assertThat(respond(recipient,invitation,"accept",acceptKey).statusCode()).isEqualTo(200);
  assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+actor+"' AND category='workspace_access'")).isEqualTo(1);
  JsonNode member=null;for(var m:parse(owner.get("/workspaces/"+w+"/members")).get("items"))if(m.get("role").asString().equals("MANAGER"))member=m;assertThat(member).isNotNull();
  String path="/workspaces/"+w+"/members/"+member.get("id").asString()+"/revoke",key=UUID.randomUUID().toString(),tag=member.get("etag").asString();
  assertThat(other.send("POST",path,"{}",key,other.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(404);
  sql("CREATE FUNCTION reject_access_notice() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.category='workspace_access' THEN RAISE EXCEPTION 'test notice failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER reject_access_notice BEFORE INSERT ON account_notification FOR EACH ROW EXECUTE FUNCTION reject_access_notice()");
  try{assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(503);}finally{sql("DROP TRIGGER reject_access_notice ON account_notification");sql("DROP FUNCTION reject_access_notice()");}
  assertThat(recipient.get("/workspaces/"+w+"/membership").statusCode()).isEqualTo(200);assertThat(count("SELECT count(*) FROM manager_allocation WHERE workspace_id='"+w+"' AND status='committed'")).isEqualTo(1);
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(200);assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(200);
  assertThat(recipient.get("/workspaces/"+w+"/membership").statusCode()).isEqualTo(404);assertThat(recipient.get("/me/notifications").body()).contains("was revoked");assertThat(owner.get("/me/notifications").body()).doesNotContain("was revoked");assertThat(other.get("/me/notifications").body()).doesNotContain("was revoked");
  assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+actor+"' AND category='workspace_access'")).isEqualTo(2);assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+w+"' AND action='notification.workspace_access_revoked'")).isEqualTo(1);
 }
 @Test void invitationExpiryDeclineCancellationAndOwnerProtection()throws Exception{
  Browser owner=new Browser(),recipient=new Browser();owner.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner);
  JsonNode expired=invite(owner,w,mobile);sql("UPDATE workspace_invitation SET created_at=CURRENT_TIMESTAMP-interval '2 days',expires_at=CURRENT_TIMESTAMP-interval '1 day' WHERE id='"+expired.get("id").asString()+"'");
  assertThat(respond(recipient,expired,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(409);
  JsonNode declined=invite(owner,w,mobile);assertThat(respond(recipient,declined,"decline",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);
  assertThat(respond(recipient,declined,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(412);
  JsonNode cancelled=invite(owner,w,mobile);String cancel="/workspaces/"+w+"/invitations/"+cancelled.get("id").asString()+"/revoke";
  assertThat(owner.send("POST",cancel,"{}",UUID.randomUUID().toString(),owner.csrf,"http://localhost:3000",cancelled.get("etag").asString()).statusCode()).isEqualTo(200);
  assertThat(respond(recipient,cancelled,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(412);
  JsonNode oldOwner=invite(owner,w,mobile);sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"' AND role='OWNER'");assertThat(respond(recipient,oldOwner,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(409);
  sql("UPDATE workspace_member SET active=true WHERE workspace_id='"+w+"' AND role='OWNER'");
  var m=parse(owner.get("/workspaces/"+w+"/members")).get("items").get(0);
  assertThat(owner.send("POST","/workspaces/"+w+"/members/"+m.get("id").asString()+"/revoke","{}",UUID.randomUUID().toString(),owner.csrf,"http://localhost:3000",m.get("etag").asString()).statusCode()).isEqualTo(409);
  try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var statement=c.createStatement();var r=statement.executeQuery("SELECT count(*) FROM workspace_invitation")){r.next();assertThat(r.getInt(1)).isZero();}
 }

 @Test void usageIsScopedReconciledAndRestrictionDoesNotBlockRevocation()throws Exception{
  Browser owner=new Browser(),recipient=new Browser(),other=new Browser();owner.login();other.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner);
  var before=owner.get("/workspaces/"+w+"/entitlements");assertThat(before.statusCode()).as(before.body()).isEqualTo(200);
  assertThat(other.get("/workspaces/"+w+"/usage").statusCode()).isEqualTo(404);
  for(var q:parse(before).get("quotas"))if(q.get("resource").asString().equals("rentable_items")){assertThat(q.get("available").asBoolean()).isTrue();assertThat(q.get("used").asInt()).isZero();assertThat(q.get("reconciled").asBoolean()).isTrue();}
  var invitation=invite(owner,w,mobile);sql("UPDATE workspace SET status='restricted' WHERE id='"+w+"'");
  var blocked=respond(recipient,invitation,"accept",UUID.randomUUID().toString());assertThat(blocked.statusCode()).isEqualTo(409);assertThat(parse(blocked).get("code").asString()).isEqualTo("WORKSPACE_RESTRICTED");
  assertThat(count("SELECT count(*) FROM manager_usage_event WHERE workspace_id='"+w+"'")).isZero();
  var restricted=parse(owner.get("/workspaces/"+w+"/entitlements"));for(var c:restricted.get("capabilities"))if(c.get("code").asString().equals("WORKSPACE_MEMBER_ADD"))assertThat(c.get("eligible").asBoolean()).isFalse();
  sql("UPDATE workspace SET status='active' WHERE id='"+w+"'");assertThat(respond(recipient,invitation,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);
  assertThat(recipient.get("/workspaces/"+w+"/usage/history").statusCode()).isEqualTo(404);
  var snapshot=parse(owner.get("/workspaces/"+w+"/usage"));for(var q:snapshot.get("quotas"))if(q.get("resource").asString().equals("managers")){assertThat(q.get("used").asInt()).isEqualTo(1);assertThat(q.get("reconciled").asBoolean()).isTrue();}
  sql("UPDATE workspace SET status='suspended' WHERE id='"+w+"'");
  JsonNode manager=null;for(var m:parse(owner.get("/workspaces/"+w+"/members")).get("items"))if(m.get("role").asString().equals("MANAGER"))manager=m;
  String path="/workspaces/"+w+"/members/"+manager.get("id").asString()+"/revoke",key=UUID.randomUUID().toString(),tag=manager.get("etag").asString();
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(200);
  assertThat(owner.send("POST",path,"{}",key,owner.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(200);
  var history=owner.get("/workspaces/"+w+"/usage/history");assertThat(history.statusCode()).as(history.body()).isEqualTo(200);assertThat(parse(history).get("totalMatching").asInt()).isEqualTo(2);assertThat(parse(history).get("netChange").asInt()).isZero();
  assertThat(owner.get("/workspaces/"+w+"/usage/history?from=2026-12-02&to=2026-12-01").statusCode()).isEqualTo(422);
  // Event boundaries are India calendar dates, not UTC calendar dates.
  sql("UPDATE manager_usage_event SET occurred_at='2026-11-06T18:29:59Z' WHERE workspace_id='"+w+"' AND kind='committed'");
  sql("UPDATE manager_usage_event SET occurred_at='2026-11-06T18:30:00Z' WHERE workspace_id='"+w+"' AND kind='released'");
  var day=parse(owner.get("/workspaces/"+w+"/usage/history?from=2026-11-07&to=2026-11-07"));assertThat(day.get("totalMatching").asInt()).isEqualTo(1);assertThat(day.get("netChange").asInt()).isEqualTo(-1);
  // A corrupted counter is reported as unknown remaining capacity and blocks further consumption.
  sql("UPDATE workspace_grant SET used=1 WHERE workspace_id='"+w+"' AND resource='managers'");
  for(var q:parse(owner.get("/workspaces/"+w+"/usage")).get("quotas"))if(q.get("resource").asString().equals("managers")){assertThat(q.get("reconciled").asBoolean()).isFalse();assertThat(q.get("remaining").isNull()).isTrue();}
  sql("UPDATE workspace SET status='active' WHERE id='"+w+"'");var retryInvite=invite(owner,w,mobile);
  var denied=respond(recipient,retryInvite,"accept",UUID.randomUUID().toString());assertThat(denied.statusCode()).isEqualTo(409);assertThat(parse(denied).get("code").asString()).isEqualTo("CAPACITY_RECONCILIATION_REQUIRED");
  // Equal totals must not conceal a missing historical commit/release pair.
  sql("UPDATE workspace_grant SET used=0 WHERE workspace_id='"+w+"' AND resource='managers'");
  sql("DELETE FROM manager_usage_event WHERE workspace_id='"+w+"'");
  for(var q:parse(owner.get("/workspaces/"+w+"/usage")).get("quotas"))if(q.get("resource").asString().equals("managers")){
   assertThat(q.get("evidence").get("eventBalance").asInt()).isZero();assertThat(q.get("evidence").get("committedAllocations").asInt()).isZero();
   assertThat(q.get("evidence").get("inconsistentAllocations").asInt()).isEqualTo(1);assertThat(q.get("reconciled").asBoolean()).isFalse();assertThat(q.get("remaining").isNull()).isTrue();
  }
  var missingPair=respond(recipient,retryInvite,"accept",UUID.randomUUID().toString());assertThat(missingPair.statusCode()).isEqualTo(409);assertThat(parse(missingPair).get("code").asString()).isEqualTo("CAPACITY_RECONCILIATION_REQUIRED");
  assertThat(count("SELECT count(*) FROM workspace_member WHERE workspace_id='"+w+"' AND active AND role='MANAGER'")).isZero();

 }

 @Test void unexplainedReservationBlocksAcceptanceWithoutConsumingOrRewritingEvidence()throws Exception{
  Browser owner=new Browser(),recipient=new Browser();owner.login();String mobile="+919"+mobiles.incrementAndGet();recipient.login(mobile);String w=workspace(owner);var invitation=invite(owner,w,mobile);
  sql("UPDATE workspace_grant SET reserved=1 WHERE workspace_id='"+w+"' AND resource='managers'");
  for(var q:parse(owner.get("/workspaces/"+w+"/usage")).get("quotas"))if(q.get("resource").asString().equals("managers")){
   assertThat(q.get("evidence").get("unexplainedReservations").asInt()).isEqualTo(1);assertThat(q.get("reconciled").asBoolean()).isFalse();assertThat(q.get("remaining").isNull()).isTrue();
  }
  var blocked=respond(recipient,invitation,"accept",UUID.randomUUID().toString());assertThat(blocked.statusCode()).isEqualTo(409);assertThat(parse(blocked).get("code").asString()).isEqualTo("CAPACITY_RECONCILIATION_REQUIRED");
  assertThat(count("SELECT count(*) FROM workspace_member WHERE workspace_id='"+w+"' AND active AND role='MANAGER'")).isZero();
  assertThat(count("SELECT count(*) FROM manager_usage_event WHERE workspace_id='"+w+"'")).isZero();
  assertThat(count("SELECT reserved FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='managers'")).isEqualTo(1);
  // A separately authorized, reviewed repair resumes the original blocked acceptance.
  String operator=parse(owner.get("/auth/session")).get("account").get("id").asString();
  sql("INSERT INTO capacity_repair_operator_grant(account_id,workspace_id,valid_until,reason,issued_by) VALUES('"+operator+"','"+w+"',CURRENT_TIMESTAMP+interval '1 hour','Investigated reservation drift','test')");
  String repairPath="/admin/workspaces/"+w+"/manager-capacity-repair",repairTag=parse(owner.get(repairPath)).get("etag").asString();
  assertThat(owner.send("POST",repairPath,"{\"reason\":\"Investigated reservation drift\"}",UUID.randomUUID().toString(),owner.csrf,"http://localhost:3000",repairTag).statusCode()).isEqualTo(200);
  assertThat(respond(recipient,invitation,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);
 }

 @Test void usageHistoryPaginationIsBoundToActorWorkspaceAndDates()throws Exception{
  Browser owner=new Browser(),manager=new Browser();owner.login();String mobile="+919"+mobiles.incrementAndGet();manager.login(mobile);String w=workspace(owner);
  for(int i=0;i<14;i++){
   var invitation=invite(owner,w,mobile);assertThat(respond(manager,invitation,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);
   JsonNode member=null;for(var m:parse(owner.get("/workspaces/"+w+"/members")).get("items"))if(m.get("role").asString().equals("MANAGER"))member=m;
   assertThat(owner.send("POST","/workspaces/"+w+"/members/"+member.get("id").asString()+"/revoke","{}",UUID.randomUUID().toString(),owner.csrf,"http://localhost:3000",member.get("etag").asString()).statusCode()).isEqualTo(200);
  }
  var first=parse(owner.get("/workspaces/"+w+"/usage/history"));assertThat(first.get("totalMatching").asInt()).isEqualTo(28);assertThat(first.get("items").size()).isEqualTo(25);
  String cursor=URLEncoder.encode(first.get("page").get("nextCursor").asString(),java.nio.charset.StandardCharsets.UTF_8);
  var second=parse(owner.get("/workspaces/"+w+"/usage/history?cursor="+cursor));assertThat(second.get("items").size()).isEqualTo(3);assertThat(second.get("netChange").asInt()).isZero();
  Set<String> ids=new HashSet<>();for(var item:first.get("items"))ids.add(item.get("id").asString());for(var item:second.get("items"))assertThat(ids.add(item.get("id").asString())).isTrue();
  assertThat(owner.get("/workspaces/"+w+"/usage/history?from=2020-01-01&to=2020-01-02&cursor="+cursor).statusCode()).isEqualTo(422);
  try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var statement=c.createStatement()){
   assertThatThrownBy(()->statement.execute("UPDATE manager_usage_event SET quantity=quantity")).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo("42501"));
  }
 }

 String emailChallenge(Browser b,String email,String key)throws Exception{
  b.token();var r=b.post("/auth/challenges",json.writeValueAsString(Map.of("channel","email","destination",email,"purpose","sign_in","displayName","Email Test Owner")),key);
  assertThat(r.statusCode()).as(r.body()).isEqualTo(202);return parse(r).get("id").asString();
 }
 @Test void emailProofCreatesIndependentAccountAndReturnsToPersistedWorkspace()throws Exception{
  Browser a=new Browser(),b=new Browser(),phone=new Browser();String email="Owner"+UUID.randomUUID()+"@EXAMPLE.test";
  String key=UUID.randomUUID().toString(),id=emailChallenge(a,email,key);assertThat(emailChallenge(a,email.replace("EXAMPLE","example"),key)).isEqualTo(id);
  assertThat(b.get("/auth/challenges/"+id).statusCode()).isEqualTo(404);
  var verified=a.post("/auth/challenges/"+id+"/verify","{\"code\":\""+otp(id)+"\"}",null);assertThat(verified.statusCode()).as(verified.body()).isEqualTo(200);String account=parse(verified).get("account").get("id").asString();a.token();String w=workspace(a);
  var profile=parse(a.get("/me/profile"));assertThat(profile.get("maskedMobile").isNull()).isTrue();assertThat(profile.get("maskedEmail").asString()).endsWith("@example.test");
  assertThat(parse(a.get("/me/invitations")).get("totalMatching").asInt()).isZero();
  assertThat(a.post("/auth/challenges/"+id+"/verify","{\"code\":\""+otp(id)+"\"}",null).statusCode()).isEqualTo(404);
  String second=emailChallenge(b,email,UUID.randomUUID().toString());var returning=b.post("/auth/challenges/"+second+"/verify","{\"code\":\""+otp(second)+"\"}",null);assertThat(returning.statusCode()).isEqualTo(200);assertThat(parse(returning).get("account").get("id").asString()).isEqualTo(account);assertThat(b.get("/workspaces/"+w+"/usage").statusCode()).isEqualTo(200);
  phone.login();assertThat(parse(phone.get("/auth/session")).get("account").get("id").asString()).isNotEqualTo(account);assertThat(phone.get("/workspaces/"+w+"/usage").statusCode()).isEqualTo(404);
 }
 @Test void emailProofLimitsValidationAndConcurrentIdentityCreation()throws Exception{
  Browser a=new Browser(),b=new Browser();a.token();String email="Proof"+UUID.randomUUID()+"@example.test";
  var invalid=a.post("/auth/challenges","{\"channel\":\"email\",\"destination\":\"bad..local@example.test\",\"purpose\":\"sign_in\",\"displayName\":\"Test\"}",UUID.randomUUID().toString());assertThat(invalid.statusCode()).isEqualTo(422);
  String first=emailChallenge(a,email,UUID.randomUUID().toString()),second=emailChallenge(b,email,UUID.randomUUID().toString());String firstCode=otp(first),secondCode=otp(second);
  try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->a.post("/auth/challenges/"+first+"/verify","{\"code\":\""+firstCode+"\"}",null));var y=pool.submit(()->b.post("/auth/challenges/"+second+"/verify","{\"code\":\""+secondCode+"\"}",null));var xr=x.get();var yr=y.get();assertThat(xr.statusCode()).isEqualTo(200);assertThat(yr.statusCode()).isEqualTo(200);assertThat(parse(xr).get("account").get("id").asString()).isEqualTo(parse(yr).get("account").get("id").asString());}
  Browser c=new Browser();String expired=emailChallenge(c,"Expired"+UUID.randomUUID()+"@example.test",UUID.randomUUID().toString());sql("UPDATE auth_challenge SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+expired+"'");assertThat(c.post("/auth/challenges/"+expired+"/verify","{\"code\":\"000000\"}",null).statusCode()).isEqualTo(409);
  String locked=emailChallenge(c,"Locked"+UUID.randomUUID()+"@example.test",UUID.randomUUID().toString());String wrong=otp(locked).equals("000000")?"111111":"000000";for(int n=0;n<5;n++)assertThat(c.post("/auth/challenges/"+locked+"/verify","{\"code\":\""+wrong+"\"}",null).statusCode()).isEqualTo(422);assertThat(c.post("/auth/challenges/"+locked+"/verify","{\"code\":\""+otp(locked)+"\"}",null).statusCode()).isEqualTo(409);
 }

 @Test void emailRateLimitAndSandboxCannotAuthenticateLiveIdentity()throws Exception{
  Browser a=new Browser();String email="Limit"+UUID.randomUUID()+"@example.test";
  for(int n=0;n<5;n++)emailChallenge(a,email,UUID.randomUUID().toString());
  var limited=a.post("/auth/challenges",json.writeValueAsString(Map.of("channel","email","destination",email,"purpose","sign_in","displayName","Email Test Owner")),UUID.randomUUID().toString());assertThat(limited.statusCode()).isEqualTo(429);
  Browser b=new Browser();String live="Live"+UUID.randomUUID()+"@example.test";sql("INSERT INTO user_account(id,email,display_name,identity_mode) VALUES(gen_random_uuid(),'"+live+"','Isolated live mode fixture','live')");String id=emailChallenge(b,live,UUID.randomUUID().toString());
  var denied=b.post("/auth/challenges/"+id+"/verify","{\"code\":\""+otp(id)+"\"}",null);assertThat(denied.statusCode()).isEqualTo(403);assertThat(parse(denied).get("code").asString()).isEqualTo("IDENTITY_MODE_CONFLICT");assertThat(parse(b.get("/auth/session")).get("authenticated").asBoolean()).isFalse();
 }

 String profileTag(Browser b)throws Exception{return parse(b.get("/me/profile")).get("etag").asString();}
 String linkChallenge(Browser b,String email,String tag)throws Exception{var r=b.send("POST","/me/identity-links/challenges",json.writeValueAsString(Map.of("destination",email)),UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag);assertThat(r.statusCode()).as(r.body()).isEqualTo(202);return parse(r).get("id").asString();}
 HttpResponse<String> link(Browser b,String id,String tag,String key,String code)throws Exception{return b.send("POST","/me/identity-links",json.writeValueAsString(Map.of("challengeId",id,"code",code)),key,b.csrf,"http://localhost:3000",tag);}
 @Test void explicitEmailLinkKeepsAccountAndContextsWithIdempotentRecovery()throws Exception{
  Browser a=new Browser(),b=new Browser();a.login();String actor=parse(a.get("/auth/session")).get("account").get("id").asString(),w=workspace(a),email="Linked"+UUID.randomUUID()+"@example.test",tag=profileTag(a),id=linkChallenge(a,email,tag),code=otp(id),key=UUID.randomUUID().toString();
  assertThat(a.post("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",code)),null).statusCode()).isEqualTo(404);
  assertThatThrownBy(()->sql("UPDATE auth_challenge SET link_profile_version=NULL WHERE id='"+id+"'")).isInstanceOf(SQLException.class);
  String wrong=code.equals("000000")?"111111":"000000";assertThat(link(a,id,tag,UUID.randomUUID().toString(),wrong).statusCode()).isEqualTo(422);
  var result=link(a,id,tag,key,code);assertThat(result.statusCode()).as(result.body()).isEqualTo(200);assertThat(parse(result).get("id").asString()).isEqualTo(actor);assertThat(parse(result).get("maskedMobile").isNull()).isFalse();assertThat(parse(result).get("maskedEmail").asString()).endsWith("@example.test");
  assertThat(link(a,id,tag,key,code).body()).isEqualTo(result.body());assertThat(link(a,id,tag,key,wrong).statusCode()).isEqualTo(409);assertThat(link(a,id,tag,UUID.randomUUID().toString(),code).statusCode()).isEqualTo(412);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='identity.email_linked'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+actor+"' AND message LIKE 'An email sign-in%'")).isEqualTo(1);
  String signin=emailChallenge(b,email,UUID.randomUUID().toString());var session=b.post("/auth/challenges/"+signin+"/verify",json.writeValueAsString(Map.of("code",otp(signin))),null);assertThat(session.statusCode()).isEqualTo(200);assertThat(parse(session).get("account").get("id").asString()).isEqualTo(actor);assertThat(b.get("/workspaces/"+w+"/usage").statusCode()).isEqualTo(200);
  sql("UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP-interval '10 minutes' WHERE account_id='"+actor+"'");assertThat(link(a,id,tag,key,code).statusCode()).isEqualTo(403);
 }
 @Test void linkingRejectsOtherAccountPurposeStaleProfileAndExistingIdentity()throws Exception{
  Browser a=new Browser(),b=new Browser();a.login();b.login();String tag=profileTag(a),email="Conflict"+UUID.randomUUID()+"@example.test",id=linkChallenge(a,email,tag),code=otp(id);
  assertThat(link(b,id,profileTag(b),UUID.randomUUID().toString(),code).statusCode()).isEqualTo(404);
  var edit=a.send("PATCH","/me/profile","{\"displayName\":\"Changed profile\"}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag);assertThat(edit.statusCode()).isEqualTo(200);assertThat(link(a,id,tag,UUID.randomUUID().toString(),code).statusCode()).isEqualTo(412);
  String sign=emailChallenge(b,email,UUID.randomUUID().toString());assertThat(link(a,sign,profileTag(a),UUID.randomUUID().toString(),otp(sign)).statusCode()).isEqualTo(404);
  assertThat(b.post("/auth/challenges/"+sign+"/verify",json.writeValueAsString(Map.of("code",otp(sign))),null).statusCode()).isEqualTo(200);
  String current=profileTag(a),conflict=linkChallenge(a,email,current);var denied=link(a,conflict,current,UUID.randomUUID().toString(),otp(conflict));assertThat(denied.statusCode()).isEqualTo(409);assertThat(parse(denied).get("code").asString()).isEqualTo("CONTACT_LINK_UNAVAILABLE");assertThat(parse(a.get("/me/profile")).get("maskedEmail").isNull()).isTrue();
 }
 @Test void concurrentEmailLinksChooseOneAccountAndAuditFailureRollsBack()throws Exception{
  Browser a=new Browser(),b=new Browser();a.login();b.login();String email="RaceLink"+UUID.randomUUID()+"@example.test",at=profileTag(a),bt=profileTag(b),ai=linkChallenge(a,email,at),bi=linkChallenge(b,email,bt),ac=otp(ai),bc=otp(bi);
  sql("CREATE FUNCTION reject_email_link_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='identity.email_linked' THEN RAISE EXCEPTION 'isolated email audit failure'; END IF; RETURN NEW; END $$; CREATE TRIGGER reject_email_link_audit BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION reject_email_link_audit()");
  try{assertThat(link(a,ai,at,UUID.randomUUID().toString(),ac).statusCode()).isGreaterThanOrEqualTo(500);assertThat(parse(a.get("/me/profile")).get("maskedEmail").isNull()).isTrue();assertThat(count("SELECT count(*) FROM auth_challenge WHERE id='"+ai+"' AND consumed")).isZero();}finally{sql("DROP TRIGGER reject_email_link_audit ON domain_audit; DROP FUNCTION reject_email_link_audit()");}
  try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->link(a,ai,at,UUID.randomUUID().toString(),ac).statusCode());var y=pool.submit(()->link(b,bi,bt,UUID.randomUUID().toString(),bc).statusCode());assertThat(List.of(x.get(),y.get())).containsExactlyInAnyOrder(200,409);}
 }

 @Test void linkingExpiryAttemptLimitAndRecentSignInAreEnforced()throws Exception{
  Browser a=new Browser();a.login();String tag=profileTag(a),id=linkChallenge(a,"ExpiredLink"+UUID.randomUUID()+"@example.test",tag);String code=otp(id);sql("UPDATE auth_challenge SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+id+"'");assertThat(link(a,id,tag,UUID.randomUUID().toString(),code).statusCode()).isEqualTo(409);
  String locked=linkChallenge(a,"LockedLink"+UUID.randomUUID()+"@example.test",tag),correct=otp(locked),wrong=correct.equals("000000")?"111111":"000000";for(int n=0;n<5;n++)assertThat(link(a,locked,tag,UUID.randomUUID().toString(),wrong).statusCode()).isEqualTo(422);assertThat(link(a,locked,tag,UUID.randomUUID().toString(),correct).statusCode()).isEqualTo(409);
  String actor=parse(a.get("/auth/session")).get("account").get("id").asString();sql("UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP-interval '10 minutes' WHERE account_id='"+actor+"'");assertThat(a.send("POST","/me/identity-links/challenges","{\"destination\":\"recent-required@example.test\"}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(403);assertThat(parse(a.get("/me/profile")).get("maskedEmail").isNull()).isTrue();
 }

 void loginEmail(Browser b)throws Exception{String id=emailChallenge(b,"MobileLink"+UUID.randomUUID()+"@example.test",UUID.randomUUID().toString());assertThat(b.post("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",otp(id))),null).statusCode()).isEqualTo(200);b.token();}
 String mobileLinkChallenge(Browser b,String mobile,String tag)throws Exception{var r=b.send("POST","/me/identity-links/challenges",json.writeValueAsString(Map.of("channel","mobile","destination",mobile)),UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag);assertThat(r.statusCode()).as(r.body()).isEqualTo(202);return parse(r).get("id").asString();}
 @Test void linkedMobilePreservesEmailAccountAndRequiresExplicitInvitationAcceptance()throws Exception{
  Browser a=new Browser(),owner=new Browser(),returning=new Browser();loginEmail(a);owner.login();String original=parse(a.get("/auth/session")).get("account").get("id").asString(),own=workspace(a),inviting=workspace(owner),mobile="+919"+mobiles.incrementAndGet();var invitation=invite(owner,inviting,mobile);assertThat(parse(a.get("/me/invitations")).get("totalMatching").asInt()).isZero();
  String tag=profileTag(a),id=mobileLinkChallenge(a,mobile,tag),code=otp(id),key=UUID.randomUUID().toString();assertThat(a.post("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",code)),null).statusCode()).isEqualTo(404);
  assertThatThrownBy(()->sql("UPDATE auth_challenge SET purpose='link_email' WHERE id='"+id+"'")).isInstanceOf(SQLException.class);
  var result=link(a,id,tag,key,code);assertThat(result.statusCode()).as(result.body()).isEqualTo(200);assertThat(parse(result).get("id").asString()).isEqualTo(original);assertThat(parse(result).get("maskedEmail").isNull()).isFalse();assertThat(parse(result).get("maskedMobile").asString()).endsWith(mobile.substring(mobile.length()-4));assertThat(link(a,id,tag,key,code).body()).isEqualTo(result.body());
  assertThat(a.get("/workspaces/"+own+"/usage").statusCode()).isEqualTo(200);assertThat(a.get("/workspaces/"+inviting+"/membership").statusCode()).isEqualTo(404);assertThat(parse(a.get("/me/invitations")).get("totalMatching").asInt()).isEqualTo(1);
  assertThat(respond(a,invitation,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(200);assertThat(a.get("/workspaces/"+inviting+"/membership").statusCode()).isEqualTo(200);
  returning.login(mobile);assertThat(parse(returning.get("/auth/session")).get("account").get("id").asString()).isEqualTo(original);assertThat(returning.get("/workspaces/"+own+"/usage").statusCode()).isEqualTo(200);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+original+"' AND action='identity.mobile_linked'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+original+"' AND message LIKE 'A mobile sign-in%'")).isEqualTo(1);
 }
 @Test void mobileLinkCannotTakeOverExistingContactOrBypassActorAndExpiry()throws Exception{
  Browser a=new Browser(),other=new Browser();loginEmail(a);String mobile="+919"+mobiles.incrementAndGet();other.login(mobile);String tag=profileTag(a),id=mobileLinkChallenge(a,mobile,tag),code=otp(id);
  assertThat(link(other,id,profileTag(other),UUID.randomUUID().toString(),code).statusCode()).isEqualTo(404);var denied=link(a,id,tag,UUID.randomUUID().toString(),code);assertThat(denied.statusCode()).isEqualTo(409);assertThat(parse(denied).get("code").asString()).isEqualTo("CONTACT_LINK_UNAVAILABLE");assertThat(parse(a.get("/me/profile")).get("maskedMobile").isNull()).isTrue();
  String expired=mobileLinkChallenge(a,"+919"+mobiles.incrementAndGet(),tag),proof=otp(expired);sql("UPDATE auth_challenge SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+expired+"'");assertThat(link(a,expired,tag,UUID.randomUUID().toString(),proof).statusCode()).isEqualTo(409);
 }
 @Test void mobileSignInAndLinkSerializeContactOwnership()throws Exception{
  Browser a=new Browser(),b=new Browser();loginEmail(a);String mobile="+919"+mobiles.incrementAndGet(),tag=profileTag(a),linked=mobileLinkChallenge(a,mobile,tag),sign=b.challenge(mobile),lc=otp(linked),sc=otp(sign);String actor=parse(a.get("/auth/session")).get("account").get("id").asString();
  try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->link(a,linked,tag,UUID.randomUUID().toString(),lc));var y=pool.submit(()->b.post("/auth/challenges/"+sign+"/verify",json.writeValueAsString(Map.of("code",sc)),null));var lr=x.get();var sr=y.get();assertThat(sr.statusCode()).isEqualTo(200);assertThat(lr.statusCode()).isIn(200,409);String signedIn=parse(sr).get("account").get("id").asString();if(lr.statusCode()==200)assertThat(signedIn).isEqualTo(actor);else{assertThat(signedIn).isNotEqualTo(actor);assertThat(parse(a.get("/me/profile")).get("maskedMobile").isNull()).isTrue();}}
  assertThat(count("SELECT count(*) FROM user_account WHERE mobile='"+mobile+"'")).isEqualTo(1);
 }

 String reauthentication(Browser b,String channel,String tag)throws Exception{var r=b.send("POST","/me/reauthentication/challenges",json.writeValueAsString(Map.of("channel",channel)),UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag);assertThat(r.statusCode()).as(r.body()).isEqualTo(202);return parse(r).get("id").asString();}
 HttpResponse<String> reverify(Browser b,String id,String code,String tag)throws Exception{return b.send("POST","/me/reauthentication/verify",json.writeValueAsString(Map.of("challengeId",id,"code",code)),null,b.csrf,"http://localhost:3000",tag);}
 @Test void reauthenticationRotatesSessionAndRestoresRecentAuthorityWithoutNewAccount()throws Exception{
  Browser a=new Browser();a.login();String actor=parse(a.get("/auth/session")).get("account").get("id").asString(),w=workspace(a),tag=profileTag(a);String oldCookie=a.cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("lmt_dev_session")).findFirst().orElseThrow().getValue();
  sql("UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP-interval '10 minutes' WHERE account_id='"+actor+"'");var blocked=a.send("POST","/me/identity-links/challenges","{\"destination\":\"recent@example.test\"}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag);assertThat(blocked.statusCode()).isEqualTo(403);
  String id=reauthentication(a,"mobile",tag),code=otp(id);assertThat(a.post("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",code)),null).statusCode()).isEqualTo(404);assertThat(link(a,id,tag,UUID.randomUUID().toString(),code).statusCode()).isEqualTo(403);
  var result=reverify(a,id,code,tag);assertThat(result.statusCode()).as(result.body()).isEqualTo(200);assertThat(parse(result).get("status").asString()).isEqualTo("reauthenticated");String next=a.cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("lmt_dev_session")).findFirst().orElseThrow().getValue();assertThat(next).isNotEqualTo(oldCookie);assertThat(a.get("/workspaces/"+w+"/usage").statusCode()).isEqualTo(200);
  var stale=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1/me/profile")).header("Cookie","lmt_dev_session="+oldCookie).GET().build();assertThat(HttpClient.newHttpClient().send(stale,HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
  assertThat(reverify(a,id,code,tag).statusCode()).isEqualTo(403);a.token();assertThat(reverify(a,id,code,tag).statusCode()).isEqualTo(404);assertThat(parse(a.get("/auth/session")).get("account").get("id").asString()).isEqualTo(actor);
  linkChallenge(a,"Renewed"+UUID.randomUUID()+"@example.test",tag);assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='identity.reauthenticated'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM account_notification WHERE account_id='"+actor+"' AND message LIKE 'Your current browser session was verified%'")).isEqualTo(1);
 }
 @Test void reauthenticationRejectsWrongActorStaleProfileAndExpiredProof()throws Exception{
  Browser a=new Browser(),b=new Browser();loginEmail(a);b.login();String tag=profileTag(a);assertThat(a.send("POST","/me/reauthentication/challenges","{\"channel\":\"mobile\"}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(422);String id=reauthentication(a,"email",tag),code=otp(id);assertThat(reverify(b,id,code,profileTag(b)).statusCode()).isEqualTo(404);
  assertThat(reverify(a,id,code.equals("000000")?"111111":"000000",tag).statusCode()).isEqualTo(422);sql("UPDATE auth_challenge SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+id+"'");assertThat(reverify(a,id,code,tag).statusCode()).isEqualTo(409);
  String fresh=reauthentication(a,"email",tag);assertThat(a.send("PATCH","/me/profile","{\"displayName\":\"Reauth profile changed\"}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(200);assertThat(reverify(a,fresh,otp(fresh),tag).statusCode()).isEqualTo(412);
 }
 @Test void reauthenticationAuditFailureCannotRefreshRegistryOrConsumeProof()throws Exception{
  Browser a=new Browser();a.login();String actor=parse(a.get("/auth/session")).get("account").get("id").asString();sql("UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP-interval '10 minutes' WHERE account_id='"+actor+"'");String tag=profileTag(a),id=reauthentication(a,"mobile",tag),code=otp(id);
  sql("CREATE FUNCTION reject_reauthentication_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='identity.reauthenticated' THEN RAISE EXCEPTION 'isolated audit failure'; END IF; RETURN NEW; END $$; CREATE TRIGGER reject_reauthentication_audit BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION reject_reauthentication_audit()");
  try{assertThat(reverify(a,id,code,tag).statusCode()).isGreaterThanOrEqualTo(500);assertThat(count("SELECT count(*) FROM auth_challenge WHERE id='"+id+"' AND consumed")).isZero();assertThat(count("SELECT count(*) FROM account_session WHERE account_id='"+actor+"' AND authenticated_at>CURRENT_TIMESTAMP-interval '5 minutes'")).isZero();}finally{sql("DROP TRIGGER reject_reauthentication_audit ON domain_audit; DROP FUNCTION reject_reauthentication_audit()");}
 }
 String notice(Browser b)throws Exception{
  var r=b.send("PATCH","/me/profile","{\"displayName\":\"Notification review test\"}",UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",profileTag(b));assertThat(r.statusCode()).isEqualTo(200);
  return parse(b.get("/me/notifications")).get("items").get(0).get("id").asString();
 }
 HttpResponse<String> readNotice(Browser b,String id,String tag,String key)throws Exception{return b.send("POST","/me/notifications/"+id+"/read",null,key,b.csrf,"http://localhost:3000",tag);}
 @Test void notificationReadIsRecipientScopedVersionedAndReplaySafe()throws Exception{
  Browser a=new Browser(),other=new Browser();a.login();other.login();String id=notice(a),tag=parse(a.get("/me/notifications")).get("items").get(0).get("etag").asString(),key=UUID.randomUUID().toString();
  assertThat(parse(a.get("/me/notifications")).get("unreadCount").asInt()).isEqualTo(1);
  assertThat(readNotice(other,id,tag,key).statusCode()).isEqualTo(404);assertThat(parse(other.get("/me/notifications")).get("totalMatching").asInt()).isZero();
  assertThat(readNotice(a,id,null,key).statusCode()).isEqualTo(428);
  assertThat(a.send("POST","/me/notifications/"+id+"/read",null,key,"wrong","http://localhost:3000",tag).statusCode()).isEqualTo(403);
  var read=readNotice(a,id,tag,key);assertThat(read.statusCode()).as(read.body()).isEqualTo(200);assertThat(parse(read).get("readAt").isNull()).isFalse();
  assertThat(readNotice(a,id,tag,key).body()).isEqualTo(read.body());assertThat(readNotice(a,id,tag,UUID.randomUUID().toString()).statusCode()).isEqualTo(412);
  assertThat(readNotice(a,UUID.randomUUID().toString(),tag,key).statusCode()).isEqualTo(409);
  assertThat(parse(a.get("/me/security-notifications")).get("unreadCount").asInt()).isZero();
  var again=readNotice(a,id,parse(read).get("etag").asString(),UUID.randomUUID().toString());assertThat(again.body()).isEqualTo(read.body());
  assertThat(count("SELECT count(*) FROM domain_audit WHERE action='identity.notification_read' AND resource_id='"+id+"'")).isEqualTo(1);
 }
 @Test void notificationReadRaceAndAuditFailurePreserveFirstReadEvidence()throws Exception{
  Browser a=new Browser();a.login();String id=notice(a),tag=parse(a.get("/me/notifications")).get("items").get(0).get("etag").asString();
  sql("CREATE FUNCTION reject_notice_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='identity.notification_read' THEN RAISE EXCEPTION 'isolated audit failure'; END IF; RETURN NEW; END $$; CREATE TRIGGER reject_notice_audit BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION reject_notice_audit()");
  String key=UUID.randomUUID().toString();try{assertThat(readNotice(a,id,tag,key).statusCode()).isGreaterThanOrEqualTo(500);assertThat(count("SELECT count(*) FROM account_notification WHERE id='"+id+"' AND read_at IS NULL AND version=1")).isEqualTo(1);}finally{sql("DROP TRIGGER reject_notice_audit ON domain_audit; DROP FUNCTION reject_notice_audit()");}
  try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->readNotice(a,id,tag,key));var y=pool.submit(()->readNotice(a,id,tag,UUID.randomUUID().toString()));assertThat(List.of(x.get().statusCode(),y.get().statusCode())).containsExactlyInAnyOrder(200,412);}
  assertThat(count("SELECT count(*) FROM domain_audit WHERE action='identity.notification_read' AND resource_id='"+id+"'")).isEqualTo(1);
 }
 @Test void notificationUnreadCountCoversEveryPageAndRejectsForeignCursor()throws Exception{
  Browser a=new Browser(),b=new Browser();a.login();b.login();String actor=parse(a.get("/auth/session")).get("account").get("id").asString();
  sql("INSERT INTO account_notification(id,account_id,message) SELECT gen_random_uuid(),'"+actor+"','Pagination fixture' FROM generate_series(1,30)");
  var first=parse(a.get("/me/notifications"));assertThat(first.get("items").size()).isEqualTo(25);assertThat(first.get("unreadCount").asInt()).isEqualTo(30);String cursor=first.get("page").get("nextCursor").asString();
  var second=parse(a.get("/me/notifications?cursor="+cursor));assertThat(second.get("items").size()).isEqualTo(5);assertThat(second.get("unreadCount").asInt()).isEqualTo(30);assertThat(b.get("/me/notifications?cursor="+cursor).statusCode()).isEqualTo(422);
 }

 String selectedBody(Browser a)throws Exception{
  var ids=new ArrayList<String>();for(var item:parse(a.get("/me/sessions")).get("items"))if(!item.get("current").asBoolean())ids.add(item.get("id").asString());return json.writeValueAsString(Map.of("sessionIds",ids));
 }
 String batchPreview(Browser a,String body)throws Exception{var r=a.post("/me/session-revocation-preview",body,null);assertThat(r.statusCode()).as(r.body()).isEqualTo(200);return parse(r).get("etag").asString();}
 HttpResponse<String> batchEnd(Browser a,String body,String tag,String key)throws Exception{return a.send("POST","/me/session-revocations",body,key,a.csrf,"http://localhost:3000",tag);}
 @Test void batchSessionRevocationIsAtomicScopedAndReplaySafe()throws Exception{
  Browser a=new Browser(),b=new Browser(),c=new Browser(),outsider=new Browser();String mobile="+919"+mobiles.incrementAndGet();a.login(mobile);b.login(mobile);c.login(mobile);outsider.login();String body=selectedBody(a),tag=batchPreview(a,body),key=UUID.randomUUID().toString();
  assertThat(outsider.post("/me/session-revocation-preview",body,null).statusCode()).isEqualTo(404);assertThat(batchEnd(a,body,null,key).statusCode()).isEqualTo(428);
  assertThat(a.send("POST","/me/session-revocations",body,key,"bad","http://localhost:3000",tag).statusCode()).isEqualTo(403);
  var result=batchEnd(a,body,tag,key);assertThat(result.statusCode()).as(result.body()).isEqualTo(200);assertThat(parse(result).get("endedCount").asInt()).isEqualTo(2);assertThat(b.get("/me/profile").statusCode()).isEqualTo(401);assertThat(c.get("/me/profile").statusCode()).isEqualTo(401);assertThat(a.get("/me/profile").statusCode()).isEqualTo(200);
  assertThat(batchEnd(a,body,tag,key).body()).isEqualTo(result.body());assertThat(batchEnd(a,body,tag+"changed",key).statusCode()).isEqualTo(409);
  assertThat(parse(a.get("/me/notifications")).get("totalMatching").asInt()).isEqualTo(1);
 }
 @Test void batchSessionRevocationRejectsCurrentDuplicateStaleAndOldAuthentication()throws Exception{
  Browser a=new Browser(),b=new Browser(),c=new Browser();String mobile="+919"+mobiles.incrementAndGet();a.login(mobile);b.login(mobile);c.login(mobile);String body=selectedBody(a),tag=batchPreview(a,body);var items=parse(a.get("/me/sessions")).get("items");String current="";for(var i:items)if(i.get("current").asBoolean())current=i.get("id").asString();
  assertThat(a.post("/me/session-revocation-preview",json.writeValueAsString(Map.of("sessionIds",List.of(current))),null).statusCode()).isEqualTo(409);
  var selected=json.readTree(body).get("sessionIds");String target=selected.get(0).asString();assertThat(a.post("/me/session-revocation-preview",json.writeValueAsString(Map.of("sessionIds",List.of(target,target))),null).statusCode()).isEqualTo(422);
  String actor=parse(a.get("/auth/session")).get("account").get("id").asString();sql("UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP-interval '10 minutes' WHERE id='"+current+"'");assertThat(batchEnd(a,body,tag,UUID.randomUUID().toString()).statusCode()).isEqualTo(403);sql("UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP WHERE id='"+current+"'");
  sql("UPDATE account_session SET version=version+1 WHERE id='"+target+"'");assertThat(batchEnd(a,body,tag,UUID.randomUUID().toString()).statusCode()).isEqualTo(412);assertThat(count("SELECT count(*) FROM account_session WHERE account_id='"+actor+"' AND revoked_at IS NOT NULL")).isZero();
 }
 @Test void batchSessionAuditRollbackAndCompetingCommandsPreserveUnselectedSessions()throws Exception{
  Browser a=new Browser(),b=new Browser(),c=new Browser(),later=new Browser();String mobile="+919"+mobiles.incrementAndGet();a.login(mobile);b.login(mobile);c.login(mobile);String body=selectedBody(a),tag=batchPreview(a,body),key=UUID.randomUUID().toString();later.login(mobile);
  sql("CREATE FUNCTION reject_batch_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='identity.sessions_revoked' THEN RAISE EXCEPTION 'isolated batch audit failure'; END IF; RETURN NEW; END $$; CREATE TRIGGER reject_batch_audit BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION reject_batch_audit()");
  try{assertThat(batchEnd(a,body,tag,key).statusCode()).isGreaterThanOrEqualTo(500);assertThat(b.get("/me/profile").statusCode()).isEqualTo(200);assertThat(c.get("/me/profile").statusCode()).isEqualTo(200);}finally{sql("DROP TRIGGER reject_batch_audit ON domain_audit; DROP FUNCTION reject_batch_audit()");}
  try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->batchEnd(a,body,tag,key));var y=pool.submit(()->batchEnd(a,body,tag,UUID.randomUUID().toString()));assertThat(List.of(x.get().statusCode(),y.get().statusCode())).containsExactlyInAnyOrder(200,412);}
  assertThat(later.get("/me/profile").statusCode()).isEqualTo(200);assertThat(a.get("/me/profile").statusCode()).isEqualTo(200);assertThat(parse(a.get("/me/notifications")).get("totalMatching").asInt()).isEqualTo(1);
 }

 @org.springframework.beans.factory.annotation.Autowired ChallengeService challengeService;
 @org.springframework.beans.factory.annotation.Autowired AccountControls accountControls;
 String recoveryCode(Browser b,String channel,String contact)throws Exception{b.token();var r=b.post("/auth/recovery/challenges",json.writeValueAsString(Map.of("channel",channel,"destination",contact)),UUID.randomUUID().toString());assertThat(r.statusCode()).as(r.body()).isEqualTo(202);return parse(r).get("id").asString();}
 HttpResponse<String> recover(Browser b,String id,String code)throws Exception{return b.post("/auth/recovery/verify",json.writeValueAsString(Map.of("challengeId",id,"code",code,"endExistingSessions",true)),null);}
 @Test void linkedEmailRecoveryPreservesAccountAndWorkspaceButEndsExistingSessionsAndCodes()throws Exception{
  Browser a=new Browser(),other=new Browser(),recovery=new Browser(),oldCode=new Browser(),oldRecovery=new Browser();String mobile="+919"+mobiles.incrementAndGet();a.login(mobile);other.login(mobile);String actor=parse(a.get("/auth/session")).get("account").get("id").asString(),w=workspace(a),email="Recover"+UUID.randomUUID()+"@example.test",tag=profileTag(a),linkId=linkChallenge(a,email,tag);assertThat(link(a,linkId,tag,UUID.randomUUID().toString(),otp(linkId)).statusCode()).isEqualTo(200);
  String pending=oldCode.challenge(mobile),old= recoveryCode(oldRecovery,"email",email),id=recoveryCode(recovery,"email",email),code=otp(id);
  assertThat(recovery.post("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",code)),null).statusCode()).isEqualTo(404);
  var result=recover(recovery,id,code);assertThat(result.statusCode()).as(result.body()).isEqualTo(200);assertThat(parse(result).get("account").get("id").asString()).isEqualTo(actor);assertThat(recovery.get("/workspaces/"+w+"/usage").statusCode()).isEqualTo(200);assertThat(a.get("/me/profile").statusCode()).isEqualTo(401);assertThat(other.get("/me/profile").statusCode()).isEqualTo(401);
  assertThat(oldCode.post("/auth/challenges/"+pending+"/verify",json.writeValueAsString(Map.of("code",otp(pending))),null).statusCode()).isEqualTo(409);assertThat(recover(oldRecovery,old,otp(old)).statusCode()).isEqualTo(409);
  assertThat(count("SELECT auth_generation FROM user_account WHERE id='"+actor+"'")).isEqualTo(2);assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='identity.account_recovered'")).isEqualTo(1);
 }
 @Test void recoveryDoesNotRegisterUnknownContactsAndProofIsSessionBound()throws Exception{
  Browser a=new Browser(),other=new Browser();String mobile="+919"+mobiles.incrementAndGet(),id=recoveryCode(a,"mobile",mobile),code=otp(id);other.token();assertThat(recover(other,id,code).statusCode()).isEqualTo(404);
  assertThat(a.post("/auth/recovery/verify",json.writeValueAsString(Map.of("challengeId",id,"code",code,"endExistingSessions",false)),null).statusCode()).isEqualTo(422);
  assertThat(recover(a,id,code.equals("000000")?"111111":"000000").statusCode()).isEqualTo(422);var denied=recover(a,id,code);assertThat(denied.statusCode()).isEqualTo(409);assertThat(parse(denied).get("code").asString()).isEqualTo("RECOVERY_UNAVAILABLE");assertThat(count("SELECT count(*) FROM user_account WHERE mobile='"+mobile+"'")).isZero();assertThat(recover(a,id,code).statusCode()).isEqualTo(409);
  String expired=recoveryCode(a,"mobile","+919"+mobiles.incrementAndGet()),expiredCode=otp(expired);sql("UPDATE auth_challenge SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+expired+"'");assertThat(recover(a,expired,expiredCode).statusCode()).isEqualTo(409);
 }
 @Test void recoveryAuditFailureRollsBackRevocationProofAndGeneration()throws Exception{
  Browser original=new Browser(),recovery=new Browser();String mobile="+919"+mobiles.incrementAndGet();original.login(mobile);String actor=parse(original.get("/auth/session")).get("account").get("id").asString(),id=recoveryCode(recovery,"mobile",mobile),code=otp(id);
  sql("CREATE FUNCTION reject_recovery_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='identity.account_recovered' THEN RAISE EXCEPTION 'isolated recovery audit failure'; END IF; RETURN NEW; END $$; CREATE TRIGGER reject_recovery_audit BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION reject_recovery_audit()");
  try{assertThat(recover(recovery,id,code).statusCode()).isGreaterThanOrEqualTo(500);assertThat(original.get("/me/profile").statusCode()).isEqualTo(200);assertThat(count("SELECT auth_generation FROM user_account WHERE id='"+actor+"'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM auth_challenge WHERE id='"+id+"' AND NOT consumed AND recovery_completed_at IS NULL")).isEqualTo(1);}finally{sql("DROP TRIGGER reject_recovery_audit ON domain_audit; DROP FUNCTION reject_recovery_audit()");}
  assertThat(recover(recovery,id,code).statusCode()).isEqualTo(200);
 }
 @Test void proofVerifiedBeforeRecoveryCannotRegisterAfterRecovery()throws Exception{
  Browser original=new Browser(),pending=new Browser(),recovery=new Browser();String mobile="+919"+mobiles.incrementAndGet();original.login(mobile);String id=pending.challenge(mobile),token=pending.cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("lmt_dev_session")).findFirst().orElseThrow().getValue();var proof=challengeService.verify(UUID.fromString(id),token,otp(id));String recoveryId=recoveryCode(recovery,"mobile",mobile);assertThat(recover(recovery,recoveryId,otp(recoveryId)).statusCode()).isEqualTo(200);
  assertThatThrownBy(()->accountControls.registerSession(proof.account().id(),"stale-session-token","Test",proof.generation())).isInstanceOf(in.lmt.shared.ApiProblem.class).hasMessageContaining("recovery changed");
  assertThat(count("SELECT count(*) FROM account_session WHERE token_hash='"+ChallengeService.hash("stale-session-token")+"'")).isZero();
 }

 JsonNode recoveryRequest(Browser b,String mobile,String key)throws Exception{b.token();var r=b.post("/auth/recovery/challenges",json.writeValueAsString(Map.of("channel","mobile","destination",mobile)),key);assertThat(r.statusCode()).as(r.body()).isEqualTo(202);return parse(r);}
 HttpResponse<String> outcome(Browser b,JsonNode challenge)throws Exception{return b.post("/auth/recovery/outcome",json.writeValueAsString(Map.of("challengeId",challenge.get("id").asString(),"outcomeToken",challenge.get("outcomeToken").asString())),null);}
 @Test void recoveryOutcomeGrantSurvivesLostCookieWithoutConferringAccountAccess()throws Exception{
  Browser original=new Browser(),recovering=new Browser(),reader=new Browser();String mobile="+919"+mobiles.incrementAndGet();original.login(mobile);String key=UUID.randomUUID().toString();var challenge=recoveryRequest(recovering,mobile,key);var same=recoveryRequest(recovering,mobile,key);assertThat(same.get("outcomeToken").asString()).isEqualTo(challenge.get("outcomeToken").asString());reader.token();
  assertThat(parse(outcome(reader,challenge)).get("status").asString()).isEqualTo("not_completed");String id=challenge.get("id").asString();
  var denied=reader.post("/auth/recovery/outcome",json.writeValueAsString(Map.of("challengeId",id,"outcomeToken","incorrect")),null);assertThat(denied.statusCode()).isEqualTo(404);
  assertThat(recover(recovering,id,otp(id)).statusCode()).isEqualTo(200);
  var result=outcome(reader,challenge);assertThat(result.statusCode()).isEqualTo(200);assertThat(result.headers().firstValue("cache-control")).contains("no-store");assertThat(parse(result).get("status").asString()).isEqualTo("completed");assertThat(parse(result).get("completedAt").isNull()).isFalse();assertThat(parse(result).size()).isEqualTo(3);
  assertThat(reader.get("/me/profile").statusCode()).isEqualTo(401);assertThat(outcome(reader,challenge).body()).isEqualTo(result.body());
  assertThat(count("SELECT count(*) FROM domain_audit WHERE action='identity.account_recovered' AND resource_id='"+id+"'")).isEqualTo(1);
  sql("UPDATE auth_challenge SET created_at=CURRENT_TIMESTAMP-interval '16 minutes' WHERE id='"+id+"'");assertThat(outcome(reader,challenge).statusCode()).isEqualTo(404);
 }
 @Test void recoveryOutcomeRejectsCrossChallengeAndReportsFailureWithoutInventingCompletion()throws Exception{
  Browser b=new Browser();var first=recoveryRequest(b,"+919"+mobiles.incrementAndGet(),UUID.randomUUID().toString());var second=recoveryRequest(b,"+919"+mobiles.incrementAndGet(),UUID.randomUUID().toString());String id=first.get("id").asString();
  assertThat(b.post("/auth/recovery/outcome",json.writeValueAsString(Map.of("challengeId",id,"outcomeToken",second.get("outcomeToken").asString())),null).statusCode()).isEqualTo(404);
  assertThat(recover(b,id,otp(id)).statusCode()).isEqualTo(409);var failed=parse(outcome(b,first));assertThat(failed.get("status").asString()).isEqualTo("failed");assertThat(failed.get("completedAt").isNull()).isTrue();
  sql("UPDATE auth_challenge SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+second.get("id").asString()+"'");assertThat(parse(outcome(b,second)).get("status").asString()).isEqualTo("expired");
  assertThatThrownBy(()->sql("UPDATE auth_challenge SET consumed=false,recovery_completed_at=CURRENT_TIMESTAMP WHERE id='"+id+"'")).isInstanceOf(SQLException.class);
 }

 void emailLogin(Browser b,String email)throws Exception{String id=emailChallenge(b,email,UUID.randomUUID().toString());assertThat(b.post("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",otp(id))),null).statusCode()).isEqualTo(200);b.token();}
 JsonNode emailInvite(Browser owner,String w,String email,String key)throws Exception{var r=owner.post("/workspaces/"+w+"/invitations",json.writeValueAsString(Map.of("email",email)),key);assertThat(r.statusCode()).as(r.body()).isEqualTo(201);return parse(owner.get("/workspaces/"+w+"/invitations/"+parse(r).get("resourceId").asString()));}
 @Test void emailInvitationRequiresExplicitAcceptanceAndPreservesMobileCompatibility()throws Exception{
  Browser owner=new Browser(),recipient=new Browser(),other=new Browser();owner.login();other.login();String w=workspace(owner),email="Invite"+UUID.randomUUID()+"@EXAMPLE.test",key=UUID.randomUUID().toString();var invitation=emailInvite(owner,w,email,key);assertThat(emailInvite(owner,w,email.replace("EXAMPLE","example"),key)).isEqualTo(invitation);assertThat(invitation.get("channel").asString()).isEqualTo("email");assertThat(invitation.get("maskedMobile").isNull()).isTrue();assertThat(invitation.get("maskedDestination").asString()).contains("@example.test");
  emailLogin(recipient,email);assertThat(recipient.get("/workspaces/"+w+"/membership").statusCode()).isEqualTo(404);assertThat(parse(recipient.get("/me/invitations")).get("totalMatching").asInt()).isEqualTo(1);assertThat(parse(other.get("/me/invitations")).get("totalMatching").asInt()).isZero();assertThat(respond(other,invitation,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(404);
  String actor=parse(other.get("/auth/session")).get("account").get("id").asString();try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var q=c.createStatement()){q.execute("SELECT set_config('lmt.account_id','"+actor+"',false)");try(var rows=q.executeQuery("SELECT count(*) FROM workspace_invitation WHERE id='"+invitation.get("id").asString()+"'")){rows.next();assertThat(rows.getLong(1)).isZero();}}
  String accepted=UUID.randomUUID().toString();assertThat(respond(recipient,invitation,"accept",accepted).statusCode()).isEqualTo(200);assertThat(respond(recipient,invitation,"accept",accepted).statusCode()).isEqualTo(200);assertThat(recipient.get("/workspaces/"+w+"/membership").statusCode()).isEqualTo(200);assertThat(recipient.get("/workspaces/"+w+"/members").statusCode()).isEqualTo(404);assertThat(parse(owner.get("/workspaces/"+w+"/members")).get("managersUsed").asInt()).isEqualTo(1);
 }
 @Test void emailAndMobileInvitationsCompeteForTheSameLastManagerSlot()throws Exception{
  Browser owner=new Browser(),emailUser=new Browser(),mobileUser=new Browser();owner.login();String w=workspace(owner),email="Quota"+UUID.randomUUID()+"@example.test",mobile="+919"+mobiles.incrementAndGet();emailLogin(emailUser,email);mobileUser.login(mobile);var e=emailInvite(owner,w,email,UUID.randomUUID().toString());var m=invite(owner,w,mobile);
  try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->respond(emailUser,e,"accept",UUID.randomUUID().toString()));var y=pool.submit(()->respond(mobileUser,m,"accept",UUID.randomUUID().toString()));assertThat(List.of(x.get().statusCode(),y.get().statusCode())).containsExactlyInAnyOrder(200,409);}
  assertThat(count("SELECT count(*) FROM workspace_member WHERE workspace_id='"+w+"' AND role='MANAGER' AND active")).isEqualTo(1);assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='managers'")).isEqualTo(1);
 }
 @Test void emailInvitationValidationExpiryAndRevocationDoNotGrantMembership()throws Exception{
  Browser owner=new Browser(),recipient=new Browser();owner.login();String w=workspace(owner),email="ExpiredInvite"+UUID.randomUUID()+"@example.test",uri="/workspaces/"+w+"/invitations";emailLogin(recipient,email);
  assertThat(owner.post(uri,"{}",UUID.randomUUID().toString()).statusCode()).isEqualTo(422);assertThat(owner.post(uri,json.writeValueAsString(Map.of("mobile","+919"+mobiles.incrementAndGet(),"email",email)),UUID.randomUUID().toString()).statusCode()).isEqualTo(422);assertThat(owner.post(uri,"{\"email\":\"not-an-email\"}",UUID.randomUUID().toString()).statusCode()).isEqualTo(422);
  var expired=emailInvite(owner,w,email,UUID.randomUUID().toString());assertThat(owner.post(uri,json.writeValueAsString(Map.of("email",email)),UUID.randomUUID().toString()).statusCode()).isEqualTo(409);String id=expired.get("id").asString();sql("UPDATE workspace_invitation SET created_at=CURRENT_TIMESTAMP-interval '2 days',expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+id+"'");assertThat(respond(recipient,expired,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(409);
  var revoked=emailInvite(owner,w,email,UUID.randomUUID().toString());assertThat(owner.send("POST",uri+"/"+revoked.get("id").asString()+"/revoke",null,UUID.randomUUID().toString(),owner.csrf,"http://localhost:3000",revoked.get("etag").asString()).statusCode()).isEqualTo(200);assertThat(respond(recipient,revoked,"accept",UUID.randomUUID().toString()).statusCode()).isEqualTo(412);assertThat(recipient.get("/workspaces/"+w+"/membership").statusCode()).isEqualTo(404);
  assertThatThrownBy(()->sql("UPDATE workspace_invitation SET mobile='+919123456789' WHERE id='"+id+"'")).isInstanceOf(SQLException.class);
 }


 HttpResponse<String> unlink(Browser b,String channel,String tag,String key)throws Exception{return b.send("POST","/me/identity-links/unlink",json.writeValueAsString(Map.of("channel",channel)),key,b.csrf,"http://localhost:3000",tag);}
 @Test void unlinkRequiresRetainedContactProofAndPreservesReplayAudit()throws Exception{
  Browser a=new Browser();a.login();String actor=parse(a.get("/auth/session")).get("account").get("id").asString();String tag=profileTag(a);
  assertThat(unlink(a,"mobile",tag,UUID.randomUUID().toString()).statusCode()).isEqualTo(409);
  String email="Unlink"+UUID.randomUUID()+"@example.test",id=linkChallenge(a,email,tag);assertThat(link(a,id,tag,UUID.randomUUID().toString(),otp(id)).statusCode()).isEqualTo(200);tag=profileTag(a);
  assertThat(unlink(a,"email",tag,UUID.randomUUID().toString()).statusCode()).isEqualTo(403);
  String wrong=reauthentication(a,"email",tag);assertThat(reverify(a,wrong,otp(wrong),tag).statusCode()).isEqualTo(200);a.token();
  assertThat(unlink(a,"email",tag,UUID.randomUUID().toString()).statusCode()).isEqualTo(403);
  String proof=reauthentication(a,"mobile",tag);assertThat(reverify(a,proof,otp(proof),tag).statusCode()).isEqualTo(200);a.token();
  assertThat(unlink(a,"email",null,UUID.randomUUID().toString()).statusCode()).isEqualTo(428);
  assertThat(unlink(a,"email","\"stale\"",UUID.randomUUID().toString()).statusCode()).isEqualTo(412);
  String pending=linkChallengeForSignIn(email);String key=UUID.randomUUID().toString();var result=unlink(a,"email",tag,key);assertThat(result.statusCode()).as(result.body()).isEqualTo(200);assertThat(parse(result).get("maskedEmail").isNull()).isTrue();
  assertThat(unlink(a,"email",tag,key).body()).isEqualTo(result.body());assertThat(unlink(a,"mobile",tag,key).statusCode()).isEqualTo(409);
  assertThat(count("SELECT count(*) FROM auth_challenge WHERE id='"+pending+"' AND consumed")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='identity.contact_unlinked'")).isEqualTo(1);
  assertThat(unlink(a,"mobile",profileTag(a),UUID.randomUUID().toString()).statusCode()).isEqualTo(409);
  Browser other=new Browser();other.login();assertThat(unlink(other,"email",tag,UUID.randomUUID().toString()).statusCode()).isEqualTo(412);
 }
 String linkChallengeForSignIn(String email)throws Exception{Browser b=new Browser();b.token();var r=b.post("/auth/challenges",json.writeValueAsString(Map.of("channel","email","destination",email,"purpose","sign_in","displayName","Existing account")),UUID.randomUUID().toString());assertThat(r.statusCode()).isEqualTo(202);return parse(r).get("id").asString();}

 @Test void opposingContactRemovalsHaveOneWinnerAndNeverRemoveLastContact()throws Exception{
  String mobile="+919"+mobiles.incrementAndGet();Browser a=new Browser(),b=new Browser();a.login(mobile);
  String tag=profileTag(a),id=linkChallenge(a,"Race"+UUID.randomUUID()+"@example.test",tag);assertThat(link(a,id,tag,UUID.randomUUID().toString(),otp(id)).statusCode()).isEqualTo(200);b.login(mobile);String current=profileTag(a);
  String pa=reauthentication(a,"mobile",current);assertThat(reverify(a,pa,otp(pa),current).statusCode()).isEqualTo(200);a.token();String pb=reauthentication(b,"email",current);assertThat(reverify(b,pb,otp(pb),current).statusCode()).isEqualTo(200);b.token();
  String actor=parse(a.get("/auth/session")).get("account").get("id").asString();
  try(var pool=Executors.newFixedThreadPool(2)){
   var gate=new CountDownLatch(1);
   var first=pool.submit(()->{gate.await();return unlink(a,"email",current,UUID.randomUUID().toString()).statusCode();});
   var second=pool.submit(()->{gate.await();return unlink(b,"mobile",current,UUID.randomUUID().toString()).statusCode();});gate.countDown();
   var statuses=List.of(first.get(15,TimeUnit.SECONDS),second.get(15,TimeUnit.SECONDS));assertThat(statuses).contains(200);assertThat(statuses.stream().filter(v->v==200).count()).isEqualTo(1);assertThat(statuses).allMatch(v->Set.of(200,401,403,412).contains(v));
  }
  assertThat(count("SELECT count(*) FROM user_account WHERE id='"+actor+"' AND ((mobile IS NULL) <> (email IS NULL))")).isEqualTo(1);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE actor_id='"+actor+"' AND action='identity.contact_unlinked'")).isEqualTo(1);
 }

 @Test void notificationFiltersAndNewestFirstPaginationAreScopedAndStable()throws Exception{
  Browser a=new Browser(),b=new Browser();a.login();b.login();String actor=parse(a.get("/auth/session")).get("account").get("id").asString();
  sql("INSERT INTO account_notification(id,account_id,message,occurred_at) SELECT gen_random_uuid(),'"+actor+"','Older notice '||n,'2020-01-01T00:00:00Z'::timestamptz FROM generate_series(1,30) n");
  String latest=UUID.randomUUID().toString();sql("INSERT INTO account_notification(id,account_id,message,occurred_at) VALUES('"+latest+"','"+actor+"','Newest notice','2021-01-01T00:00:00Z')");
  var first=parse(a.get("/me/notifications?status=unread"));assertThat(first.get("items").get(0).get("id").asString()).isEqualTo(latest);assertThat(first.get("totalMatching").asInt()).isEqualTo(31);
  String cursor=first.get("page").get("nextCursor").asString();
  assertThat(a.get("/me/notifications?status=read&cursor="+cursor).statusCode()).isEqualTo(422);assertThat(b.get("/me/notifications?status=unread&cursor="+cursor).statusCode()).isEqualTo(422);assertThat(a.get("/me/notifications?status=unknown").statusCode()).isEqualTo(422);
  assertThat(readNotice(a,latest,first.get("items").get(0).get("etag").asString(),UUID.randomUUID().toString()).statusCode()).isEqualTo(200);
  var second=parse(a.get("/me/notifications?status=unread&cursor="+cursor));assertThat(second.get("items").size()).isEqualTo(6);assertThat(second.get("totalMatching").asInt()).isEqualTo(30);assertThat(second.get("unreadCount").asInt()).isEqualTo(30);
  Set<String> ids=new HashSet<>();for(var item:first.get("items"))assertThat(ids.add(item.get("id").asString())).isTrue();for(var item:second.get("items"))assertThat(ids.add(item.get("id").asString())).isTrue();assertThat(ids).hasSize(31);
  var read=parse(a.get("/me/security-notifications?status=read"));assertThat(read.get("totalMatching").asInt()).isEqualTo(1);assertThat(read.get("unreadCount").asInt()).isEqualTo(30);assertThat(read.get("items").get(0).get("id").asString()).isEqualTo(latest);
  assertThat(parse(b.get("/me/notifications?status=all")).get("totalMatching").asInt()).isZero();
 }

 @Test void notificationPreferencesAreVersionedScopedAndIdempotent()throws Exception{
  Browser a=new Browser(),b=new Browser();assertThat(a.get("/me/notification-preferences").statusCode()).isEqualTo(401);a.login();b.login();
  String path="/me/notification-preferences",tag=parse(a.get(path)).get("etag").asString(),key=UUID.randomUUID().toString(),body="{\"workspaceSetupEnabled\":true}";
  assertThat(parse(a.get(path)).get("workspaceSetupEnabled").asBoolean()).isFalse();
  assertThat(a.send("PATCH",path,body,key,a.csrf,"http://localhost:3000",null).statusCode()).isEqualTo(428);
  assertThat(b.send("PATCH",path,body,key,b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(412);
  var saved=a.send("PATCH",path,body,key,a.csrf,"http://localhost:3000",tag);assertThat(saved.statusCode()).as(saved.body()).isEqualTo(200);assertThat(parse(saved).get("workspaceSetupEnabled").asBoolean()).isTrue();
  assertThat(a.send("PATCH",path,body,key,a.csrf,"http://localhost:3000",tag).body()).isEqualTo(saved.body());
  assertThat(a.send("PATCH",path,"{\"workspaceSetupEnabled\":false}",key,a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(409);
  assertThat(a.send("PATCH",path,body,UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(412);
  assertThat(parse(b.get(path)).get("workspaceSetupEnabled").asBoolean()).isFalse();assertThat(parse(a.get("/me/notifications")).get("totalMatching").asInt()).isEqualTo(1);
  assertThat(a.send("PATCH",path,"{}",UUID.randomUUID().toString(),a.csrf,"http://localhost:3000",parse(saved).get("etag").asString()).statusCode()).isEqualTo(422);
 }

 @Test void quietPreferencesValidateAndLegacyUpdatesPreserveSchedule()throws Exception{
  Browser b=new Browser();b.login();String path="/me/notification-preferences",tag=parse(b.get(path)).get("etag").asString();
  for(String body:List.of("{\"workspaceSetupEnabled\":true,\"quietHoursEnabled\":true}","{\"workspaceSetupEnabled\":true,\"quietHoursEnabled\":true,\"quietStart\":\"22:00\",\"quietEnd\":\"22:00\"}","{\"workspaceSetupEnabled\":true,\"quietHoursEnabled\":true,\"quietStart\":\"24:00\",\"quietEnd\":\"07:00\"}"))assertThat(b.send("PATCH",path,body,UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",tag).statusCode()).isEqualTo(422);
  String body="{\"workspaceSetupEnabled\":true,\"quietHoursEnabled\":true,\"quietStart\":\"23:00\",\"quietEnd\":\"08:00\"}",key=UUID.randomUUID().toString();var saved=b.send("PATCH",path,body,key,b.csrf,"http://localhost:3000",tag);assertThat(saved.statusCode()).as(saved.body()).isEqualTo(200);assertThat(b.send("PATCH",path,body,key,b.csrf,"http://localhost:3000",tag).body()).isEqualTo(saved.body());
  var legacy=b.send("PATCH",path,"{\"workspaceSetupEnabled\":true}",UUID.randomUUID().toString(),b.csrf,"http://localhost:3000",parse(saved).get("etag").asString());assertThat(legacy.statusCode()).isEqualTo(200);assertThat(parse(legacy).get("quietHoursEnabled").asBoolean()).isTrue();assertThat(parse(legacy).get("quietStart").asString()).isEqualTo("23:00");
 }
}
