package in.lmt.workspace;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
@Component
public class WorkspaceWelcomeDelivery {
 private final WelcomeOperations operations;
 public WorkspaceWelcomeDelivery(WelcomeOperations operations){this.operations=operations;}
 @Scheduled(fixedDelayString="${lmt.workspace.delivery-delay-ms:1000}",initialDelayString="${lmt.workspace.initial-delay-ms:0}")
 public void deliver(){
  for(var claim:operations.claim()){
   try{operations.complete(claim);}
   catch(org.springframework.dao.DataAccessException|org.springframework.transaction.TransactionException e){
    // If recording failure also loses the DB, the durable lease expires and can be reclaimed.
    operations.failed(claim);
   }
  }
 }
}
