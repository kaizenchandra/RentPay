package in.lmt.commercial;
import in.lmt.identity.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import org.springframework.stereotype.Component;
import tools.jackson.databind.*;
/** Read-only local-provider evidence; never resubmits a message. */
@Component
public class LocalCommunicationEvidence {
 public record Result(String code,String reference){}
 private final ChallengeService challenges;private final ObjectMapper json;
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
 public LocalCommunicationEvidence(ChallengeService challenges,ObjectMapper json){this.challenges=challenges;this.json=json;}
 public static String message(java.util.UUID id){return "LMT workspace communication metering test. No payment or external delivery. Reference: "+id;}
 private JsonNode get(String url)throws Exception{var pending=http.sendAsync(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),ignored->new LiveOtpTransport.BoundedBody());HttpResponse<String> r;try{r=pending.get(8,java.util.concurrent.TimeUnit.SECONDS);}finally{if(!pending.isDone())pending.cancel(true);}if(r.statusCode()==404)return null;if(r.statusCode()!=200)throw new IllegalStateException("Local inbox unavailable");return json.readTree(r.body());}
 private boolean recipient(String value,String expected){return challenges.proofFingerprint("communication-contact:"+value).equals(expected);}
 public Result find(CommunicationReconciliation.Evidence evidence){try{
  String id=evidence.operationId().toString();
  if(evidence.channel().equals("sms")){var m=get("http://sms-inbox:8080/messages/"+id);if(m==null)return new Result("EVIDENCE_NOT_FOUND",null);return m.path("id").asString("").equals(id)&&m.path("text").asString("").equals(message(evidence.operationId()))&&recipient(m.path("destination").asString(""),evidence.fingerprint())?new Result("MATCHED",id):new Result("EVIDENCE_AMBIGUOUS",null);}
  if(!evidence.channel().equals("email"))return new Result("REFERENCE_UNAVAILABLE",null);
  var result=get("http://mailpit:8025/api/v1/search?query=subject%3A"+id);if(result==null||!result.path("messages").isArray()||result.path("messages").isEmpty())return new Result("EVIDENCE_NOT_FOUND",null);
  String match=null;int checked=0;
  for(var candidate:result.path("messages")){if(++checked>5)return new Result("EVIDENCE_AMBIGUOUS",null);String reference=candidate.path("ID").asString("");if(!reference.matches("[A-Za-z0-9-]{1,100}"))return new Result("EVIDENCE_AMBIGUOUS",null);var m=get("http://mailpit:8025/api/v1/message/"+reference);
   if(m!=null&&m.path("ID").asString("").equals(reference)&&m.path("From").path("Address").asString("").equals("workspace@lmt.test")&&m.path("Subject").asString("").equals("LMT workspace metering "+id)&&m.path("Text").asString("").stripTrailing().equals(message(evidence.operationId()))&&m.path("To").size()==1&&recipient(m.path("To").get(0).path("Address").asString(""),evidence.fingerprint())){if(match!=null)return new Result("EVIDENCE_AMBIGUOUS",null);match=reference;}
  }
  return new Result(match==null?"EVIDENCE_NOT_FOUND":"MATCHED",match);
 }catch(Exception failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();return new Result("PROVIDER_UNAVAILABLE",null);}}
}
