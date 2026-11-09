package in.lmt.documents;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/documents/{id}/storage")
public class StorageRecoveryController {
 private final StorageRecovery recovery;
 public StorageRecoveryController(StorageRecovery recovery){this.recovery=recovery;}
 private ResponseEntity<StorageRecovery.View> response(StorageRecovery.View v){return ResponseEntity.ok().header("Cache-Control","no-store").eTag(v.etag()).body(v);}
 @GetMapping public ResponseEntity<StorageRecovery.View> read(Authentication a,@PathVariable UUID id){return response(recovery.read(UUID.fromString(a.getName()),id));}
 @PostMapping("/check") public ResponseEntity<StorageRecovery.View> check(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String expected){return response(recovery.check(UUID.fromString(a.getName()),id,key,expected));}
 @PostMapping(value="/restore",consumes="application/octet-stream") public ResponseEntity<StorageRecovery.View> restore(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String expected,HttpServletRequest request)throws java.io.IOException{return response(recovery.restore(UUID.fromString(a.getName()),id,key,expected,request.getInputStream()));}
}
