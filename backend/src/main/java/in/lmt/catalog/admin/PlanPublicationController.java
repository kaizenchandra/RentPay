package in.lmt.catalog.admin;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/plan-versions")
public class PlanPublicationController {
 private final PlanPublication service;private final PlanDraftEdits edits;private final PlanDraftDiscard discards;public PlanPublicationController(PlanPublication service,PlanDraftEdits edits,PlanDraftDiscard discards){this.service=service;this.edits=edits;this.discards=discards;}
 @GetMapping("/{id}/activation-preview") public ResponseEntity<PlanPublication.Preview> preview(Authentication a,@PathVariable UUID id){var result=service.preview(UUID.fromString(a.getName()),id);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);}
 @PostMapping(value="/{id}/activate",consumes="application/json") public PlanPublication.Result activate(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@jakarta.validation.Valid @RequestBody PlanPublication.Input input){return service.activate(UUID.fromString(a.getName()),id,key,tag,input);}
 @PatchMapping(value="/{id}",consumes="application/json") public PlanDraftEdits.Result edit(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@jakarta.validation.Valid @RequestBody PlanDrafts.Input input){return edits.update(UUID.fromString(a.getName()),id,key,tag,input);}
 @PostMapping(value="/{id}/discard",consumes="application/json") public PlanDraftDiscard.Result discard(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@jakarta.validation.Valid @RequestBody PlanDraftDiscard.Input input){return discards.discard(UUID.fromString(a.getName()),id,key,tag,input);}
}
