package in.lmt.property;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/workspaces/{w}/properties")
public class PropertyController {
 private final Properties service;public PropertyController(Properties service){this.service=service;}
 public record Input(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{1,40}")String code,@NotBlank @Size(max=120)String name,@NotNull @Size(max=240)String addressLine,@NotNull @Size(max=120)String locality,@NotNull @Size(max=100)String city,@NotNull @Size(max=100)String state,@NotNull @Pattern(regexp="|[1-9][0-9]{5}")String pin,@Pattern(regexp="-?[0-9]{1,3}(\\.[0-9]{1,6})?")String latitude,@Pattern(regexp="-?[0-9]{1,3}(\\.[0-9]{1,6})?")String longitude,@NotNull @Size(max=4000)String houseRules){}
 public record SpaceInput(UUID parentId,@NotNull @Pattern(regexp="building|floor|unit|bed")String kind,@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{1,40}")String code,@NotBlank @Size(max=120)String name,@NotNull @Size(max=80)String category,@Pattern(regexp="[0-9]{1,8}(\\.[0-9]{1,2})?")String area,@NotNull @Pattern(regexp="sq_ft|sq_m")String areaUnit,@Min(1) @Max(1000)int capacity,@Min(0) @Max(100)int bathrooms,@Min(0) @Max(100)int balconies,@NotNull @Pattern(regexp="unfurnished|semi_furnished|furnished")String furnishing,@NotNull @Pattern(regexp="whole|shared")String rentalMode,@Pattern(regexp="[0-9]{1,12}(\\.[0-9]{1,2})?")String rent,@Pattern(regexp="[0-9]{1,12}(\\.[0-9]{1,2})?")String deposit,@Pattern(regexp="[0-9]{1,12}(\\.[0-9]{1,2})?")String maintenance){}
 public record State(@NotNull @Pattern(regexp="draft|ready|maintenance_blocked|archived")String status,@NotBlank @Size(min=10,max=500)String reason){}
 public record Reason(@NotBlank @Size(min=10,max=500)String reason){}
 private UUID actor(Authentication a){return UUID.fromString(a.getName());}
 @GetMapping public Properties.Page<Properties.Property> list(Authentication a,@PathVariable UUID w,@RequestParam(required=false)String cursor,@RequestParam(defaultValue="all")String status){return service.list(actor(a),w,cursor,status);}
 @PostMapping public ResponseEntity<Properties.Property> create(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key")UUID key,@Valid @RequestBody Input body){var result=service.create(actor(a),w,key,body);return ResponseEntity.created(java.net.URI.create("/api/v1/workspaces/"+w+"/properties/"+result.id())).eTag(result.etag()).body(result);}
 @GetMapping("/{id}") public Properties.Detail detail(Authentication a,@PathVariable UUID w,@PathVariable UUID id){return service.detail(actor(a),w,id);}
 @PatchMapping("/{id}") public Properties.Property edit(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Input body){return service.edit(actor(a),w,id,key,tag,body);}
 @PostMapping("/{id}/archive-requests") public Properties.Property archive(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Reason body){return service.archive(actor(a),w,id,key,tag,body.reason());}
 @PostMapping("/{id}/restore") public Properties.Property restore(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Reason body){return service.restore(actor(a),w,id,key,tag,body.reason());}
 @PostMapping("/{id}/readiness-reviews") public Properties.Detail review(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Reason body){return service.review(actor(a),w,id,key,tag,body.reason());}
 @GetMapping("/{id}/spaces") public Properties.Page<Properties.Space> spaces(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestParam(required=false)String cursor){return service.spaces(actor(a),w,id,cursor);}
 @PostMapping("/{id}/spaces") public Properties.Space addSpace(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@Valid @RequestBody SpaceInput body){return service.addSpace(actor(a),w,id,key,body);}
 @GetMapping("/{id}/spaces/{space}") public Properties.Space space(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@PathVariable UUID space){return service.getSpace(actor(a),w,id,space);}
 @PatchMapping("/{id}/spaces/{space}") public Properties.Space editSpace(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@PathVariable UUID space,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody SpaceInput body){return service.editSpace(actor(a),w,id,space,key,tag,body);}
 @PostMapping("/{id}/spaces/{space}/state") public Properties.Space state(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@PathVariable UUID space,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody State body){return service.state(actor(a),w,id,space,key,tag,body);}
}
