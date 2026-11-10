package in.lmt.identity;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import tools.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;
class LiveOtpTransportTest {
 @Test void providerFailuresPreserveUncertaintyAndEmailIdempotency()throws Exception{
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var status=new AtomicInteger(503);var responseBody=new AtomicReference<String>("{}");var key=new AtomicReference<String>();var payload=new AtomicReference<String>();
  server.createContext("/",e->{key.set(e.getRequestHeaders().getFirst("Idempotency-Key"));payload.set(new String(e.getRequestBody().readAllBytes()));byte[] body=responseBody.get().getBytes();e.sendResponseHeaders(status.get(),body.length);e.getResponseBody().write(body);e.close();});server.start();
  try{var env=new MockEnvironment().withProperty("lmt.identity.hourly-limit","20").withProperty("lmt.sms.twilio.account-sid","AC"+"1".repeat(32)).withProperty("lmt.sms.twilio.auth-token","fixture").withProperty("lmt.sms.twilio.messaging-service-sid","MG"+"2".repeat(32)).withProperty("lmt.sms.template","Fixture {code}").withProperty("lmt.email.resend.api-key","fixture").withProperty("lmt.email.from","fixture@example.test");
   var config=new IdentityConfig(env,"live","x".repeat(40),"","https://localhost","");var base=URI.create("http://127.0.0.1:"+server.getAddress().getPort());var transport=new LiveOtpTransport(env,config,new ObjectMapper(),base,base.resolve("/emails"));
   var sms=new LiveDeliveryStore.Dispatch(UUID.randomUUID(),UUID.randomUUID(),"mobile","+919876500456",1);var email=new LiveDeliveryStore.Dispatch(UUID.randomUUID(),UUID.randomUUID(),"email","test@example.test",1);
   assertThat(transport.send(sms,"123456").status()).isEqualTo("unknown");assertThat(transport.send(email,"123456").status()).isEqualTo("retry");String original=payload.get();assertThat(key.get()).isEqualTo("lmt-sign-in/"+email.id());transport.send(email,"123456");assertThat(payload.get()).isEqualTo(original);assertThat(key.get()).isEqualTo("lmt-sign-in/"+email.id());
   status.set(429);assertThat(transport.send(sms,"123456").status()).isEqualTo("retry");status.set(400);assertThat(transport.send(sms,"123456").status()).isEqualTo("failed");status.set(201);assertThat(transport.send(email,"123456").status()).isEqualTo("unknown");responseBody.set("{\"sid\":\"SM"+"a".repeat(32)+"\",\"status\":\"failed\"}");assertThat(transport.send(sms,"123456").status()).isEqualTo("failed");
  }finally{server.stop(0);}
 }
}
