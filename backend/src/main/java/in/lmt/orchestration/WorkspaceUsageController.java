package in.lmt.orchestration;
import in.lmt.commercial.UsageQueries;
import in.lmt.commercial.CapacityRepairHistory;
import in.lmt.workspace.WorkspaceCommands;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.*;
@RestController @RequestMapping("/api/v1/workspaces/{w}")
@Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
public class WorkspaceUsageController {
 private final WorkspaceCommands authority;private final UsageQueries usage;private final CapacityRepairHistory repairs;
 public WorkspaceUsageController(WorkspaceCommands authority,UsageQueries usage,CapacityRepairHistory repairs){this.authority=authority;this.usage=usage;this.repairs=repairs;}
 @GetMapping({"/entitlements","/usage"}) public UsageQueries.Snapshot snapshot(Authentication a,@PathVariable UUID w){authority.requireOwner(w,UUID.fromString(a.getName()));return usage.snapshot(w);}
 @GetMapping("/usage/history") public UsageQueries.History history(Authentication a,@PathVariable UUID w,@RequestParam(required=false)LocalDate from,@RequestParam(required=false)LocalDate to,@RequestParam(required=false)String cursor){UUID actor=UUID.fromString(a.getName());authority.requireOwner(w,actor);return usage.history(actor,w,from,to,cursor);}
 @GetMapping("/usage/repairs") public CapacityRepairHistory.History repairs(Authentication a,@PathVariable UUID w,@RequestParam(required=false)LocalDate from,@RequestParam(required=false)LocalDate to,@RequestParam(required=false)String cursor){UUID actor=UUID.fromString(a.getName());authority.requireOwner(w,actor);return repairs.read(actor,w,from,to,cursor);}
}
