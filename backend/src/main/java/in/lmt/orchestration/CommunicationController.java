package in.lmt.orchestration;
import in.lmt.commercial.CommunicationAllowances;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/workspaces/{w}")
public class CommunicationController {
 private final in.lmt.commercial.CommunicationPeriods periods;private final in.lmt.commercial.CommunicationReconciliation recovery;private final CommunicationAllowances service;public CommunicationController(CommunicationAllowances service,in.lmt.commercial.CommunicationReconciliation recovery,in.lmt.commercial.CommunicationPeriods periods){this.periods=periods;this.service=service;this.recovery=recovery;}
 @GetMapping("/usage/communications/periods") public in.lmt.commercial.CommunicationPeriods.Page periods(Authentication a,@PathVariable UUID w,@RequestParam(required=false)java.time.LocalDate from,@RequestParam(required=false)java.time.LocalDate to,@RequestParam(required=false)String cursor){return periods.read(UUID.fromString(a.getName()),w,from,to,cursor);}
 public record ReconcileRequest(@NotBlank @Size(min=10,max=300) String reason){}
 @PostMapping("/communications/{id}/reconcile") public ResponseEntity<in.lmt.commercial.CommunicationReconciliation.Request> reconcile(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false)String expected,@RequestBody @Valid ReconcileRequest input){return ResponseEntity.accepted().body(recovery.request(UUID.fromString(a.getName()),w,id,key,expected,input.reason()));}
 public record TestRequest(@NotNull UUID contractId,@Min(1) int contactVersion){}
 @GetMapping("/usage/communications") public CommunicationAllowances.Snapshot read(Authentication a,@PathVariable UUID w){return service.read(UUID.fromString(a.getName()),w);}
 @PostMapping("/communications/tests") public ResponseEntity<CommunicationAllowances.Operation> test(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key") UUID key,@RequestBody @Valid TestRequest input){return ResponseEntity.accepted().body(service.enqueue(UUID.fromString(a.getName()),w,key,input.contractId(),input.contactVersion()));}
}
