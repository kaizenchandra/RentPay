package in.lmt.commercial;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/workspaces/{w}/subscription/changes/{id}")
public class SubscriptionSettlementController {
 private final SubscriptionSettlements service;
 public SubscriptionSettlementController(SubscriptionSettlements service){this.service=service;}
 public record Callback(@NotNull @Pattern(regexp="pending|paid|reversed") String outcome){}
 public record Empty(){}
 @PostMapping("/sandbox-callbacks") public SubscriptionUpgrades.Change callback(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID event,@RequestHeader(value="If-Match",required=false) String tag,@Valid @RequestBody Callback body){return service.callback(UUID.fromString(a.getName()),w,id,event,tag,body.outcome());}
 @PostMapping("/retry-activation") public SubscriptionUpgrades.Change retry(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@RequestBody Empty body){return service.retry(UUID.fromString(a.getName()),w,id,key,tag);}
}
