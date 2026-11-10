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
class SubscriptionIntegrationTest {
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
 @Test void dashboardUsesContractAndReturnsHonestCoverageWithEffectiveGrants()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String path="/workspaces/"+w+"/subscription";var response=b.send(path,null,null,null);assertThat(response.statusCode()).as(response.body()).isEqualTo(200);assertThat(response.headers().firstValue("Cache-Control").orElse("")).contains("no-store");var d=json.readTree(response.body());
  assertThat(d.get("planCode").asString()).isEqualTo("FREE");assertThat(d.get("subscriptionStatus").asString()).isEqualTo("active_free");assertThat(d.get("startedAt").isNull()).isFalse();assertThat(d.get("billingPeriod").get("status").asString()).isEqualTo("not_applicable");assertThat(d.get("billingPeriod").get("start").isNull()).isTrue();assertThat(d.get("nextRenewal").get("at").isNull()).isTrue();assertThat(d.get("nextInvoiceEstimate").get("amount").isNull()).isTrue();
  for(String field:List.of("activeAddOns","paymentHistory","upgradeAvailability"))assertThat(d.get(field).get("status").asString()).isEqualTo("unavailable");for(var c:d.get("communicationUsage")){assertThat(c.get("used").isNull()).isTrue();assertThat(c.get("allowance").isNull()).isTrue();}for(var u:d.get("upgrades")){assertThat(u.get("available").asBoolean()).isFalse();assertThat(u.get("code").asString()).doesNotStartWith("TENANT_");}
  var usage=b.get("/workspaces/"+w+"/usage");assertThat(d.get("quotas")).isEqualTo(usage.get("quotas"));assertThat(d.get("entitlements")).isEqualTo(usage.get("capabilities"));
  String contracted=d.get("planVersionId").asString();UUID newer=UUID.randomUUID();
  sql("INSERT INTO plan_version(version_id,audience,code,version_number,display_name,publication_state,effective_at,baseline_only,rentable_limit,manager_limit,monthly_amount,annual_amount,currency,commercial_disclosure) SELECT '"+newer+"',audience,code,99,'New catalogue name','published',CURRENT_TIMESTAMP,baseline_only,rentable_limit,manager_limit,monthly_amount,annual_amount,currency,commercial_disclosure FROM plan_version WHERE version_id='"+contracted+"'");
  assertThat(b.get("/public/plans/FREE").get("versionId").asString()).isEqualTo(newer.toString());var retained=b.get(path);assertThat(retained.get("planVersionId").asString()).isEqualTo(contracted);assertThat(retained.get("planName").asString()).isEqualTo(d.get("planName").asString());
  sql("UPDATE workspace SET status='restricted' WHERE id='"+w+"'");var restricted=b.get(path);assertThat(restricted.get("workspaceStatus").asString()).isEqualTo("restricted");assertThat(restricted.get("entitlements").toString()).contains("WORKSPACE_RESTRICTED");
 }
 @Test void anonymousOtherManagerAndRevokedOwnerCannotReadSubscription()throws Exception{
  Browser owner=new Browser(),other=new Browser(),anonymous=new Browser();owner.login();other.login();UUID w=owner.provision();String path="/workspaces/"+w+"/subscription";
  assertThat(anonymous.send(path,null,null,null).statusCode()).isEqualTo(401);assertThat(other.send(path,null,null,null).statusCode()).isEqualTo(404);String actor=other.get("/auth/session").get("account").get("id").asString();
  sql("INSERT INTO workspace_member(workspace_id,account_id,role) VALUES('"+w+"','"+actor+"','MANAGER')");assertThat(other.send(path,null,null,null).statusCode()).isEqualTo(404);
  assertThat(owner.send("/workspaces/"+UUID.randomUUID()+"/subscription",null,null,null).statusCode()).isEqualTo(404);sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"' AND role='OWNER'");assertThat(owner.send(path,null,null,null).statusCode()).isEqualTo(404);
 }
}
