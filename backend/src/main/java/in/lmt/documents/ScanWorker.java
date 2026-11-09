package in.lmt.documents;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
@Component
public class ScanWorker {
 private final ScanOperations operations;
 public ScanWorker(ScanOperations operations){this.operations=operations;}
 @Scheduled(fixedDelayString="${lmt.documents.scan-delay-ms:1000}",initialDelayString="${lmt.documents.scan-initial-delay-ms:1000}")
 public void run(){
  for(var claim:operations.claim()){
   try{operations.complete(claim);}
   catch(org.springframework.dao.DataAccessException|org.springframework.transaction.TransactionException e){
    try{operations.failed(claim);}catch(org.springframework.dao.DataAccessException|org.springframework.transaction.TransactionException unavailable){/* Durable lease recovery when the database returns. */}
   }
  }
 }
}
