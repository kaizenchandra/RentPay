package in.lmt.identity;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import tools.jackson.databind.ObjectMapper;
/** Container-only test transport; never verifies real contact ownership. */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class LocalInboxDelivery {
 private final IdentityConfig config;private final LiveDeliveryStore store;private final ChallengeService challenges;private final ObjectMapper json;
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
 public LocalInboxDelivery(IdentityConfig config,LiveDeliveryStore store,ChallengeService challenges,ObjectMapper json){this.config=config;this.store=store;this.challenges=challenges;this.json=json;}
 @Scheduled(fixedDelayString="${lmt.identity.delivery-delay-ms:1000}")
 public void deliver(){if(!config.sandbox||!config.localContainers)return;store.claim("sandbox").ifPresent(work->store.finish(work,send(work)));}
 private LiveOtpTransport.Result send(LiveDeliveryStore.Dispatch work){
  boolean email=work.channel().equals("email");
  String text="Your LMT test verification code is "+challenges.code(work.id())+". It expires five minutes after request. Do not share it. Reference: "+work.id();
  var body=email?Map.of("From",Map.of("Email","verification@lmt.test","Name","LMT test environment"),"To",List.of(Map.of("Email",work.destination())),"Subject","LMT verification "+work.id(),"Text",text):Map.of("id",work.id().toString(),"destination",work.destination(),"text",text);
  try{
   var request=HttpRequest.newBuilder(URI.create(email?"http://mailpit:8025/api/v1/send":"http://sms-inbox:8080/messages")).timeout(Duration.ofSeconds(5)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
   var pending=http.sendAsync(request,ignored->new LiveOtpTransport.BoundedBody());
   HttpResponse<String> response;try{response=pending.get(8,java.util.concurrent.TimeUnit.SECONDS);}finally{if(!pending.isDone())pending.cancel(true);}
   if(response.statusCode()>=200&&response.statusCode()<300){String reference=json.readTree(response.body()).path(email?"ID":"id").asString("");if(reference.matches("[A-Za-z0-9-]{1,100}"))return new LiveOtpTransport.Result("delivered",reference,null);}
   if(response.statusCode()==429)return new LiveOtpTransport.Result("retry",null,"LOCAL_INBOX_BUSY");
   return new LiveOtpTransport.Result(response.statusCode()>=400&&response.statusCode()<500?"failed":"unknown",null,"LOCAL_INBOX_DELIVERY_UNCONFIRMED");
  }catch(Exception unavailable){if(unavailable instanceof InterruptedException)Thread.currentThread().interrupt();return new LiveOtpTransport.Result("unknown",null,"PROVIDER_OUTCOME_UNKNOWN");}
 }
}
