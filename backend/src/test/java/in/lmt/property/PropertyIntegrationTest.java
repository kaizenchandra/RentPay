package in.lmt.property;
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
class PropertyIntegrationTest {
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
 String property(String code){return """
 {"code":"%s","name":"Test property","addressLine":"10 Main Road","locality":"Kothrud","city":"Pune","state":"Maharashtra","pin":"411038","latitude":null,"longitude":null,"houseRules":"Quiet hours after 10 pm"}
 """.formatted(code);}
 String space(String code){return """
 {"parentId":null,"kind":"unit","code":"%s","name":"Test unit","category":"Apartment","area":"450.50","areaUnit":"sq_ft","capacity":2,"bathrooms":1,"balconies":0,"furnishing":"unfurnished","rentalMode":"whole","rent":"12000.10","deposit":"24000.20","maintenance":"0.00"}
 """.formatted(code);}
 JsonNode post(Browser b,String path,String body,int status)throws Exception{var r=b.send(path,body,UUID.randomUUID().toString(),null);assertThat(r.statusCode()).as(r.body()).isEqualTo(status);return json.readTree(r.body());}
 @Test void propertyVersionsReplayPrivacyAndArchiveAreDurable()throws Exception{
  Browser b=new Browser(),other=new Browser();b.login();other.login();UUID w=b.provision();String root="/workspaces/"+w+"/properties",key=UUID.randomUUID().toString(),body=property("HOME");
  var created=b.send(root,body,key,null);assertThat(created.statusCode()).as(created.body()).isEqualTo(201);var p=json.readTree(created.body());String path=root+"/"+p.get("id").asString(),tag=p.get("etag").asString();
  assertThat(json.readTree(b.send(root,body,key,null).body())).isEqualTo(p);
  assertThat(b.send(root,property("OTHER"),key,null).statusCode()).isEqualTo(409);
  assertThat(b.get(root).get("totalMatching").asInt()).isEqualTo(1);
  assertThat(other.send(path,null,null,null).statusCode()).isEqualTo(404);
  assertThat(b.send(path+"?edit",body,UUID.randomUUID().toString(),null).statusCode()).isEqualTo(428);
  assertThat(b.send(path+"?edit",body,UUID.randomUUID().toString(),"\"stale\"").statusCode()).isEqualTo(412);
  var edited=b.send(path+"?edit",property("UPDATED"),UUID.randomUUID().toString(),tag);assertThat(edited.statusCode()).as(edited.body()).isEqualTo(200);tag=json.readTree(edited.body()).get("etag").asString();
  var review=b.send(path+"/readiness-reviews","{\"reason\":\"Review pending authority evidence\"}",UUID.randomUUID().toString(),tag);assertThat(review.statusCode()).as(review.body()).isEqualTo(200);var detail=json.readTree(review.body());assertThat(detail.get("readinessBlockers").size()).isGreaterThan(1);assertThat(detail.get("property").get("status").asString()).isEqualTo("review_required");
  var unit=post(b,path+"/spaces",space("U1"),200);assertThat(unit.get("rent").asString()).isEqualTo("12000.10");
  assertThat(b.send(path+"/archive-requests","{\"reason\":\"Archive reviewed unused draft\"}",UUID.randomUUID().toString(),detail.get("property").get("etag").asString()).statusCode()).isEqualTo(409);
  assertThat(count("SELECT count(*) FROM property_history WHERE workspace_id='"+w+"'")).isEqualTo(4);
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");assertThat(b.send(root,body,key,null).statusCode()).isEqualTo(404);
 }
 @Test void lastSlotRaceArchiveReleaseRestoreAndUnknownRetryReconcile()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String root="/workspaces/"+w+"/properties";var p=post(b,root,property("QUOTA"),201);String path=root+"/"+p.get("id").asString();JsonNode first=null;
  for(int n=0;n<4;n++){var created=post(b,path+"/spaces",space("U"+n),200);if(first==null)first=created;}
  try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(()->b.send(path+"/spaces",space("A"),UUID.randomUUID().toString(),null));var c=pool.submit(()->b.send(path+"/spaces",space("B"),UUID.randomUUID().toString(),null));assertThat(List.of(a.get().statusCode(),c.get().statusCode())).containsExactlyInAnyOrder(200,409);}
  assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='rentable_items'")).isEqualTo(5);
  String statePath=path+"/spaces/"+first.get("id").asString()+"/state",key=UUID.randomUUID().toString(),body="{\"status\":\"archived\",\"reason\":\"Remove unused unit from capacity\"}";
  var archived=b.send(statePath,body,key,first.get("etag").asString());assertThat(archived.statusCode()).as(archived.body()).isEqualTo(200);assertThat(json.readTree(b.send(statePath,body,key,first.get("etag").asString()).body())).isEqualTo(json.readTree(archived.body()));
  assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='rentable_items'")).isEqualTo(4);
  var restored=b.send(statePath,"{\"status\":\"draft\",\"reason\":\"Restore reviewed unused unit\"}",UUID.randomUUID().toString(),json.readTree(archived.body()).get("etag").asString());assertThat(restored.statusCode()).as(restored.body()).isEqualTo(200);
  for(var q:b.get("/workspaces/"+w+"/usage").get("quotas"))if(q.get("resource").asString().equals("rentable_items")){assertThat(q.get("used").asInt()).isEqualTo(5);assertThat(q.get("remaining").asInt()).isZero();assertThat(q.get("reconciled").asBoolean()).isTrue();}
  assertThat(count("SELECT sum(quantity) FROM rentable_usage_event WHERE workspace_id='"+w+"'")).isEqualTo(5);
  assertThat(count("SELECT count(*) FROM property_space WHERE workspace_id='"+w+"'")).isEqualTo(5);
 }
 JsonNode quota(Browser b,UUID w)throws Exception{for(var q:b.get("/workspaces/"+w+"/entitlements/snapshot").get("quotas"))if(q.get("resource").asString().equals("rentable_items"))return q;throw new AssertionError("Missing unit quota");}
 @Test void quotaWarningsRestoreCompetitionAndOverLimitCorrections()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String root="/workspaces/"+w+"/properties",path=root+"/"+post(b,root,property("LIMITS"),201).get("id").asString();
  var first=post(b,path+"/spaces",space("FIRST"),200);String firstPath=path+"/spaces/"+first.get("id").asString();
  var archived=json.readTree(b.send(firstPath+"/state","{\"status\":\"archived\",\"reason\":\"Reviewed unused inventory\"}",UUID.randomUUID().toString(),first.get("etag").asString()).body());
  for(int n=0;n<4;n++)post(b,path+"/spaces",space("U"+n),200);
  assertThat(quota(b,w).get("status").asString()).isEqualTo("NEAR_LIMIT");assertThat(quota(b,w).get("remaining").asInt()).isEqualTo(1);
  String restore="{\"status\":\"draft\",\"reason\":\"Restore reviewed inventory\"}",restoreKey=UUID.randomUUID().toString();
  try(var pool=Executors.newFixedThreadPool(2)){
   var a=pool.submit(()->b.send(firstPath+"/state",restore,restoreKey,archived.get("etag").asString()));var c=pool.submit(()->b.send(path+"/spaces",space("COMPETING"),UUID.randomUUID().toString(),null));
   var ar=a.get();var cr=c.get();assertThat(List.of(ar.statusCode(),cr.statusCode())).containsExactlyInAnyOrder(200,409);assertThat(json.readTree(ar.statusCode()==409?ar.body():cr.body()).get("code").asString()).isEqualTo("QUOTA_EXCEEDED");
  }
  assertThat(quota(b,w).get("status").asString()).isEqualTo("AT_LIMIT");assertThat(quota(b,w).get("used").asInt()).isEqualTo(5);
  assertThat(post(b,path+"/spaces",space("BLOCKED"),409).get("code").asString()).isEqualTo("QUOTA_EXCEEDED");
  // A contracted reduction never deletes evidence or blocks a safe correction.
  sql("UPDATE workspace_grant SET allowance=4 WHERE workspace_id='"+w+"' AND resource='rentable_items'");assertThat(quota(b,w).get("status").asString()).isEqualTo("OVER_LIMIT");
  var units=b.get(path+"/spaces").get("items");JsonNode active=null;for(var unit:units)if(!unit.get("status").asString().equals("archived")){active=unit;break;}assertThat(active).isNotNull();
  var release=b.send(path+"/spaces/"+active.get("id").asString()+"/state","{\"status\":\"archived\",\"reason\":\"Reviewed unused inventory\"}",UUID.randomUUID().toString(),active.get("etag").asString());assertThat(release.statusCode()).as(release.body()).isEqualTo(200);
  assertThat(quota(b,w).get("used").asInt()).isEqualTo(4);assertThat(count("SELECT sum(quantity) FROM rentable_usage_event WHERE workspace_id='"+w+"'")).isEqualTo(4);
 }
 @Test void corruptedCountersFailClosedAndDoNotCreateInventory()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String root="/workspaces/"+w+"/properties",path=root+"/"+post(b,root,property("COUNTERS"),201).get("id").asString();var existing=post(b,path+"/spaces",space("EXISTING"),200);
  sql("UPDATE workspace_grant SET used=0 WHERE workspace_id='"+w+"' AND resource='rentable_items'");assertThat(quota(b,w).get("status").asString()).isEqualTo("RECONCILIATION_REQUIRED");assertThat(quota(b,w).get("remaining").isNull()).isTrue();
  String key=UUID.randomUUID().toString();var blocked=b.send(path+"/spaces",space("RETRY"),key,null);assertThat(blocked.statusCode()).isEqualTo(409);assertThat(json.readTree(blocked.body()).get("code").asString()).isEqualTo("CAPACITY_RECONCILIATION_REQUIRED");assertThat(count("SELECT count(*) FROM property_space WHERE workspace_id='"+w+"'")).isEqualTo(1);
  sql("UPDATE workspace_grant SET used=1 WHERE workspace_id='"+w+"' AND resource='rentable_items'");assertThat(b.send(path+"/spaces",space("RETRY"),key,null).statusCode()).isEqualTo(200);assertThat(quota(b,w).get("used").asInt()).isEqualTo(2);
  var building=post(b,path+"/spaces",space("CONTAINER").replace("\"kind\":\"unit\"","\"kind\":\"building\""),200);
  sql("UPDATE rentable_allocation SET resource_id='"+building.get("id").asString()+"' WHERE resource_id='"+existing.get("id").asString()+"'");
  try{assertThat(quota(b,w).get("status").asString()).isEqualTo("RECONCILIATION_REQUIRED");assertThat(post(b,path+"/spaces",space("MAPPED"),409).get("code").asString()).isEqualTo("CAPACITY_RECONCILIATION_REQUIRED");}
  finally{sql("UPDATE rentable_allocation SET resource_id='"+existing.get("id").asString()+"' WHERE resource_id='"+building.get("id").asString()+"'");}
 }
 @Test void hierarchyValidationAndSharedPolicyCannotBeBypassed()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String root="/workspaces/"+w+"/properties";String one=root+"/"+post(b,root,property("ONE"),201).get("id").asString(),two=root+"/"+post(b,root,property("TWO"),201).get("id").asString();
  var building=post(b,one+"/spaces",space("BLD").replace("\"kind\":\"unit\"","\"kind\":\"building\""),200);
  assertThat(b.send(two+"/spaces",space("WRONG").replace("\"parentId\":null","\"parentId\":\""+building.get("id").asString()+"\""),UUID.randomUUID().toString(),null).statusCode()).isEqualTo(404);
  assertThat(b.send(one+"/spaces",space("SHARED").replace("\"rentalMode\":\"whole\"","\"rentalMode\":\"shared\""),UUID.randomUUID().toString(),null).statusCode()).isEqualTo(200);
  assertThat(b.send(one+"/spaces",space("FLOOR").replace("\"kind\":\"unit\"","\"kind\":\"floor\""),UUID.randomUUID().toString(),null).statusCode()).isEqualTo(422);
  assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='rentable_items'")).isZero();
 }
 @Test void independentBedsBoundPolicyAndRapidRestoreCannotBypassCounting()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String root="/workspaces/"+w+"/properties",path=root+"/"+post(b,root,property("SHARED_RULES"),201).get("id").asString();
  assertThat(quota(b,w).get("countingPolicyVersion").asInt()).isEqualTo(2);
  String sharedBody=space("ROOM").replace("\"rentalMode\":\"whole\"","\"rentalMode\":\"shared\"");var room=post(b,path+"/spaces",sharedBody,200);String roomId=room.get("id").asString();assertThat(quota(b,w).get("used").asInt()).isZero();
  String bedBody=space("BED1").replace("\"kind\":\"unit\"","\"kind\":\"bed\"").replace("\"parentId\":null","\"parentId\":\""+roomId+"\"");var bed=post(b,path+"/spaces",bedBody,200);post(b,path+"/spaces",bedBody.replace("BED1","BED2"),200);assertThat(quota(b,w).get("used").asInt()).isEqualTo(2);
  assertThat(post(b,path+"/spaces",bedBody.replace("BED1","BED3"),409).get("code").asString()).isEqualTo("BED_CAPACITY_EXCEEDED");
  assertThat(b.send(path+"/spaces/"+roomId+"?edit",sharedBody.replace("\"capacity\":2","\"capacity\":1"),UUID.randomUUID().toString(),room.get("etag").asString()).statusCode()).isEqualTo(409);
  String bedPath=path+"/spaces/"+bed.get("id").asString();
  for(int n=0;n<4;n++){
   var current=b.get(bedPath);String archive="{\"status\":\"archived\",\"reason\":\"Reviewed unused bed archive\"}";String key=UUID.randomUUID().toString();var archived=b.send(bedPath+"/state",archive,key,current.get("etag").asString());assertThat(archived.statusCode()).isEqualTo(200);assertThat(b.send(bedPath+"/state",archive,key,current.get("etag").asString()).body()).isEqualTo(archived.body());assertThat(quota(b,w).get("used").asInt()).isEqualTo(1);
   String tag=json.readTree(archived.body()).get("etag").asString(),restore="{\"status\":\"draft\",\"reason\":\"Restore separately rentable bed\"}";
   try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(()->b.send(bedPath+"/state",restore,UUID.randomUUID().toString(),tag));var c=pool.submit(()->b.send(bedPath+"/state",restore,UUID.randomUUID().toString(),tag));assertThat(List.of(a.get().statusCode(),c.get().statusCode())).containsExactlyInAnyOrder(200,412);}
   assertThat(quota(b,w).get("used").asInt()).isEqualTo(2);
  }
  var current=b.get(bedPath);assertThat(b.send(bedPath+"/state","{\"status\":\"maintenance_blocked\",\"reason\":\"Temporarily unavailable for repair\"}",UUID.randomUUID().toString(),current.get("etag").asString()).statusCode()).isEqualTo(200);assertThat(quota(b,w).get("used").asInt()).isEqualTo(2);
  assertThat(b.send(path+"/spaces/"+roomId+"/state","{\"status\":\"archived\",\"reason\":\"Parent must preserve active children\"}",UUID.randomUUID().toString(),room.get("etag").asString()).statusCode()).isEqualTo(409);
  assertThatThrownBy(()->sql("UPDATE workspace_counting_policy SET policy_version=1 WHERE workspace_id='"+w+"'")).isInstanceOf(SQLException.class);
  sql("INSERT INTO rentable_counting_policy VALUES(900,'units_and_beds','Unselected test policy')");assertThat(quota(b,w).get("countingPolicyVersion").asInt()).isEqualTo(2);assertThat(quota(b,w).get("used").asInt()).isEqualTo(2);
  assertThat(count("SELECT sum(quantity) FROM rentable_usage_event WHERE workspace_id='"+w+"'")).isEqualTo(2);
  sql("UPDATE workspace SET mode='live' WHERE id='"+w+"'");assertThat(post(b,path+"/spaces",sharedBody.replace("ROOM","LIVE"),409).get("code").asString()).isEqualTo("COUNTING_POLICY_REQUIRED");
 }
 @Test void auditFailureRollsBackInventoryAndQuotaAndSameKeyRecovers()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String root="/workspaces/"+w+"/properties",p=post(b,root,property("RECOVERY"),201).get("id").asString(),path=root+"/"+p+"/spaces",key=UUID.randomUUID().toString();
  sql("CREATE FUNCTION fail_property_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='space.created' THEN RAISE EXCEPTION 'Injected property audit failure'; END IF; RETURN NEW; END; $$");
  sql("CREATE TRIGGER property_audit_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_property_audit()");
  try{assertThat(b.send(path,space("ROLLBACK"),key,null).statusCode()).isEqualTo(503);}finally{sql("DROP TRIGGER property_audit_failure ON domain_audit");sql("DROP FUNCTION fail_property_audit()");}
  assertThat(count("SELECT count(*) FROM property_space WHERE workspace_id='"+w+"'")).isZero();assertThat(count("SELECT used FROM workspace_grant WHERE workspace_id='"+w+"' AND resource='rentable_items'")).isZero();assertThat(count("SELECT count(*) FROM rentable_usage_event WHERE workspace_id='"+w+"'")).isZero();
  var recovered=b.send(path,space("ROLLBACK"),key,null);assertThat(recovered.statusCode()).as(recovered.body()).isEqualTo(200);var unit=json.readTree(recovered.body());
  assertThat(b.get(path+"/"+unit.get("id").asString())).isEqualTo(unit);
  String state=path+"/"+unit.get("id").asString()+"/state";assertThat(b.send(state,"{\"status\":\"ready\",\"reason\":\"          x\"}",UUID.randomUUID().toString(),unit.get("etag").asString()).statusCode()).isEqualTo(422);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+w+"' AND action='space.created'")).isEqualTo(1);
 }
}
