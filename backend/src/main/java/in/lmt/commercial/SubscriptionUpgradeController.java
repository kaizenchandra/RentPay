package in.lmt.commercial;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/workspaces/{w}/subscription")
public class SubscriptionUpgradeController {
 private final SubscriptionUpgrades upgrades;public SubscriptionUpgradeController(SubscriptionUpgrades upgrades){this.upgrades=upgrades;}
 public record QuoteInput(@NotNull UUID offerId){}
 public record Confirmation(@NotNull UUID quoteId){}
 public record Empty(){}
 public record Outcome(@NotNull @Pattern(regexp="paid|failed|unknown")String outcome){}
 @GetMapping("/payment-recovery") public SubscriptionUpgrades.Recovery recovery(Authentication a,@PathVariable UUID w){return upgrades.recovery(UUID.fromString(a.getName()),w);}
 @PostMapping("/changes/{id}/retry-payment") public SubscriptionUpgrades.Change retry(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@RequestBody Empty body){return upgrades.retryPayment(UUID.fromString(a.getName()),w,id,key,tag);}
 @GetMapping("/upgrade-options") public SubscriptionUpgrades.State state(Authentication a,@PathVariable UUID w){return upgrades.state(UUID.fromString(a.getName()),w);}
 @PostMapping("/change-quotes") public SubscriptionUpgrades.Quote quote(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String etag,@Valid @RequestBody QuoteInput body){return upgrades.createQuote(UUID.fromString(a.getName()),w,key,etag,body.offerId());}
 @PostMapping("/changes") public SubscriptionUpgrades.Change confirm(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String etag,@Valid @RequestBody Confirmation body){return upgrades.confirm(UUID.fromString(a.getName()),w,key,etag,body.quoteId());}
 @GetMapping("/changes/{id}") public SubscriptionUpgrades.Change get(Authentication a,@PathVariable UUID w,@PathVariable UUID id){return upgrades.get(UUID.fromString(a.getName()),w,id);}
 @PostMapping("/changes/{id}/sandbox-outcomes") public SubscriptionUpgrades.Change simulate(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String etag,@Valid @RequestBody Outcome body){return upgrades.simulate(UUID.fromString(a.getName()),w,id,key,etag,body.outcome());}
}
