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
class UploadIntegrationTest {
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


 byte[] pdf="%PDF-1.7\nSandbox test document\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
 String hash(byte[] bytes)throws Exception{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}
 HttpResponse<String> intent(Browser a,String key,byte[] bytes,String type)throws Exception{return a.post("/documents/upload-intents",json.writeValueAsString(Map.of("filename","test.pdf","mediaType",type,"byteSize",bytes.length,"sha256",hash(bytes))),key);}
 HttpResponse<String> upload(Browser a,String id,String etag,byte[] bytes,String csrf)throws Exception{
  var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1/documents/"+id+"/finalize")).header("Content-Type","application/octet-stream").header("Origin","http://localhost:3000");if(etag!=null)b.header("If-Match",etag);if(csrf!=null)b.header("X-CSRF-Token",csrf);
  return a.http.send(b.POST(HttpRequest.BodyPublishers.ofByteArray(bytes)).build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test void quarantineIsPrivateImmutableAndRecoverable()throws Exception{
  Browser a=new Browser();a.login();Browser b=new Browser();b.login();String key=UUID.randomUUID().toString();var created=intent(a,key,pdf,"application/pdf");assertThat(created.statusCode()).as(created.body()).isEqualTo(200);var v=parse(created);String id=v.get("id").asString(),tag=v.get("etag").asString();
  assertThat(intent(a,key,pdf,"application/pdf").body()).isEqualTo(created.body());assertThat(intent(a,key,pdf,"image/png").statusCode()).isEqualTo(409);
  assertThat(b.get("/documents/"+id).statusCode()).isEqualTo(404);assertThat(b.get("/documents/upload-intents").body()).doesNotContain(id);assertThat(upload(b,id,tag,pdf,b.csrf).statusCode()).isEqualTo(404);
  assertThat(upload(a,id,tag,pdf,null).statusCode()).isEqualTo(403);assertThat(upload(a,id,null,pdf,a.csrf).statusCode()).isEqualTo(428);assertThat(upload(a,id,"\"wrong\"",pdf,a.csrf).statusCode()).isEqualTo(412);
  assertThat(upload(a,id,tag,new byte[]{1},a.csrf).statusCode()).isEqualTo(422);byte[] changed=pdf.clone();changed[8]=33;assertThat(upload(a,id,tag,changed,a.csrf).statusCode()).isEqualTo(409);
  sql("CREATE FUNCTION fail_upload_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='UPLOAD_QUARANTINED' THEN RAISE EXCEPTION 'Injected failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER upload_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_upload_audit()");
  assertThat(upload(a,id,tag,pdf,a.csrf).statusCode()).isEqualTo(503);assertThat(parse(a.get("/documents/"+id)).get("state").asString()).isEqualTo("awaiting_upload");assertThat(Files.readAllBytes(inbox.resolveSibling("quarantine").resolve(id))).isEqualTo(pdf);assertThat(count("SELECT count(*) FROM document_scan_job WHERE upload_id='"+id+"'")).isZero();sql("DROP TRIGGER upload_failure ON domain_audit");
  HttpResponse<String> one,two;try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->upload(a,id,tag,pdf,a.csrf));var y=pool.submit(()->upload(a,id,tag,pdf,a.csrf));one=x.get();two=y.get();}assertThat(one.statusCode()).isEqualTo(200);assertThat(two.body()).isEqualTo(one.body());assertThat(parse(one).get("state").asString()).isEqualTo("quarantined");assertThat(parse(one).get("scanStatus").asString()).isEqualTo("unavailable");
  assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+id+"' AND action='UPLOAD_QUARANTINED'")).isEqualTo(1);assertThat(intent(a,key,pdf,"application/pdf").body()).isEqualTo(created.body());
  assertThat(a.get("/documents/"+id+"/download").statusCode()).isEqualTo(403);
  assertThatThrownBy(()->sql("UPDATE private_upload SET state='awaiting_upload',received_at=NULL WHERE id='"+id+"'")).hasMessageContaining("immutable");
  assertThat(Files.getPosixFilePermissions(inbox.resolveSibling("quarantine").resolve(id))).isEqualTo(java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
  try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var st=c.createStatement();var rows=st.executeQuery("SELECT count(*) FROM private_upload")){rows.next();assertThat(rows.getInt(1)).isZero();}
 }
 @Test void expiredAndSpoofedUploadsNeverBecomeQuarantined()throws Exception{
  Browser a=new Browser();a.login();byte[] spoof="not a PDF file".getBytes();var v=parse(intent(a,UUID.randomUUID().toString(),spoof,"application/pdf"));String id=v.get("id").asString(),tag=v.get("etag").asString();assertThat(upload(a,id,tag,spoof,a.csrf).statusCode()).isEqualTo(422);assertThat(Files.exists(inbox.resolveSibling("quarantine").resolve(id))).isFalse();
  sql("UPDATE private_upload SET expires_at=CURRENT_TIMESTAMP-interval '1 second' WHERE id='"+id+"'");assertThat(parse(a.get("/documents/"+id)).get("state").asString()).isEqualTo("expired");assertThat(parse(a.get("/documents/"+id)).get("etag").asString()).isNotEqualTo(tag);assertThat(upload(a,id,tag,spoof,a.csrf).statusCode()).isEqualTo(409);
  var bad=a.post("/documents/upload-intents","{\"filename\":\"../secret.pdf\",\"mediaType\":\"application/pdf\",\"byteSize\":1048577,\"sha256\":\""+hash(pdf)+"\"}",UUID.randomUUID().toString());assertThat(bad.statusCode()).as(bad.body()).isEqualTo(422);
 }
 HttpResponse<String> restore(Browser a,String id,String etag,String key,byte[] bytes)throws Exception{
  var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1/documents/"+id+"/storage/restore")).header("Content-Type","application/octet-stream").header("Origin","http://localhost:3000").header("X-CSRF-Token",a.csrf).header("Idempotency-Key",key);
  if(etag!=null)b.header("If-Match",etag);
  return a.http.send(b.POST(HttpRequest.BodyPublishers.ofByteArray(bytes)).build(),HttpResponse.BodyHandlers.ofString());
 }
 HttpResponse<String> check(Browser a,String id,String tag,String key)throws Exception{return a.send("POST","/documents/"+id+"/storage/check",null,key,a.csrf,"http://localhost:3000",tag);}
 String quarantined(Browser a)throws Exception{var v=parse(intent(a,UUID.randomUUID().toString(),pdf,"application/pdf"));String id=v.get("id").asString();assertThat(upload(a,id,v.get("etag").asString(),pdf,a.csrf).statusCode()).isEqualTo(200);return id;}
 @Test void missingFileRecoveryIsSubjectScopedAuditedAndIdempotent()throws Exception{
  Browser a=new Browser();a.login();Browser b=new Browser();b.login();String id=quarantined(a),path="/documents/"+id+"/storage";Path file=inbox.resolveSibling("quarantine").resolve(id);
  assertThat(b.get(path).statusCode()).isEqualTo(404);assertThat(check(b,id,"wrong",UUID.randomUUID().toString()).statusCode()).isEqualTo(404);assertThat(restore(b,id,"wrong",UUID.randomUUID().toString(),pdf).statusCode()).isEqualTo(404);
  var initial=parse(a.get(path));assertThat(initial.get("outcome").asString()).isEqualTo("unchecked");String tag=initial.get("etag").asString();assertThat(check(a,id,null,UUID.randomUUID().toString()).statusCode()).isEqualTo(428);assertThat(a.send("POST",path+"/check",null,UUID.randomUUID().toString(),null,"http://localhost:3000",tag).statusCode()).isEqualTo(403);
  var first=check(a,id,tag,UUID.randomUUID().toString());assertThat(first.statusCode()).as(first.body()).isEqualTo(200);assertThat(parse(first).get("outcome").asString()).isEqualTo("intact");assertThat(restore(a,id,parse(first).get("etag").asString(),UUID.randomUUID().toString(),pdf).statusCode()).isEqualTo(409);
  Files.delete(file);String missingKey=UUID.randomUUID().toString();var missing=check(a,id,parse(first).get("etag").asString(),missingKey);assertThat(parse(missing).get("outcome").asString()).isEqualTo("missing");String missingTag=parse(missing).get("etag").asString(),key=UUID.randomUUID().toString();
  assertThat(restore(a,id,null,key,pdf).statusCode()).isEqualTo(428);assertThat(restore(a,id,tag,key,pdf).statusCode()).isEqualTo(412);assertThat(restore(a,id,missingTag,key,new byte[]{1}).statusCode()).isEqualTo(409);
  sql("CREATE FUNCTION fail_restore_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.action='UPLOAD_STORAGE_RESTORED' THEN RAISE EXCEPTION 'Injected failure'; END IF; RETURN NEW; END; $$");sql("CREATE TRIGGER restore_failure BEFORE INSERT ON domain_audit FOR EACH ROW EXECUTE FUNCTION fail_restore_audit()");
  assertThat(restore(a,id,missingTag,key,pdf).statusCode()).isEqualTo(503);assertThat(Files.readAllBytes(file)).isEqualTo(pdf);assertThat(parse(a.get(path)).get("outcome").asString()).isEqualTo("missing");assertThat(count("SELECT count(*) FROM upload_storage_command WHERE upload_id='"+id+"' AND operation='restore'")).isZero();sql("DROP TRIGGER restore_failure ON domain_audit");
  HttpResponse<String> restored;try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->restore(a,id,missingTag,key,pdf));var y=pool.submit(()->restore(a,id,missingTag,key,pdf));restored=x.get();assertThat(y.get().body()).isEqualTo(restored.body());}assertThat(restored.statusCode()).as(restored.body()).isEqualTo(200);assertThat(parse(restored).get("outcome").asString()).isEqualTo("intact");assertThat(restore(a,id,missingTag,key,pdf).body()).isEqualTo(restored.body());assertThat(restore(a,id,parse(restored).get("etag").asString(),key,pdf).statusCode()).isEqualTo(409);
  assertThat(check(a,id,parse(first).get("etag").asString(),missingKey).body()).isEqualTo(missing.body());assertThat(parse(a.get(path)).get("outcome").asString()).isEqualTo("intact");assertThat(count("SELECT count(*) FROM domain_audit WHERE resource_id='"+id+"' AND action='UPLOAD_STORAGE_RESTORED'")).isEqualTo(1);assertThat(parse(a.get("/documents/"+id)).get("state").asString()).isEqualTo("quarantined");
  try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var st=c.createStatement();var r=st.executeQuery("SELECT count(*) FROM upload_storage_command")){r.next();assertThat(r.getInt(1)).isZero();}
  assertThat(a.post("/auth/logout",null,null).statusCode()).isEqualTo(204);assertThat(restore(a,id,missingTag,key,pdf).statusCode()).isIn(401,403);
 }
 @Test void competingChecksAndChangedObjectsCannotOverwriteEvidence()throws Exception{
  Browser a=new Browser();a.login();String id=quarantined(a),path="/documents/"+id+"/storage",tag=parse(a.get(path)).get("etag").asString();
  HttpResponse<String> one,two;try(var pool=Executors.newFixedThreadPool(2)){var x=pool.submit(()->check(a,id,tag,UUID.randomUUID().toString()));var y=pool.submit(()->check(a,id,tag,UUID.randomUUID().toString()));one=x.get();two=y.get();}assertThat(List.of(one.statusCode(),two.statusCode())).containsExactlyInAnyOrder(200,412);
  Path file=inbox.resolveSibling("quarantine").resolve(id);Files.delete(file);var missing=parse(check(a,id,parse(a.get(path)).get("etag").asString(),UUID.randomUUID().toString()));byte[] changed=pdf.clone();changed[10]=42;Files.write(file,changed);
  var denied=restore(a,id,missing.get("etag").asString(),UUID.randomUUID().toString(),pdf);assertThat(denied.statusCode()).isEqualTo(409);assertThat(parse(denied).get("code").asString()).isEqualTo("STORAGE_CONTENT_CONFLICT");assertThat(Files.readAllBytes(file)).isEqualTo(changed);
  var mismatch=parse(check(a,id,missing.get("etag").asString(),UUID.randomUUID().toString()));assertThat(mismatch.get("outcome").asString()).isEqualTo("mismatch");assertThat(restore(a,id,mismatch.get("etag").asString(),UUID.randomUUID().toString(),pdf).statusCode()).isEqualTo(409);
  Files.delete(file);Path other=Files.createTempFile("private-other-",".pdf");Files.write(other,pdf);Files.createSymbolicLink(file,other);var symlink=parse(check(a,id,mismatch.get("etag").asString(),UUID.randomUUID().toString()));assertThat(symlink.get("outcome").asString()).isEqualTo("mismatch");assertThat(Files.readAllBytes(other)).isEqualTo(pdf);Files.delete(file);Files.delete(other);
 }

}
