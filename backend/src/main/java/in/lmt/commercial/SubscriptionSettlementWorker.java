package in.lmt.commercial;
import in.lmt.shared.ApiProblem;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class SubscriptionSettlementWorker {
 private final SubscriptionSettlements service;
 public SubscriptionSettlementWorker(SubscriptionSettlements service){this.service=service;}
 @Scheduled(fixedDelayString="${lmt.subscription.settlement-delay-ms:5000}",initialDelayString="${lmt.subscription.settlement-initial-delay-ms:10000}")
 public void run(){for(var claim:service.claim()){try{service.apply(claim);}catch(ApiProblem failure){service.fail(claim,"ACTIVATION_REVIEW_REQUIRED");}catch(RuntimeException failure){service.fail(claim,"JOB_FAILED");}}}
}
