package in.lmt.documents;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/documents/{id}/scan")
public class ScanController {
 private final ScanOperations operations;
 public ScanController(ScanOperations operations){this.operations=operations;}
 @GetMapping public ResponseEntity<ScanOperations.Detail> read(Authentication a,@PathVariable UUID id){var result=operations.read(UUID.fromString(a.getName()),id);return ResponseEntity.ok().header("Cache-Control","no-store").eTag(result.operation().etag()).body(result);}
 @PostMapping("/retry") public ResponseEntity<ScanOperations.Job> retry(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String expected,@Valid @RequestBody ScanOperations.Input input){var result=operations.command(UUID.fromString(a.getName()),id,key,expected,input,false);return ResponseEntity.accepted().header("Cache-Control","no-store").header("Location","/api/v1/documents/"+id+"/scan").eTag(result.etag()).body(result);}
 @PostMapping("/cancel") public ResponseEntity<ScanOperations.Job> cancel(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String expected,@Valid @RequestBody ScanOperations.Input input){var result=operations.command(UUID.fromString(a.getName()),id,key,expected,input,true);return ResponseEntity.ok().header("Cache-Control","no-store").eTag(result.etag()).body(result);}
}
