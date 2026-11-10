package in.lmt.commercial;
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
class EntitlementEvaluationIntegrationTest {
 static final PostgreSQLContainer db=new PostgreSQLContainer("postgres:17.11");static final AtomicInteger mobiles=new AtomicInteger(300000000);static final Path inbox;
 static{try{inbox=Files.createTempDirectory("lmt-operation-inbox-");db.start();try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");}}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 @DynamicPropertySource static void config(DynamicPropertyRegistry r){r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",()->"lmt_runtime");r.add("spring.datasource.password",()->"runtime_test");r.add("spring.flyway.user",db::getUsername);r.add("spring.flyway.password",db::getPassword);r.add("lmt.identity.sandbox-secret",()->"test-operation-secret-longer-than-32-characters");r.add("lmt.identity.inbox",inbox::toString);r.add("lmt.identity.delivery-delay-ms",()->"25");r.add("lmt.workspace.initial-delay-ms",()->"3600000");}
 @LocalServerPort int port;
 final ObjectMapper json=new ObjectMapper();
 class Browser {
  final HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();String csrf;
  HttpResponse<String> send(String path,String body,String key,String tag)throws Exception{
   var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1"+path.replace("?edit", "")));
   if(body==null)b.GET();else{b.method(path.endsWith("?edit")?"PATCH":"POST",HttpRequest.BodyPublishers.ofString(body)).header("Content-Type","application/json").header("Origin","http://localhost:3000").header("X-CSRF-Token",csrf);if(key!=null)b.header("Idempotency-Key",key);if(tag!=null)b.header("If-Match",tag);}
   return client.send(b.build(),HttpResponse.BodyHandlers.ofString());
  }
  JsonNode get(String path)throws Exception{var r=send(path,null,null,null);assertThat(r.statusCode()).as(r.body()).isEqualTo(200);return json.readTree(r.body());}
  void login()throws Exception{csrf=get("/auth/csrf").get("token").asString();var c=send("/auth/challenges",json.writeValueAsString(Map.of("channel","mobile","destination","+919"+mobiles.incrementAndGet(),"purpose","sign_in","displayName","Operation Owner")),UUID.randomUUID().toString(),null);assertThat(c.statusCode()).as(c.body()).isEqualTo(202);String id=json.readTree(c.body()).get("id").asString();String code=null;for(int n=0;n<100;n++){var f=inbox.resolve(id+".txt");if(Files.exists(f)&&Files.size(f)==6){code=Files.readString(f);break;}Thread.sleep(25);}assertThat(send("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",code)),null,null).statusCode()).isEqualTo(200);csrf=get("/auth/csrf").get("token").asString();}
  UUID provision()throws Exception{var plan=get("/public/plans/FREE");var r=send("/workspaces",json.writeValueAsString(Map.of("profile",Map.of("name","Operations test","customerType","individual","city","Pune","state","Maharashtra"),"selectedPlanVersionId",plan.get("versionId").asString(),"consentIds",List.of("sandbox-use-v1"))),UUID.randomUUID().toString(),null);assertThat(r.statusCode()).as(r.body()).isEqualTo(201);return UUID.fromString(json.readTree(r.body()).get("id").asString());}
 }
 void sql(String sql)throws Exception{try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute(sql);}}
 long count(String sql)throws Exception{try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getLong(1);}}

 UUID input(UUID w,String code,String kind,String effect,String starts,String ends)throws Exception{UUID id=UUID.randomUUID();sql("INSERT INTO workspace_entitlement_input(id,workspace_id,policy_version,capability,kind,effect,plan_version_id,starts_at,expires_at,mode,reason,created_by) SELECT '"+id+"','"+w+"',3,'"+code+"','"+kind+"','"+effect+"',plan_version_id,"+starts+","+ends+",'sandbox','Integration test configuration','test' FROM workspace_subscription WHERE workspace_id='"+w+"'");return id;}
 JsonNode decision(Browser b,UUID w,String code)throws Exception{return b.get("/workspaces/"+w+"/entitlements/"+code).get("decision");}
 void revoke(UUID w,UUID id)throws Exception{sql("INSERT INTO workspace_entitlement_revocation(input_id,workspace_id,reason,created_by) VALUES('"+id+"','"+w+"','Test revocation','test')");}
 @Test void safeSnapshotRefreshesAndNeverGrantsBillingAuthorityToManagers()throws Exception{
  Browser owner=new Browser(),manager=new Browser(),other=new Browser();owner.login();manager.login();other.login();UUID w=owner.provision();String actor=manager.get("/auth/session").get("account").get("id").asString();
  sql("INSERT INTO workspace_member(workspace_id,account_id,role) VALUES('"+w+"','"+actor+"','MANAGER')");String path="/workspaces/"+w+"/entitlements/snapshot";
  var response=owner.send(path,null,null,null);assertThat(response.statusCode()).as(response.body()).isEqualTo(200);assertThat(response.headers().firstValue("Cache-Control").orElse("")).contains("no-store");
  var first=json.readTree(response.body());assertThat(first.get("canManageBilling").asBoolean()).isTrue();assertThat(first.get("quotas").size()).isGreaterThan(0);
  var member=manager.get(path);assertThat(member.get("canManageBilling").asBoolean()).isFalse();for(var f:member.get("features"))assertThat(f.get("upgradeAvailable").asBoolean()).isFalse();
  assertThat(other.send(path,null,null,null).statusCode()).isEqualTo(404);
  UUID deny=input(w,"PROPERTY_CREATE","commercial_override","deny","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");
  var restricted=owner.get(path);for(var f:restricted.get("features"))if(f.get("code").asString().equals("PROPERTY_CREATE")){assertThat(f.get("eligible").asBoolean()).isFalse();assertThat(f.get("reason").asString()).isEqualTo("COMMERCIAL_OVERRIDE_DENIED");assertThat(f.get("upgradeAvailable").asBoolean()).isFalse();}
  revoke(w,deny);for(var f:owner.get(path).get("features"))if(f.get("code").asString().equals("PROPERTY_CREATE"))assertThat(f.get("eligible").asBoolean()).isTrue();
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"' AND account_id='"+actor+"'");assertThat(manager.send(path,null,null,null).statusCode()).isEqualTo(404);
 }
 @Test void layersUseScopedPlanContractsAndDenyWinsWithoutEnablingUnavailableFeatures()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision(),plan=UUID.randomUUID();String original=b.get("/workspaces/"+w+"/subscription").get("planVersionId").asString();
  sql("INSERT INTO plan_version(version_id,audience,code,version_number,display_name,publication_state,effective_at,baseline_only,rentable_limit,manager_limit,monthly_amount,annual_amount,currency,commercial_disclosure) SELECT '"+plan+"',audience,'TEST_LAYER',1,'Test contract','published',CURRENT_TIMESTAMP,true,rentable_limit,manager_limit,0,0,currency,'Test-only entitlement contract' FROM plan_version WHERE version_id='"+original+"'");sql("UPDATE workspace_subscription SET plan_version_id='"+plan+"' WHERE workspace_id='"+w+"'");
  sql("INSERT INTO subscription_upgrade_offer(id,source_plan_id,target_plan_id,mode,base_amount,tax_rate,interval_months,proration,recipient,disclosure) VALUES(gen_random_uuid(),'"+plan+"','"+original+"','sandbox',100,18,1,'none_free_only','Local test merchant','Local test only')");
  for(var f:b.get("/workspaces/"+w+"/entitlements/snapshot").get("features"))if(f.get("code").asString().equals("WORKSPACE_MEMBER_ADD")){assertThat(f.get("upgradeAvailable").asBoolean()).isTrue();assertThat(f.get("requiredPlans").size()).isEqualTo(1);}
  assertThat(decision(b,w,"WORKSPACE_MEMBER_ADD").get("eligible").asBoolean()).isFalse();UUID addon=input(w,"WORKSPACE_MEMBER_ADD","add_on","allow","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");assertThat(decision(b,w,"WORKSPACE_MEMBER_ADD").get("source").asString()).isEqualTo("add_on");UUID grant=input(w,"WORKSPACE_MEMBER_ADD","timed_grant","allow","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");revoke(w,addon);assertThat(decision(b,w,"WORKSPACE_MEMBER_ADD").get("source").asString()).isEqualTo("timed_grant");
  UUID deny=input(w,"WORKSPACE_MEMBER_ADD","commercial_override","deny","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");input(w,"WORKSPACE_MEMBER_ADD","commercial_override","allow","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");assertThat(decision(b,w,"WORKSPACE_MEMBER_ADD").get("reason").asString()).isEqualTo("COMMERCIAL_OVERRIDE_DENIED");revoke(w,deny);assertThat(decision(b,w,"WORKSPACE_MEMBER_ADD").get("eligible").asBoolean()).isTrue();
  input(w,"AI_CHATBOT","add_on","allow","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");assertThat(decision(b,w,"AI_CHATBOT").get("reason").asString()).isEqualTo("FEATURE_UNAVAILABLE");sql("UPDATE workspace SET status='restricted' WHERE id='"+w+"'");assertThat(decision(b,w,"WORKSPACE_MEMBER_ADD").get("reason").asString()).isEqualTo("WORKSPACE_RESTRICTED");sql("UPDATE workspace SET status='suspended' WHERE id='"+w+"'");assertThat(decision(b,w,"WORKSPACE_MEMBER_ADD").get("reason").asString()).isEqualTo("SECURITY_SUSPENDED");assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+w+"' AND action='entitlement.input_configured'")).isEqualTo(5);
 }
 @Test void timeBoundsRevocationAndWorkspaceScopeAreAuthoritative()throws Exception{
  Browser b=new Browser(),other=new Browser();b.login();other.login();UUID w=b.provision();String path="/workspaces/"+w+"/entitlements/PROPERTY_CREATE";assertThat(other.send(path,null,null,null).statusCode()).isEqualTo(404);assertThat(b.send("/workspaces/"+w+"/entitlements/UNKNOWN_TEST",null,null,null).statusCode()).isEqualTo(422);
  input(w,"PROPERTY_CREATE","commercial_override","deny","CURRENT_TIMESTAMP+interval '1 hour'","CURRENT_TIMESTAMP+interval '2 hours'");input(w,"PROPERTY_CREATE","commercial_override","deny","CURRENT_TIMESTAMP-interval '2 hours'","CURRENT_TIMESTAMP-interval '1 hour'");assertThat(decision(b,w,"PROPERTY_CREATE").get("eligible").asBoolean()).isTrue();
  UUID id=input(w,"PROPERTY_CREATE","commercial_override","deny","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 second'");assertThat(decision(b,w,"PROPERTY_CREATE").get("eligible").asBoolean()).isFalse();Thread.sleep(1100);assertThat(decision(b,w,"PROPERTY_CREATE").get("eligible").asBoolean()).isTrue();UUID next=input(w,"PROPERTY_CREATE","commercial_override","deny","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");revoke(w,next);assertThat(decision(b,w,"PROPERTY_CREATE").get("eligible").asBoolean()).isTrue();
  sql("UPDATE workspace SET mode='live' WHERE id='"+w+"'");assertThat(decision(b,w,"PROPERTY_CREATE").get("contributions").size()).isZero();sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");assertThat(b.send(path,null,null,null).statusCode()).isEqualTo(404);
 }
 @Test void actualPropertyMutationIsGatedWhileCorrectionsAndResourceAuthorityStaySeparate()throws Exception{
  Browser b=new Browser(),manager=new Browser();b.login();manager.login();UUID w=b.provision();String actor=manager.get("/auth/session").get("account").get("id").asString();sql("INSERT INTO workspace_member(workspace_id,account_id,role) VALUES('"+w+"','"+actor+"','MANAGER')");assertThat(decision(manager,w,"PROPERTY_MANAGEMENT").get("eligible").asBoolean()).isTrue();
  String body="{\"code\":\"ENT\",\"name\":\"Entitlement test\",\"addressLine\":\"10 Main Road\",\"locality\":\"\",\"city\":\"Pune\",\"state\":\"Maharashtra\",\"pin\":\"411038\",\"latitude\":null,\"longitude\":null,\"houseRules\":\"\"}";String root="/workspaces/"+w+"/properties";assertThat(manager.send(root,body,UUID.randomUUID().toString(),null).statusCode()).isEqualTo(404);
  UUID deny=input(w,"PROPERTY_MANAGEMENT","commercial_override","deny","CURRENT_TIMESTAMP-interval '1 minute'","CURRENT_TIMESTAMP+interval '1 hour'");assertThat(decision(b,w,"PROPERTY_CREATE").get("reason").asString()).isEqualTo("PARENT_CAPABILITY_DENIED");assertThat(b.send(root,body,UUID.randomUUID().toString(),null).statusCode()).isEqualTo(409);assertThat(decision(b,w,"PROPERTY_EDIT").get("eligible").asBoolean()).isTrue();assertThatThrownBy(()->input(w,"PROPERTY_EDIT","commercial_override","deny","CURRENT_TIMESTAMP","CURRENT_TIMESTAMP+interval '1 hour'")).isInstanceOf(SQLException.class);
  sql("INSERT INTO workspace_entitlement_input(id,workspace_id,policy_version,capability,kind,effect,plan_version_id,starts_at,expires_at,mode,reason,created_by) SELECT gen_random_uuid(),'"+w+"',2,'PROPERTY_EDIT','commercial_override','deny',plan_version_id,CURRENT_TIMESTAMP-interval '1 minute',CURRENT_TIMESTAMP+interval '1 hour','sandbox','Legacy-policy protection test','test' FROM workspace_subscription WHERE workspace_id='"+w+"'");assertThat(decision(b,w,"PROPERTY_EDIT").get("eligible").asBoolean()).isTrue();
  revoke(w,deny);assertThat(b.send(root,body,UUID.randomUUID().toString(),null).statusCode()).isEqualTo(201);assertThat(count("SELECT count(*) FROM property_record WHERE workspace_id='"+w+"'")).isEqualTo(1);
 }
}
