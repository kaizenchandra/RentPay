package in.lmt.identity;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class LiveDeliveryWorker {
 private final IdentityConfig config;private final LiveDeliveryStore store;private final LiveOtpTransport transport;private final ChallengeService challenges;
 public LiveDeliveryWorker(IdentityConfig config,LiveDeliveryStore store,LiveOtpTransport transport,ChallengeService challenges){this.config=config;this.store=store;this.transport=transport;this.challenges=challenges;}
 @Scheduled(fixedDelayString="${lmt.identity.live-delivery-delay-ms:1000}")
 public void deliver(){if(!config.live)return;store.claim().ifPresent(work->{var result=transport.send(work,challenges.code(work.id()));store.finish(work,result);});}
}
