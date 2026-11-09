package in.lmt.commercial;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/workspaces/{id}/member-entitlement")
public class EntitlementOverrideController {
 private final EntitlementOverrides overrides;
 public EntitlementOverrideController(EntitlementOverrides overrides){this.overrides=overrides;}
 private ResponseEntity<EntitlementOverrides.View> response(EntitlementOverrides.View v){return ResponseEntity.ok().header("Cache-Control","no-store").eTag(v.etag()).body(v);}
 @GetMapping public ResponseEntity<EntitlementOverrides.View> read(Authentication a,@PathVariable UUID id){return response(overrides.read(UUID.fromString(a.getName()),id));}
 @PostMapping public ResponseEntity<EntitlementOverrides.View> change(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String expected,@Valid @RequestBody EntitlementOverrides.Input input){return response(overrides.change(UUID.fromString(a.getName()),id,key,expected,input));}
}
