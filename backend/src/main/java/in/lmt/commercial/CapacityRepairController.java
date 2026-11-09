package in.lmt.commercial;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/workspaces/{id}/manager-capacity-repair")
public class CapacityRepairController {
 private final CapacityRepairs overrides;
 public CapacityRepairController(CapacityRepairs overrides){this.overrides=overrides;}
 private ResponseEntity<CapacityRepairs.View> response(CapacityRepairs.View v){return ResponseEntity.ok().header("Cache-Control","no-store").eTag(v.etag()).body(v);}
 @GetMapping public ResponseEntity<CapacityRepairs.View> read(Authentication a,@PathVariable UUID id){return response(overrides.read(UUID.fromString(a.getName()),id));}
 @PostMapping public ResponseEntity<CapacityRepairs.View> change(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String expected,@Valid @RequestBody CapacityRepairs.Input input){return response(overrides.repair(UUID.fromString(a.getName()),id,key,expected,input));}
}
