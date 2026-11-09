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
class ScanOperationsIntegrationTest {
 static final PostgreSQLContainer db=new PostgreSQLContainer("postgres:17.11");
 static final Path inbox;
 static final AtomicInteger mobiles=new AtomicInteger(100000000);
 static final String free="00000000-0000-4000-8000-000000000006";
 static {try{inbox=Files.createTempDirectory("lmt-test-inbox-");db.start();try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");}}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 @DynamicPropertySource static void properties(DynamicPropertyRegistry r){
  r.add("lmt.documents.scan-initial-delay-ms",()->"3600000");r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",()->"lmt_runtime");r.add("spring.datasource.password",()->"runtime_test");r.add("spring.flyway.user",db::getUsername);r.add("spring.flyway.password",db::getPassword);
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


 byte[] pdf="%PDF-1.7\nSandbox test document\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
 String hash(byte[] bytes)throws Exception{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}
 HttpResponse<String> intent(Browser a,String key,byte[] bytes,String type)throws Exception{return a.post("/documents/upload-intents",json.writeValueAsString(Map.of("filename","test.pdf","mediaType",type,"byteSize",bytes.length,"sha256",hash(bytes))),key);}
 HttpResponse<String> upload(Browser a,String id,String etag,byte[] bytes,String csrf)throws Exception{
  var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1/documents/"+id+"/finalize")).header("Content-Type","application/octet-stream").header("Origin","http://localhost:3000");if(etag!=null)b.header("If-Match",etag);if(csrf!=null)b.header("X-CSRF-Token",csrf);
  return a.http.send(b.POST(HttpRequest.BodyPublishers.ofByteArray(bytes)).build(),HttpResponse.BodyHandlers.ofString());
 }

 @org.springframework.beans.factory.annotation.Autowired in.lmt.documents.ScanOperations operations;
 String quarantined(Browser a)throws Exception{var v=parse(intent(a,UUID.randomUUID().toString(),pdf,"application/pdf"));String id=v.get("id").asString();assertThat(upload(a,id,v.get("etag").asString(),pdf,a.csrf).statusCode()).isEqualTo(200);return id;}
 JsonNode job(Browser a,String id)throws Exception{return parse(a.get("/documents/"+id+"/scan")).get("operation");}
 in.lmt.documents.ScanOperations.Claim claim(String id){return operations.claim().stream().filter(c->c.upload().toString().equals(id)).findFirst().orElseThrow();}
 HttpResponse<String> command(Browser a,String id,String action,String etag,String key)throws Exception{return a.send("POST","/documents/"+id+"/scan/"+action,"{\"reason\":\"Review sandbox scan preparation\"}",key,a.csrf,"http://localhost:3000",etag);}
 @Test void cancellationFencesWorkersAndRecoveryIsAuthorizedAndAtomic()throws Exception{
  Browser a=new Browser();a.login();Browser b=new Browser();b.login();String id=quarantined(a);assertThat(b.get("/documents/"+id+"/scan").statusCode()).isEqualTo(404);var queued=job(a,id);assertThat(queued.get("status").asString()).isEqualTo("queued");String otherActor=parse(b.get("/auth/session")).get("account").get("id").asString();assertThatThrownBy(()->sql("INSERT INTO document_scan_command(account_id,request_key,job_id,request_hash,reason,result) VALUES('"+otherActor+"',gen_random_uuid(),'"+queued.get("id").asString()+"','invalid','Cross-account test fixture','{}')")).hasMessageContaining("foreign key constraint");assertThat(count("SELECT count(*) FROM document_scan_job WHERE upload_id='"+id+"'")).isEqualTo(1);
  assertThat(command(b,id,"cancel",queued.get("etag").asString(),UUID.randomUUID().toString()).statusCode()).isEqualTo(404);assertThat(command(a,id,"cancel",null,UUID.randomUUID().toString()).statusCode()).isEqualTo(428);
  java.util.List<in.lmt.documents.ScanOperations.Claim> claims;try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->operations.claim());var y=pool.submit(()->operations.claim());claims=new ArrayList<>(x.get());claims.addAll(y.get());}assertThat(claims.stream().filter(c->c.upload().toString().equals(id))).hasSize(1);var lease=claims.stream().filter(c->c.upload().toString().equals(id)).findFirst().orElseThrow();
  assertThat(command(a,id,"cancel",queued.get("etag").asString(),UUID.randomUUID().toString()).statusCode()).isEqualTo(412);String runningTag=job(a,id).get("etag").asString(),cancelKey=UUID.randomUUID().toString();assertThat(a.send("POST","/documents/"+id+"/scan/cancel","{\"reason\":\"Cancel sandbox scan preparation\"}",cancelKey,null,"http://localhost:3000",runningTag).statusCode()).isEqualTo(403);
  var cancelled=command(a,id,"cancel",runningTag,cancelKey);assertThat(cancelled.statusCode()).as(cancelled.body()).isEqualTo(200);operations.complete(lease);assertThat(job(a,id).get("status").asString()).isEqualTo("cancelled");assertThat(command(a,id,"cancel",runningTag,cancelKey).body()).isEqualTo(cancelled.body());assertThat(command(a,id,"retry",runningTag,cancelKey).statusCode()).isEqualTo(409);
  String tag=job(a,id).get("etag").asString(),retryKey=UUID.randomUUID().toString();sql("CREATE FUNCTION fail_scan_retry() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='DOCUMENT_SCAN_REQUEUED' THEN RAISE EXCEPTION 'Injected retry failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER scan_retry_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_scan_retry()");assertThat(command(a,id,"retry",tag,retryKey).statusCode()).isEqualTo(503);assertThat(job(a,id).get("status").asString()).isEqualTo("cancelled");sql("DROP TRIGGER scan_retry_failure ON domain_audit");
  var retry=command(a,id,"retry",tag,retryKey);assertThat(retry.statusCode()).as(retry.body()).isEqualTo(202);assertThat(retry.headers().firstValue("Location").orElseThrow()).isEqualTo("/api/v1/documents/"+id+"/scan");operations.complete(claim(id));assertThat(job(a,id).get("lastCode").asString()).isEqualTo("SCANNER_UNAVAILABLE");assertThat(command(a,id,"retry",tag,retryKey).body()).isEqualTo(retry.body());assertThat(job(a,id).get("status").asString()).isEqualTo("blocked");assertThat(parse(a.get("/documents/"+id)).get("state").asString()).isEqualTo("quarantined");assertThat(a.get("/documents/"+id+"/download").statusCode()).isEqualTo(403);
  assertThat(a.post("/auth/logout",null,null).statusCode()).isEqualTo(204);assertThat(command(a,id,"retry",tag,retryKey).statusCode()).isIn(401,403);
 }
 @Test void expiredLeaseAndTransientFailureRecoverWithoutFalseScanSuccess()throws Exception{
  Browser a=new Browser();a.login();String id=quarantined(a);var old=claim(id);sql("UPDATE document_scan_job SET lease_until=CURRENT_TIMESTAMP-interval '1 second' WHERE upload_id='"+id+"'");operations.complete(old);assertThat(job(a,id).get("status").asString()).isEqualTo("running");var current=claim(id);operations.complete(old);assertThat(job(a,id).get("attempts").asInt()).isEqualTo(2);
  sql("CREATE FUNCTION fail_scan_completion() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='DOCUMENT_SCAN_BLOCKED' THEN RAISE EXCEPTION 'Injected completion failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER scan_complete_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_scan_completion()");assertThatThrownBy(()->operations.complete(current)).isInstanceOf(org.springframework.dao.DataAccessException.class);assertThat(job(a,id).get("status").asString()).isEqualTo("running");sql("DROP TRIGGER scan_complete_failure ON domain_audit");operations.failed(current);assertThat(job(a,id).get("lastCode").asString()).isEqualTo("TRANSIENT_FAILURE");assertThat(operations.claim().stream().noneMatch(c->c.upload().toString().equals(id))).isTrue();
  sql("UPDATE document_scan_job SET next_attempt_at=CURRENT_TIMESTAMP-interval '1 second' WHERE upload_id='"+id+"'");Files.delete(inbox.resolveSibling("quarantine").resolve(id));operations.complete(claim(id));assertThat(job(a,id).get("lastCode").asString()).isEqualTo("STORAGE_MISSING");Files.write(inbox.resolveSibling("quarantine").resolve(id),pdf);var state=job(a,id);assertThat(command(a,id,"retry",state.get("etag").asString(),UUID.randomUUID().toString()).statusCode()).isEqualTo(202);operations.complete(claim(id));assertThat(job(a,id).get("lastCode").asString()).isEqualTo("SCANNER_UNAVAILABLE");assertThat(job(a,id).get("attempts").asInt()).isEqualTo(4);
 }
 @Test void automaticAttemptsAreBoundedAndHistorySurvivesAnotherCycle()throws Exception{
  Browser a=new Browser();a.login();String id=quarantined(a);
  for(int n=0;n<5;n++){var lease=claim(id);sql("UPDATE document_scan_job SET lease_until=CURRENT_TIMESTAMP-interval '1 second' WHERE upload_id='"+id+"'");operations.complete(lease);}
  assertThat(operations.claim().stream().noneMatch(c->c.upload().toString().equals(id))).isTrue();var failed=job(a,id);assertThat(failed.get("status").asString()).isEqualTo("failed");assertThat(failed.get("lastCode").asString()).isEqualTo("RETRY_EXHAUSTED");assertThat(failed.get("attempts").asInt()).isEqualTo(5);
  assertThat(command(a,id,"retry",failed.get("etag").asString(),UUID.randomUUID().toString()).statusCode()).isEqualTo(202);operations.complete(claim(id));var detail=parse(a.get("/documents/"+id+"/scan"));assertThat(detail.get("totalAttempts").asInt()).isEqualTo(6);assertThat(detail.get("recentAttempts").size()).isEqualTo(5);assertThat(detail.get("operation").get("cycleAttempts").asInt()).isEqualTo(1);assertThat(detail.get("operation").get("status").asString()).isEqualTo("blocked");
 }
}
