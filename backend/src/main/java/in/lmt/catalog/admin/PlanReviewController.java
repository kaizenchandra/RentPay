package in.lmt.catalog.admin;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/plans")
public class PlanReviewController {
 private final PlanReview service; private final PlanDrafts drafts;
 public PlanReviewController(PlanReview service,PlanDrafts drafts){this.service=service;this.drafts=drafts;}
 @GetMapping public ResponseEntity<PlanReview.Review> list(Authentication actor,
  @RequestParam(defaultValue="workspace") String audience,@RequestParam(required=false) UUID cursor){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.list(UUID.fromString(actor.getName()),audience,cursor));
 }
 @PostMapping(value="/{id}/versions",consumes="application/json") public ResponseEntity<PlanDrafts.Result> create(Authentication actor,@PathVariable UUID id,@RequestHeader("Idempotency-Key") UUID key,@RequestHeader(value="If-Match",required=false) String tag,@jakarta.validation.Valid @RequestBody PlanDrafts.Input body){return ResponseEntity.status(201).body(drafts.create(UUID.fromString(actor.getName()),id,key,tag,body));}
}
