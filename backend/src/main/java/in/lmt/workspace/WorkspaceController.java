package in.lmt.workspace;
import in.lmt.orchestration.WorkspaceProvisioning;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1")
public class WorkspaceController {
 private final WorkspaceProvisioning provisioning;private final WorkspaceQueries queries;
 public WorkspaceController(WorkspaceProvisioning provisioning,WorkspaceQueries queries){this.provisioning=provisioning;this.queries=queries;}
 public record Profile(@NotBlank @Size(max=120) String name,@NotNull @Pattern(regexp="individual|organization") String customerType,@NotBlank @Size(max=100) String city,@NotBlank @Size(max=100) String state){}
 public record Create(@NotNull @Valid Profile profile,@NotNull UUID selectedPlanVersionId,@NotNull @Size(min=1,max=1) List<String> consentIds){}
 public record Created(UUID id,String status,String setupUrl){}
 @PostMapping(value="/workspaces",consumes="application/json") public ResponseEntity<Created> create(Authentication a,@RequestHeader("Idempotency-Key") UUID key,@Valid @RequestBody Create body){
  var p=body.profile();UUID id=provisioning.create(UUID.fromString(a.getName()),key,new WorkspaceProvisioning.Profile(p.name().strip(),p.customerType(),p.city().strip(),p.state().strip()),body.selectedPlanVersionId(),body.consentIds());
  return ResponseEntity.status(201).header("Location","/api/v1/workspaces/"+id+"/setup").body(new Created(id,"active_free","/setup?workspace="+id));
 }
 @GetMapping({"/workspaces","/me/contexts"}) public WorkspaceQueries.Contexts contexts(Authentication a,@RequestParam(required=false) String cursor){return queries.contexts(UUID.fromString(a.getName()),cursor);}
 @GetMapping("/workspaces/{id}/setup") public ResponseEntity<WorkspaceQueries.Setup> setup(Authentication a,@PathVariable UUID id){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(queries.setup(UUID.fromString(a.getName()),id));}
}
