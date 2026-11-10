package in.lmt.commercial;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class DowngradeWorker {
 private final SubscriptionDowngrades downgrades;
 public DowngradeWorker(SubscriptionDowngrades downgrades){this.downgrades=downgrades;}
 @Scheduled(fixedDelayString="${lmt.subscription.downgrade-delay-ms:30000}",initialDelayString="${lmt.subscription.downgrade-initial-delay-ms:60000}")
 public void run(){for(var claim:downgrades.claim()){try{downgrades.apply(claim);}catch(RuntimeException failure){downgrades.fail(claim,"JOB_FAILED");}}}
}
