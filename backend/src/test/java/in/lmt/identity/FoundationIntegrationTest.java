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
  for(var q:parse(before).get("quotas"))if(q.get("resource").asString().equals("rentable_items")){assertThat(q.get("available").asBoolean()).isFalse();assertThat(q.get("used").isNull()).isTrue();}
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
}
