package in.lmt.inventory;
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
class AmenityCatalogIntegrationTest {
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

 String input(String code,String name,String alias,String status)throws Exception{return json.writeValueAsString(Map.of("code",code,"name",name,"category","Custom furniture","description","Custom workspace definition","trackingMode","quantity","units",List.of("item"),"expectedDetails",List.of("color"),"aliases",alias.isBlank()?List.of():List.of(alias),"status",status));}
 @Test void defaultsAliasResolutionRetirementAndImmutableVersionsAreReal()throws Exception{
  Browser b=new Browser();b.login();UUID w=b.provision();String root="/workspaces/"+w+"/amenity-types";assertThat(b.get(root).get("totalMatching").asInt()).isEqualTo(25);var fridge=b.get(root+"?search=fridge");assertThat(fridge.get("totalMatching").asInt()).isEqualTo(1);assertThat(fridge.get("items").get(0).get("code").asString()).isEqualTo("REFRIGERATOR");assertThat(b.get(root+"?search=freezer").get("items").get(0).get("code").asString()).isEqualTo("FREEZER");
  String body=input("BOOKSHELF","Bookshelf","bookcase","active"),key=UUID.randomUUID().toString();var create=b.send(root,body,key,null);assertThat(create.statusCode()).as(create.body()).isEqualTo(201);assertThat(b.send(root,body,key,null).body()).isEqualTo(create.body());var type=json.readTree(create.body());String path=root+"/"+type.get("id").asString(),etag=type.get("etag").asString();
  assertThat(b.send(path+"?edit",input("BOOKSHELF","Bookshelf","","retired"),UUID.randomUUID().toString(),null).statusCode()).isEqualTo(428);assertThat(b.send(path+"?edit",body,UUID.randomUUID().toString(),"\"stale\"").statusCode()).isEqualTo(412);
  var retire=b.send(path+"?edit",input("BOOKSHELF","Retired bookshelf","","retired"),UUID.randomUUID().toString(),etag);assertThat(retire.statusCode()).as(retire.body()).isEqualTo(200);assertThat(b.get(root+"?search=bookcase").get("totalMatching").asInt()).isZero();assertThat(b.get(root+"?search=bookcase&status=all").get("totalMatching").asInt()).isEqualTo(1);var detail=b.get(path);assertThat(detail.get("revisions").size()).isEqualTo(2);assertThat(detail.get("revisions").get(1).get("definition").get("name").asString()).isEqualTo("Bookshelf");assertThat(detail.get("revisions").get(1).get("definition").get("status").asString()).isEqualTo("active");
  var duplicate=b.send(root,input("ANOTHER","Another shelf","bookcase","active"),UUID.randomUUID().toString(),null);assertThat(duplicate.statusCode()).isEqualTo(409);assertThat(json.readTree(duplicate.body()).get("code").asString()).isEqualTo("CATALOG_TERM_EXISTS");
  assertThatThrownBy(()->sql("UPDATE amenity_type SET code='OTHER_CODE',version=version+1 WHERE id='"+type.get("id").asString()+"'")).isInstanceOf(SQLException.class);
  assertThatThrownBy(()->sql("UPDATE amenity_type_version SET snapshot='{}' WHERE type_id='"+type.get("id").asString()+"'")).isInstanceOf(SQLException.class);
  assertThat(b.send(root,input("SERVICE_TEST","Service test","","active").replace("quantity","service"),UUID.randomUUID().toString(),null).statusCode()).isEqualTo(422);
  assertThat(count("SELECT count(*) FROM domain_audit WHERE scope_id='"+w+"' AND action='inventory.catalog_definition_saved'")).isEqualTo(2);
 }
 @Test void scopedAccessConcurrentAliasesAndAuditRollbackPreventDuplicates()throws Exception{
  Browser b=new Browser(),other=new Browser();b.login();other.login();UUID w=b.provision();String root="/workspaces/"+w+"/amenity-types";assertThat(other.send(root,null,null,null).statusCode()).isEqualTo(404);String actor=other.get("/auth/session").get("account").get("id").asString();sql("INSERT INTO workspace_member(workspace_id,account_id,role) VALUES('"+w+"','"+actor+"','MANAGER')");assertThat(other.send(root,null,null,null).statusCode()).isEqualTo(404);
  try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(()->b.send(root,input("SHELF_A","Shelf A","same spelling","active"),UUID.randomUUID().toString(),null));var c=pool.submit(()->b.send(root,input("SHELF_B","Shelf B","same spelling","active"),UUID.randomUUID().toString(),null));assertThat(List.of(a.get().statusCode(),c.get().statusCode())).containsExactlyInAnyOrder(201,409);}
  sql("CREATE FUNCTION fail_catalog_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='inventory.catalog_definition_saved' THEN RAISE EXCEPTION 'Injected audit failure'; END IF; RETURN NEW; END; $$; CREATE TRIGGER catalog_audit_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_catalog_audit()");String body=input("RECOVER","Recoverable shelf","recovery alias","active"),key=UUID.randomUUID().toString();
  try{assertThat(b.send(root,body,key,null).statusCode()).isEqualTo(503);}finally{sql("DROP TRIGGER catalog_audit_failure ON domain_audit; DROP FUNCTION fail_catalog_audit()");}assertThat(b.get(root+"?search=recovery_alias").get("totalMatching").asInt()).isZero();assertThat(b.send(root,body,key,null).statusCode()).isEqualTo(201);
  var report=b.get(root);assertThat(report.get("items").size()).isEqualTo(25);assertThat(report.get("totalMatching").asInt()).isEqualTo(27);assertThat(b.get(root+"?cursor="+report.get("nextCursor").asString()).get("items").size()).isEqualTo(2);assertThat(b.send(root+"?search=changed&cursor="+report.get("nextCursor").asString(),null,null,null).statusCode()).isEqualTo(422);
  sql("UPDATE workspace_member SET active=false WHERE workspace_id='"+w+"'");assertThat(b.send(root,body,key,null).statusCode()).isEqualTo(404);
 }
}
