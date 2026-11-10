package in.lmt.workspace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
@RestController @RequestMapping("/api/v1")
public class OperationsController {
 private final WelcomeOperations service;public OperationsController(WelcomeOperations service){this.service=service;}
 @GetMapping("/me/notification-deliveries") public WelcomeOperations.PersonalDeliveries personal(Authentication a,@RequestParam(required=false)String cursor){return service.personalDeliveries(UUID.fromString(a.getName()),cursor);}
 public record Recovery(@NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*") String reason){}
 @GetMapping("/workspaces/{w}/operations") public WelcomeOperations.Operations list(Authentication a,@PathVariable UUID w,@RequestParam(required=false)String cursor){return service.list(UUID.fromString(a.getName()),w,cursor);}
 @GetMapping("/operations/{id}") public ResponseEntity<WelcomeOperations.Detail> detail(Authentication a,@PathVariable UUID id){var result=service.detail(UUID.fromString(a.getName()),id);return ResponseEntity.ok().eTag(result.operation().etag()).body(result);}
 @PostMapping(value="/operations/{id}/retry-failed",consumes="application/json") public ResponseEntity<WelcomeOperations.Operation> retry(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Recovery body){var result=service.recover(UUID.fromString(a.getName()),id,key,tag,body.reason(),false);return ResponseEntity.accepted().header("Location","/api/v1/operations/"+id).body(result);}
 @PostMapping(value="/operations/{id}/cancel-request",consumes="application/json") public WelcomeOperations.Operation cancel(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Recovery body){return service.recover(UUID.fromString(a.getName()),id,key,tag,body.reason(),true);}
}
