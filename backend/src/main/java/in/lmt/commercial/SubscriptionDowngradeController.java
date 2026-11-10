package in.lmt.commercial;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/workspaces/{w}/subscription/downgrades")
public class SubscriptionDowngradeController {
 private final SubscriptionDowngrades service;public SubscriptionDowngradeController(SubscriptionDowngrades service){this.service=service;}
 public record Preview(@NotNull UUID targetVersionId){}
 public record Confirm(@NotNull UUID reviewId){}
 public record Empty(){}
 @GetMapping public SubscriptionDowngrades.State state(Authentication a,@PathVariable UUID w){return service.state(UUID.fromString(a.getName()),w);}
 @PostMapping("/reviews") public SubscriptionDowngrades.Review preview(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Preview body){return service.preview(UUID.fromString(a.getName()),w,key,tag,body.targetVersionId());}
 @PostMapping public SubscriptionDowngrades.Request schedule(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Confirm body){return service.schedule(UUID.fromString(a.getName()),w,key,tag,body.reviewId());}
 @PostMapping("/{id}/cancel") public SubscriptionDowngrades.Request cancel(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@RequestBody Empty body){return service.transition(UUID.fromString(a.getName()),w,id,key,tag,"cancel");}
 @PostMapping("/{id}/retry") public SubscriptionDowngrades.Request retry(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@RequestBody Empty body){return service.transition(UUID.fromString(a.getName()),w,id,key,tag,"retry");}
}
