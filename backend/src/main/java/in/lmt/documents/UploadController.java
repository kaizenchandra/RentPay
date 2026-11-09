package in.lmt.documents;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/documents")
public class UploadController {
 private final Uploads uploads;
 public UploadController(Uploads uploads){this.uploads=uploads;}
 private ResponseEntity<Uploads.View> response(Uploads.View v){return ResponseEntity.ok().header("Cache-Control","no-store").eTag(v.etag()).body(v);}
 @PostMapping(value="/upload-intents",consumes="application/json") public ResponseEntity<Uploads.View> create(Authentication a,@RequestHeader("Idempotency-Key")UUID key,@Valid @RequestBody Uploads.Input input){return response(uploads.create(UUID.fromString(a.getName()),key,input));}
 @GetMapping("/upload-intents") public ResponseEntity<Uploads.Listing> list(Authentication a,@RequestParam(required=false)UUID cursor){return ResponseEntity.ok().header("Cache-Control","no-store").body(uploads.list(UUID.fromString(a.getName()),cursor));}
 @GetMapping("/{id}") public ResponseEntity<Uploads.View> read(Authentication a,@PathVariable UUID id){return response(uploads.read(UUID.fromString(a.getName()),id));}
 @PostMapping(value="/{id}/finalize",consumes="application/octet-stream") public ResponseEntity<Uploads.View> receive(Authentication a,@PathVariable UUID id,@RequestHeader(value="If-Match",required=false)String etag,HttpServletRequest request)throws java.io.IOException{return response(uploads.receive(UUID.fromString(a.getName()),id,etag,request.getInputStream()));}
}
