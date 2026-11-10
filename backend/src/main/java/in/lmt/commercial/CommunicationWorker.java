package in.lmt.commercial;
import in.lmt.identity.*;
import in.lmt.shared.ApiProblem;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import tools.jackson.databind.ObjectMapper;
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class CommunicationWorker {
 private final CommunicationAllowances service;private final IdentityConfig config;private final boolean localEnabled;private final ObjectMapper json;
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
 public CommunicationWorker(CommunicationAllowances service,IdentityConfig config,ObjectMapper json,@org.springframework.beans.factory.annotation.Value("${lmt.communication.local-enabled:${lmt.identity.local-containers:false}}")boolean localEnabled){this.localEnabled=localEnabled;this.service=service;this.config=config;this.json=json;}
 @Scheduled(fixedDelayString="${lmt.communication.delay-ms:1000}",initialDelayString="${lmt.communication.initial-delay-ms:10000}")
 public void run(){if(!config.sandbox||!localEnabled)return;for(var claim:service.claim()){
  CommunicationAllowances.Dispatch work;
  try{work=service.prepare(claim);}catch(ApiProblem unavailable){service.finish(claim,"cancelled",null,unavailable.status==404?"AUTHORITY_REVOKED":"CONTRACT_UNAVAILABLE");continue;}catch(RuntimeException failure){continue;}
  String reference=null,status="unknown",code="PROVIDER_OUTCOME_UNKNOWN";
  try{
   boolean email=work.channel().equals("email");String text=LocalCommunicationEvidence.message(claim.id());
   Object body=email?Map.of("From",Map.of("Email","workspace@lmt.test","Name","LMT test environment"),"To",List.of(Map.of("Email",work.destination())),"Subject","LMT workspace metering "+claim.id(),"Text",text):Map.of("id",claim.id().toString(),"destination",work.destination(),"text",text);
   var request=HttpRequest.newBuilder(URI.create(email?"http://mailpit:8025/api/v1/send":"http://sms-inbox:8080/messages")).timeout(Duration.ofSeconds(5)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
   var pending=http.sendAsync(request,ignored->new LiveOtpTransport.BoundedBody());HttpResponse<String> response;try{response=pending.get(8,java.util.concurrent.TimeUnit.SECONDS);}finally{if(!pending.isDone())pending.cancel(true);}
   if(response.statusCode()>=200&&response.statusCode()<300){reference=json.readTree(response.body()).path(email?"ID":"id").asString("");if(reference.matches("[A-Za-z0-9-]{1,100}")){status="accepted";code=null;}else reference=null;}
   else if(response.statusCode()>=400&&response.statusCode()<500){status="failed";code="PROVIDER_REJECTED";}
  }catch(Exception failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();}
  // Persist receipt separately from the already committed dispatch intent. Unknown is never resent automatically.
  service.finish(claim,status,reference,code);
 }}
}
