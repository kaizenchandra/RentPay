package in.lmt.identity;
import static org.assertj.core.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LiveIdentityIntegrationTest.Fixture.class)
class LiveIdentityIntegrationTest {
 static final PostgreSQLContainer db=new PostgreSQLContainer("postgres:17.11");
 static final Map<String,String> codes=new ConcurrentHashMap<>();static final Map<String,String> keys=new ConcurrentHashMap<>();
 static final HttpServer provider;static final Path keyStore;static final ObjectMapper json=new ObjectMapper();
 static {
  try{
   provider=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
   provider.createContext("/",exchange->{String body=new String(exchange.getRequestBody().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);String destination,text,reply;
    if(exchange.getRequestURI().getPath().equals("/emails")){var data=json.readTree(body);destination=data.get("to").get(0).asString();text=data.get("text").asString();keys.put(destination,exchange.getRequestHeaders().getFirst("Idempotency-Key"));reply="{\"id\":\""+UUID.randomUUID()+"\"}";}
    else{Map<String,String> form=new HashMap<>();for(String part:body.split("&")){var pair=part.split("=",2);form.put(pair[0],URLDecoder.decode(pair[1],java.nio.charset.StandardCharsets.UTF_8));}destination=form.get("To");text=form.get("Body");reply="{\"sid\":\"SM"+"a".repeat(32)+"\",\"status\":\"queued\"}";}
    var matcher=java.util.regex.Pattern.compile("\\b[0-9]{6}\\b").matcher(text);if(matcher.find())codes.put(destination,matcher.group());byte[] bytes=reply.getBytes(java.nio.charset.StandardCharsets.UTF_8);exchange.sendResponseHeaders(201,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();});provider.start();
   keyStore=Files.createTempDirectory("lmt-live-tls-").resolve("test.p12");
   var process=new ProcessBuilder(System.getProperty("java.home")+"/bin/keytool","-genkeypair","-alias","localhost","-keyalg","RSA","-keystore",keyStore.toString(),"-storepass","test-only-password","-keypass","test-only-password","-storetype","PKCS12","-dname","CN=localhost","-ext","SAN=dns:localhost,ip:127.0.0.1","-validity","2","-noprompt").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();if(!process.waitFor(20,TimeUnit.SECONDS)||process.exitValue()!=0)throw new IllegalStateException("Test TLS generation failed");
   db.start();try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var st=c.createStatement()){st.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");}
  }catch(Exception e){throw new ExceptionInInitializerError(e);}
 }
 @DynamicPropertySource static void properties(DynamicPropertyRegistry r){
  r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",()->"lmt_runtime");r.add("spring.datasource.password",()->"runtime_test");r.add("spring.flyway.user",db::getUsername);r.add("spring.flyway.password",db::getPassword);
  r.add("lmt.identity.mode",()->"live");r.add("lmt.identity.otp-secret",()->"isolated-test-only-otp-secret-at-least-32-characters");r.add("lmt.identity.hourly-limit",()->"20");r.add("lmt.security.allowed-origins",()->"https://localhost:3000");r.add("lmt.identity.live-delivery-delay-ms",()->"3600000");
  r.add("server.ssl.enabled",()->"true");r.add("server.ssl.key-store",()->keyStore.toUri().toString());r.add("server.ssl.key-store-password",()->"test-only-password");r.add("server.ssl.key-store-type",()->"PKCS12");
  r.add("lmt.sms.twilio.account-sid",()->"AC"+"1".repeat(32));r.add("lmt.sms.twilio.auth-token",()->"test-token");r.add("lmt.sms.twilio.messaging-service-sid",()->"MG"+"2".repeat(32));r.add("lmt.sms.template",()->"Test fixture code {code}");r.add("lmt.email.resend.api-key",()->"test-key");r.add("lmt.email.from",()->"fixture@example.test");
 }
 @TestConfiguration static class Fixture {@Bean @Primary LiveOtpTransport fixtureTransport(Environment env,IdentityConfig config,ObjectMapper mapper){URI origin=URI.create("http://127.0.0.1:"+provider.getAddress().getPort());return new LiveOtpTransport(env,config,mapper,origin,origin.resolve("/emails"));}}
 @org.springframework.beans.factory.annotation.Autowired LiveDeliveryWorker worker;
 @org.springframework.beans.factory.annotation.Autowired LiveDeliveryStore delivery;
 @LocalServerPort int port;
 class Browser {
  final HttpClient client;String csrf;
  Browser()throws Exception {var ssl=SSLContext.getInstance("TLS");ssl.init(null,new TrustManager[]{new X509TrustManager(){public java.security.cert.X509Certificate[] getAcceptedIssuers(){return new java.security.cert.X509Certificate[0];}public void checkClientTrusted(java.security.cert.X509Certificate[] c,String a){}public void checkServerTrusted(java.security.cert.X509Certificate[] c,String a){}}},new java.security.SecureRandom());client=HttpClient.newBuilder().sslContext(ssl).cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();}
  HttpResponse<String> send(String path,String body,String key)throws Exception {var b=HttpRequest.newBuilder(URI.create("https://localhost:"+port+"/api/v1"+path));if(body==null)b.GET();else{b.POST(HttpRequest.BodyPublishers.ofString(body)).header("Content-Type","application/json").header("Origin","https://localhost:3000").header("X-CSRF-Token",csrf);if(key!=null)b.header("Idempotency-Key",key);}return client.send(b.build(),HttpResponse.BodyHandlers.ofString());}
 }
 @Test void bothChannelsTraverseDurableProviderDeliveryAndSecureLogin()throws Exception{
  for(String channel:List.of("mobile","email")){
   Browser b=new Browser();var csrf=b.send("/auth/csrf",null,null);b.csrf=json.readTree(csrf.body()).get("token").asString();assertThat(csrf.headers().allValues("set-cookie").toString()).contains("__Host-lmt_session","Secure","HttpOnly");
   String contact=channel.equals("mobile")?"+919876500123":"live-fixture@example.test",key=UUID.randomUUID().toString();String body=json.writeValueAsString(Map.of("channel",channel,"destination",contact,"displayName","Live adapter fixture","purpose","sign_in"));var requested=b.send("/auth/challenges",body,key);assertThat(requested.statusCode()).as(requested.body()).isEqualTo(202);String id=json.readTree(requested.body()).get("id").asString();assertThat(json.readTree(b.send("/auth/challenges",body,key).body()).get("id").asString()).isEqualTo(id);
   String status="";for(int i=0;i<100;i++){worker.deliver();status=json.readTree(b.send("/auth/challenges/"+id,null,null).body()).get("status").asString();if(status.equals("accepted"))break;Thread.sleep(50);}assertThat(status).isEqualTo("accepted");assertThat(codes.get(contact)).matches("[0-9]{6}");if(channel.equals("email"))assertThat(keys.get(contact)).isEqualTo("lmt-sign-in/"+id);
   var verified=b.send("/auth/challenges/"+id+"/verify",json.writeValueAsString(Map.of("code",codes.get(contact))),null);assertThat(verified.statusCode()).as(verified.body()).isEqualTo(200);assertThat(json.readTree(verified.body()).get("mode").asString()).isEqualTo("live");assertThat(b.send("/me/profile",null,null).statusCode()).isEqualTo(200);
   Browser outsider=new Browser();assertThat(outsider.send("/auth/challenges/"+id,null,null).statusCode()).isEqualTo(404);
  }
 }

 @Test void lostSmsDispatchIsNotRetriedBlindly()throws Exception{
  Browser b=new Browser();b.csrf=json.readTree(b.send("/auth/csrf",null,null).body()).get("token").asString();var r=b.send("/auth/challenges",json.writeValueAsString(Map.of("channel","mobile","destination","+919876500789","displayName","Crash fixture","purpose","sign_in")),UUID.randomUUID().toString());assertThat(r.statusCode()).isEqualTo(202);String id=json.readTree(r.body()).get("id").asString();
  try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var st=c.createStatement()){st.execute("UPDATE auth_delivery SET status='dispatching',attempts=1,dispatch_token=gen_random_uuid(),dispatched_at=clock_timestamp()-interval '1 minute' WHERE challenge_id='"+id+"'");}
  worker.deliver();assertThat(json.readTree(b.send("/auth/challenges/"+id,null,null).body()).get("status").asString()).isEqualTo("unknown");assertThat(codes).doesNotContainKey("+919876500789");
 }
}
