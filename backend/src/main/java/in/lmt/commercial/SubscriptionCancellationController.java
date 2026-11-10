package in.lmt.commercial;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
/** Cancellation uses the same authoritative scheduled subscription transition and worker. */
@RestController @RequestMapping("/api/v1/workspaces/{w}/subscription/cancellation")
public class SubscriptionCancellationController {
 private final SubscriptionDowngrades service;
 public SubscriptionCancellationController(SubscriptionDowngrades service){this.service=service;}
 public record Confirm(@NotNull UUID reviewId){}
 public record Empty(){}
 @GetMapping public SubscriptionDowngrades.State state(Authentication a,@PathVariable UUID w){return service.cancellationState(UUID.fromString(a.getName()),w);}
 @PostMapping("/reviews") public SubscriptionDowngrades.Review preview(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@RequestBody Empty body){return service.cancellationPreview(UUID.fromString(a.getName()),w,key,tag);}
 @PostMapping public SubscriptionDowngrades.Request schedule(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@Valid @RequestBody Confirm body){return service.scheduleCancellation(UUID.fromString(a.getName()),w,key,tag,body.reviewId());}
 @PostMapping("/{id}/withdraw") public SubscriptionDowngrades.Request withdraw(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@RequestBody Empty body){return service.transitionCancellation(UUID.fromString(a.getName()),w,id,key,tag,"cancel");}
 @PostMapping("/{id}/retry") public SubscriptionDowngrades.Request retry(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@RequestBody Empty body){return service.transitionCancellation(UUID.fromString(a.getName()),w,id,key,tag,"retry");}
}
