package in.lmt.workspace;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class InvitationExpiryWorker {
 private final InvitationExpiry expiry;
 public InvitationExpiryWorker(InvitationExpiry expiry){this.expiry=expiry;}
 @Scheduled(fixedDelayString="${lmt.workspace.expiry-delay-ms:30000}",initialDelayString="${lmt.workspace.expiry-initial-delay-ms:60000}")
 public void expire(){expiry.expireDue();}
}
