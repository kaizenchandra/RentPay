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
class CommunicationAllowancesIntegrationTest {
 static final PostgreSQLContainer db=new PostgreSQLContainer("postgres:17.11");static final AtomicInteger mobiles=new AtomicInteger(300000000);static final Path inbox;
 static{try{inbox=Files.createTempDirectory("lmt-operation-inbox-");db.start();try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");}}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 @DynamicPropertySource static void config(DynamicPropertyRegistry r){r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",()->"lmt_runtime");r.add("spring.datasource.password",()->"runtime_test");r.add("spring.flyway.user",db::getUsername);r.add("spring.flyway.password",db::getPassword);r.add("lmt.identity.sandbox-secret",()->"test-operation-secret-longer-than-32-characters");r.add("lmt.identity.inbox",inbox::toString);r.add("lmt.identity.delivery-delay-ms",()->"25");r.add("lmt.workspace.initial-delay-ms",()->"3600000");r.add("lmt.communication.local-enabled",()->"true");r.add("lmt.communication.initial-delay-ms",()->"3600000");}
 @Autowired CommunicationAllowances service;
 @Autowired CommunicationReconciliation reconciliation;
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


 UUID contract(UUID w,int allowance)throws Exception{UUID id=UUID.randomUUID();sql("INSERT INTO communication_contract(id,workspace_id,plan_version_id,channel,metric,allowance,starts_at,ends_at,mode,reason) SELECT '"+id+"',workspace_id,plan_version_id,'sms','provider_accepted',"+allowance+",CURRENT_TIMESTAMP-interval '1 hour',CURRENT_TIMESTAMP+interval '1 hour','sandbox','Explicit test contract' FROM workspace_subscription WHERE workspace_id='"+w+"'");return id;}
 String body(Browser b,UUID w,UUID c)throws Exception{return json.writeValueAsString(Map.of("contractId",c.toString(),"contactVersion",b.get("/workspaces/"+w+"/usage/communications").get("contactVersion").asInt()));}
 JsonNode channel(Browser b,UUID w)throws Exception{return b.get("/workspaces/"+w+"/usage/communications").get("channels").get(1);}
 @Test void lastAllowanceReservationReplayAndDuplicateReceiptMeterExactlyOnce()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision(),c=contract(w,1);String path="/workspaces/"+w+"/communications/tests",body=body(b,w,c);String key=UUID.randomUUID().toString(),otherKey=UUID.randomUUID().toString();String[] replay=new String[2];
  try(var pool=Executors.newFixedThreadPool(2)){var first=pool.submit(()->b.send(path,body,key,null));var second=pool.submit(()->b.send(path,body,otherKey,null));var a=first.get();var z=second.get();assertThat(List.of(a.statusCode(),z.statusCode())).containsExactlyInAnyOrder(202,409);replay[0]=a.statusCode()==202?key:otherKey;replay[1]=a.statusCode()==202?a.body():z.body();}
  assertThat(channel(b,w).get("used").asInt()).isZero();assertThat(channel(b,w).get("reserved").asInt()).isEqualTo(1);
  var claim=service.claim().getFirst();service.prepare(claim);service.finish(claim,"unknown",null,"PROVIDER_OUTCOME_UNKNOWN");assertThat(channel(b,w).get("reserved").asInt()).isEqualTo(1);assertThat(service.claim()).isEmpty();
  service.finish(claim,"accepted","local-test-receipt",null);service.finish(claim,"accepted","local-test-receipt",null);assertThat(b.send(path,body,replay[0],null).body()).isEqualTo(replay[1]);assertThat(channel(b,w).get("used").asInt()).isEqualTo(1);assertThat(channel(b,w).get("reserved").asInt()).isZero();
  assertThat(count("SELECT count(*) FROM communication_usage_event WHERE workspace_id='"+w+"' AND metric='provider_accepted'")).isEqualTo(1);assertThat(count("SELECT count(*) FROM communication_usage_event WHERE workspace_id='"+w+"' AND metric='dispatch_authorized'")).isEqualTo(1);
  var unknown=b.get("/workspaces/"+w+"/usage/communications").get("channels").get(2);assertThat(unknown.get("used").isNull()).isTrue();assertThat(unknown.get("available").asBoolean()).isFalse();
 }
 @Test void changedBodyRevocationAndPeriodRulesCannotBypassMetering()throws Exception{
  Browser b=new Browser(),other=new Browser();b.login();other.login();UUID w=b.provision(),c=contract(w,2);String path="/workspaces/"+w+"/communications/tests",body=body(b,w,c),key=UUID.randomUUID().toString();var first=b.send(path,body,key,null);assertThat(first.statusCode()).as(first.body()).isEqualTo(202);assertThat(b.send(path,body,key,null).body()).isEqualTo(first.body());assertThat(b.send(path,body.replace(c.toString(),UUID.randomUUID().toString()),key,null).statusCode()).isEqualTo(409);assertThat(other.send(path,body,UUID.randomUUID().toString(),null).statusCode()).isEqualTo(404);
  assertThatThrownBy(()->contract(w,3)).isInstanceOf(SQLException.class);assertThatThrownBy(()->sql("UPDATE communication_contract SET metric='attempted' WHERE id='"+c+"'")).isInstanceOf(SQLException.class);
  var claim=service.claim().getFirst();sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");assertThatThrownBy(()->service.prepare(claim)).isInstanceOf(in.lmt.shared.ApiProblem.class);service.finish(claim,"cancelled",null,"AUTHORITY_REVOKED");assertThat(count("SELECT count(*) FROM communication_usage_event WHERE workspace_id='"+w+"'")).isZero();assertThat(b.send("/workspaces/"+w+"/usage/communications",null,null,null).statusCode()).isEqualTo(404);
 }
 @Test void expiredDispatchHoldsReservationAndFailedProviderDoesNotConsume()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision(),c=contract(w,1);String path="/workspaces/"+w+"/communications/tests",body=body(b,w,c);assertThat(b.send(path,body,UUID.randomUUID().toString(),null).statusCode()).isEqualTo(202);var claim=service.claim().getFirst();service.prepare(claim);
  sql("UPDATE communication_operation SET lease_until=CURRENT_TIMESTAMP-interval '1 minute' WHERE id='"+claim.id()+"'");assertThat(service.claim()).isEmpty();assertThat(channel(b,w).get("used").asInt()).isZero();assertThat(channel(b,w).get("reserved").asInt()).isEqualTo(1);service.finish(claim,"failed",null,"PROVIDER_REJECTED");assertThat(channel(b,w).get("remaining").asInt()).isEqualTo(1);
  assertThat(b.send(path,body,UUID.randomUUID().toString(),null).statusCode()).isEqualTo(202);var second=service.claim().getFirst();service.prepare(second);service.finish(second,"accepted","second-test-receipt",null);assertThat(channel(b,w).get("used").asInt()).isEqualTo(1);
 }
 @Test void evidenceRecoveryIsVersionedScopedIdempotentAndNeverReleasesMissingEvidence()throws Exception{
  Browser b=new Browser(),other=new Browser();b.login();other.login();UUID w=b.provision(),c=contract(w,1);String path="/workspaces/"+w+"/communications/tests";b.send(path,body(b,w,c),UUID.randomUUID().toString(),null);var dispatch=service.claim().getFirst();service.prepare(dispatch);service.finish(dispatch,"unknown",null,"PROVIDER_OUTCOME_UNKNOWN");
  var op=b.get("/workspaces/"+w+"/usage/communications").get("operations").get(0);String recovery="/workspaces/"+w+"/communications/"+dispatch.id()+"/reconcile",reason="{\"reason\":\"Check existing local inbox evidence\"}",key=UUID.randomUUID().toString(),etag=op.get("etag").asString();
  assertThat(b.send(recovery,reason,key,null).statusCode()).isEqualTo(428);assertThat(b.send(recovery,reason,key,"\"stale\"").statusCode()).isEqualTo(412);assertThat(other.send(recovery,reason,key,etag).statusCode()).isEqualTo(404);
  var requested=b.send(recovery,reason,key,etag);assertThat(requested.statusCode()).as(requested.body()).isEqualTo(202);assertThat(b.send(recovery,reason,key,etag).body()).isEqualTo(requested.body());assertThat(b.send(recovery,reason.replace("existing","changed"),key,etag).statusCode()).isEqualTo(409);
  var check=reconciliation.claim().getFirst();assertThat(reconciliation.prepare(check).fingerprint()).hasSize(64);reconciliation.finish(check,"EVIDENCE_NOT_FOUND",null);assertThat(channel(b,w).get("reserved").asInt()).isEqualTo(1);assertThat(channel(b,w).get("used").asInt()).isZero();
  assertThat(b.send(recovery,reason,UUID.randomUUID().toString(),etag).statusCode()).isEqualTo(202);var second=reconciliation.claim().getFirst();reconciliation.prepare(second);reconciliation.finish(second,"MATCHED","trusted-test-receipt");reconciliation.finish(second,"MATCHED","trusted-test-receipt");assertThat(channel(b,w).get("used").asInt()).isEqualTo(1);assertThat(channel(b,w).get("reserved").asInt()).isZero();assertThat(b.send(recovery,reason,UUID.randomUUID().toString(),etag).statusCode()).isEqualTo(412);
 }
 @Test void revokedReconciliationAndExpiredWorkerTokensCannotApplyReceipts()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision(),c=contract(w,1);b.send("/workspaces/"+w+"/communications/tests",body(b,w,c),UUID.randomUUID().toString(),null);var dispatch=service.claim().getFirst();service.prepare(dispatch);service.finish(dispatch,"unknown",null,"PROVIDER_OUTCOME_UNKNOWN");var op=b.get("/workspaces/"+w+"/usage/communications").get("operations").get(0);
  b.send("/workspaces/"+w+"/communications/"+dispatch.id()+"/reconcile","{\"reason\":\"Check trusted local inbox evidence\"}",UUID.randomUUID().toString(),op.get("etag").asString());var old=reconciliation.claim().getFirst();
  sql("UPDATE communication_reconciliation SET lease_until=CURRENT_TIMESTAMP-interval '1 minute' WHERE id='"+old.id()+"'");var current=reconciliation.claim().getFirst();reconciliation.finish(old,"MATCHED","old-test-receipt");assertThat(channel(b,w).get("used").asInt()).isZero();
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");assertThatThrownBy(()->reconciliation.prepare(current)).isInstanceOf(in.lmt.shared.ApiProblem.class);reconciliation.finish(current,"MATCHED","revoked-test-receipt");assertThat(count("SELECT count(*) FROM communication_usage_event WHERE workspace_id='"+w+"' AND metric='provider_accepted'")).isZero();assertThat(count("SELECT count(*) FROM communication_reconciliation WHERE workspace_id='"+w+"' AND status='failed' AND last_code='AUTHORITY_REVOKED'")).isEqualTo(1);
 }

 @Test void periodHistoryRetainsLateUsageAndBindsPaginationToScopeAndDates()throws Exception{
  Browser b=new Browser(),other=new Browser();b.login();other.login();UUID w=b.provision(),c=contract(w,2);b.send("/workspaces/"+w+"/communications/tests",body(b,w,c),UUID.randomUUID().toString(),null);var dispatch=service.claim().getFirst();service.prepare(dispatch);service.finish(dispatch,"accepted","period-test-receipt",null);
  for(int i=2;i<29;i++)sql("INSERT INTO communication_contract(id,workspace_id,plan_version_id,channel,metric,allowance,starts_at,ends_at,mode,reason) SELECT gen_random_uuid(),workspace_id,plan_version_id,'sms','provider_accepted',3,date_trunc('day',CURRENT_TIMESTAMP)-interval '"+i+" days',date_trunc('day',CURRENT_TIMESTAMP)-interval '"+(i-1)+" days','sandbox','Historical period fixture' FROM workspace_subscription WHERE workspace_id='"+w+"'");
  String path="/workspaces/"+w+"/usage/communications/periods";var report=b.get(path);assertThat(report.get("totalMatching").asInt()).isEqualTo(28);assertThat(report.get("items").size()).isEqualTo(25);assertThat(report.get("items").get(0).get("used").asInt()).isEqualTo(1);assertThat(report.get("items").get(0).get("metric").asString()).isEqualTo("provider_accepted");String cursor=report.get("nextCursor").asString();var next=b.get(path+"?cursor="+cursor+"&from="+report.get("from").asString()+"&to="+report.get("to").asString());assertThat(next.get("items").size()).isEqualTo(3);assertThat(next.get("nextCursor").isNull()).isTrue();
  assertThat(other.send(path,null,null,null).statusCode()).isEqualTo(404);assertThat(b.send(path+"?from=2020-01-01&to=2022-01-01",null,null,null).statusCode()).isEqualTo(422);assertThat(b.send(path+"?cursor="+cursor+"&from=2020-01-01&to=2020-02-01",null,null,null).statusCode()).isEqualTo(422);
 }

}
