package in.lmt.identity;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/me/notification-preferences")
public class NotificationPreferenceController {
 private final NotificationPreferences preferences;
 public NotificationPreferenceController(NotificationPreferences preferences){this.preferences=preferences;}
 public record Change(@NotNull Boolean workspaceSetupEnabled,Boolean quietHoursEnabled,String quietStart,String quietEnd){}
 @GetMapping public ResponseEntity<NotificationPreferences.View> get(Authentication auth){var result=preferences.get(UUID.fromString(auth.getName()));return ResponseEntity.ok().eTag(result.etag()).body(result);}
 @PatchMapping public ResponseEntity<NotificationPreferences.View> update(Authentication auth,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false)String expected,@Valid @RequestBody Change body){var result=preferences.update(UUID.fromString(auth.getName()),key,expected,body.workspaceSetupEnabled(),body.quietHoursEnabled(),body.quietStart(),body.quietEnd());return ResponseEntity.ok().eTag(result.etag()).body(result);}
}
