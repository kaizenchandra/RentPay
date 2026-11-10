package in.lmt.identity;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class LiveOtpTransport {
 public record Result(String status,String reference,String code){}
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
 private final ObjectMapper json;private final String account,token,service,key,from,template;private final URI sms,email;
 @org.springframework.beans.factory.annotation.Autowired
 public LiveOtpTransport(Environment env,IdentityConfig config,ObjectMapper json){this(env,config,json,URI.create("https://api.twilio.com"),URI.create("https://api.resend.com/emails"));}
 // Test-only construction can target an isolated HTTP fixture. Runtime endpoints are fixed above.
 LiveOtpTransport(Environment env,IdentityConfig config,ObjectMapper json,URI sms,URI email){
  this.json=json;this.sms=sms;this.email=email;account=env.getProperty("lmt.sms.twilio.account-sid","");token=env.getProperty("lmt.sms.twilio.auth-token","");service=env.getProperty("lmt.sms.twilio.messaging-service-sid","");key=env.getProperty("lmt.email.resend.api-key","");from=env.getProperty("lmt.email.from","");template=env.getProperty("lmt.sms.template","");
  if(config.live&&(!account.matches("AC[0-9a-fA-F]{32}")||token.isBlank()||!service.matches("MG[0-9a-fA-F]{32}")||key.isBlank()||!from.matches("[^\\s<>@]+@[^\\s<>@]+\\.[^\\s<>@]+")||template.length()>320||!template.contains("{code}")||template.contains("\r")||template.contains("\n")))throw new IllegalStateException("Live SMS/email sender credentials and approved SMS template must be configured");
 }
 private static String form(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
 public Result send(LiveDeliveryStore.Dispatch work,String code){
  boolean isEmail=work.channel().equals("email");
  try{
   HttpRequest.Builder request;
   if(isEmail){var body=json.writeValueAsString(Map.of("from",from,"to",List.of(work.destination()),"subject","Your LMT sign-in code","text","Your LMT sign-in code is "+code+". It expires in five minutes. Do not share this code. If you did not request it, ignore this email."));request=HttpRequest.newBuilder(email).header("Authorization","Bearer "+key).header("Content-Type","application/json").header("Idempotency-Key","lmt-sign-in/"+work.id()).POST(HttpRequest.BodyPublishers.ofString(body));}
   else{String body="To="+form(work.destination())+"&MessagingServiceSid="+form(service)+"&Body="+form(template.replace("{code}",code));request=HttpRequest.newBuilder(sms.resolve("/2010-04-01/Accounts/"+account+"/Messages.json")).header("Authorization","Basic "+Base64.getEncoder().encodeToString((account+":"+token).getBytes(StandardCharsets.UTF_8))).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body));}
   var pending=http.sendAsync(request.timeout(Duration.ofSeconds(5)).build(),ignored->new BoundedBody());
   HttpResponse<String> response;try{response=pending.get(8,java.util.concurrent.TimeUnit.SECONDS);}finally{if(!pending.isDone())pending.cancel(true);}
   String body=response.body();
   int status=response.statusCode();
   if(status>=200&&status<300&&body.length()<=16384){var data=json.readTree(body);String reference=data.path(isEmail?"id":"sid").asString("");if(!isEmail&&Set.of("failed","undelivered","canceled").contains(data.path("status").asString("")))return new Result("failed",reference.matches("SM[0-9a-fA-F]{32}")?reference:null,"PROVIDER_REJECTED");if(isEmail?reference.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"):reference.matches("SM[0-9a-fA-F]{32}"))return new Result("accepted",reference,null);return new Result("unknown",null,"PROVIDER_OUTCOME_UNKNOWN");}
   if(status==429)return new Result("retry",null,"PROVIDER_RATE_LIMIT");
   if(status>=500)return new Result(isEmail?"retry":"unknown",null,"PROVIDER_OUTCOME_UNKNOWN");
   return new Result("failed",null,"PROVIDER_REJECTED");
  }catch(InterruptedException e){Thread.currentThread().interrupt();return new Result("unknown",null,"PROVIDER_OUTCOME_UNKNOWN");}
  catch(Exception e){return new Result(isEmail?"retry":"unknown",null,"PROVIDER_OUTCOME_UNKNOWN");}
 }
 public static final class BoundedBody implements HttpResponse.BodySubscriber<String> {
  private final java.util.concurrent.CompletableFuture<String> result=new java.util.concurrent.CompletableFuture<>();
  private final java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();private java.util.concurrent.Flow.Subscription subscription;
  public java.util.concurrent.CompletionStage<String> getBody(){return result;}
  public void onSubscribe(java.util.concurrent.Flow.Subscription value){subscription=value;value.request(1);}
  public void onNext(List<java.nio.ByteBuffer> buffers){
   for(var buffer:buffers){if(bytes.size()+buffer.remaining()>16384){subscription.cancel();result.completeExceptionally(new IllegalStateException("Provider response too large"));return;}byte[] part=new byte[buffer.remaining()];buffer.get(part);bytes.writeBytes(part);}subscription.request(1);
  }
  public void onError(Throwable failure){result.completeExceptionally(failure);}
  public void onComplete(){result.complete(bytes.toString(StandardCharsets.UTF_8));}
 }
}
