package in.lmt.catalog.admin;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/plan-versions")
public class OfferingAvailabilityController {
 private final OfferingAvailability service;public OfferingAvailabilityController(OfferingAvailability service){this.service=service;}
 @GetMapping("/{id}/availability") public ResponseEntity<OfferingAvailability.View> read(Authentication a,@PathVariable UUID id){var result=service.read(UUID.fromString(a.getName()),id);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).eTag(result.etag()).body(result);}
 @PostMapping(value="/{id}/availability",consumes="application/json") public OfferingAvailability.View change(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@jakarta.validation.Valid @RequestBody OfferingAvailability.Input input){return service.change(UUID.fromString(a.getName()),id,key,tag,input);}
}
