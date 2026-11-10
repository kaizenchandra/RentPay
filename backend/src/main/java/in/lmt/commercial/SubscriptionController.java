package in.lmt.commercial;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
public class SubscriptionController {
 private final SubscriptionDashboard dashboard;public SubscriptionController(SubscriptionDashboard dashboard){this.dashboard=dashboard;}
 @GetMapping("/api/v1/workspaces/{w}/subscription")
 public ResponseEntity<SubscriptionDashboard.Dashboard> get(Authentication actor,@PathVariable UUID w){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(dashboard.get(UUID.fromString(actor.getName()),w));}
}
