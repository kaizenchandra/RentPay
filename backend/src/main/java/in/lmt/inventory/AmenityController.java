package in.lmt.inventory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/workspaces/{w}/amenity-types")
public class AmenityController {
 private final AmenityCatalog service;public AmenityController(AmenityCatalog service){this.service=service;}
 public record Input(@NotBlank @Pattern(regexp="[A-Z][A-Z0-9_]{1,39}")String code,@NotBlank @Size(max=120)String name,@NotBlank @Size(max=80)String category,@NotNull @Size(max=1000)String description,@NotBlank String trackingMode,@NotNull @Size(min=1,max=8)List<@NotBlank @Pattern(regexp="[a-z][a-z0-9_]{0,29}")String> units,@NotNull @Size(max=12)List<@NotBlank @Size(max=80)String> expectedDetails,@NotNull @Size(max=12)List<@NotBlank @Size(max=120)String> aliases,@NotBlank String status){}
 @GetMapping public AmenityCatalog.Page list(Authentication a,@PathVariable UUID w,@RequestParam(required=false)String search,@RequestParam(required=false)String status,@RequestParam(required=false)String category,@RequestParam(required=false)String cursor){return service.list(UUID.fromString(a.getName()),w,search,status,category,cursor);}
 @GetMapping("/{id}") public AmenityCatalog.Detail detail(Authentication a,@PathVariable UUID w,@PathVariable UUID id){return service.detail(UUID.fromString(a.getName()),w,id);}
 @PostMapping public ResponseEntity<AmenityCatalog.Type> create(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key")UUID key,@RequestBody @Valid Input input){return ResponseEntity.status(201).body(service.save(UUID.fromString(a.getName()),w,null,key,null,input));}
 @PatchMapping("/{id}") public AmenityCatalog.Type update(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@RequestBody @Valid Input input){return service.save(UUID.fromString(a.getName()),w,id,key,tag,input);}
}
