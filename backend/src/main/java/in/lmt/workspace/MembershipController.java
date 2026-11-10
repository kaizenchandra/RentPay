package in.lmt.workspace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
@RestController @RequestMapping("/api/v1")
public class MembershipController {
 private final WorkspaceMembership service;
 public MembershipController(WorkspaceMembership service){this.service=service;}
 public record Invite(@Pattern(regexp="\\+91[6-9][0-9]{9}") String mobile,@Size(max=254) String email){}
 private UUID actor(Authentication a){return UUID.fromString(a.getName());}
 @GetMapping("/workspaces/{w}/invitation-expiry") public WorkspaceMembership.ExpiryStatus expiryStatus(Authentication a,@PathVariable UUID w){return service.expiryStatus(actor(a),w);}
 @GetMapping("/workspaces/{w}/membership") public WorkspaceMembership.Context context(Authentication a,@PathVariable UUID w){return service.context(actor(a),w);}
 @GetMapping("/workspaces/{w}/members") public WorkspaceMembership.Members members(Authentication a,@PathVariable UUID w,@RequestParam(required=false)String cursor){return service.members(actor(a),w,cursor);}
 @GetMapping("/workspaces/{w}/invitations") public WorkspaceMembership.Invitations sent(Authentication a,@PathVariable UUID w,@RequestParam(required=false)String cursor){return service.invitations(actor(a),w,cursor);}
 @GetMapping("/workspaces/{w}/invitations/{id}") public ResponseEntity<WorkspaceMembership.Invitation> detail(Authentication a,@PathVariable UUID w,@PathVariable UUID id){var result=service.invitationDetail(actor(a),w,id);return ResponseEntity.ok().eTag(result.etag()).body(result);}
 @GetMapping("/me/invitations") public WorkspaceMembership.Invitations received(Authentication a,@RequestParam(required=false)String cursor){return service.invitations(actor(a),null,cursor);}
 @PostMapping(value="/workspaces/{w}/invitations",consumes="application/json") public ResponseEntity<WorkspaceMembership.Outcome> invite(Authentication a,@PathVariable UUID w,@RequestHeader("Idempotency-Key")UUID key,@Valid @RequestBody Invite body){var result=service.invite(actor(a),w,key,body.mobile(),body.email());return ResponseEntity.created(java.net.URI.create("/api/v1/workspaces/"+w+"/invitations/"+result.resourceId())).body(result);}
 @PostMapping("/workspaces/{w}/invitations/{id}/replace-expired") public ResponseEntity<WorkspaceMembership.Outcome> replace(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag){var result=service.replaceExpired(actor(a),w,id,key,tag);return ResponseEntity.created(java.net.URI.create("/api/v1/workspaces/"+w+"/invitations/"+result.resourceId())).body(result);}
 @PostMapping("/me/invitations/{id}/accept") public WorkspaceMembership.Outcome accept(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag){return service.respond(actor(a),id,key,tag,true);}
 @PostMapping("/me/invitations/{id}/decline") public WorkspaceMembership.Outcome decline(Authentication a,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag){return service.respond(actor(a),id,key,tag,false);}
 @PostMapping("/workspaces/{w}/invitations/{id}/revoke") public WorkspaceMembership.Outcome cancel(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag){return service.revokeInvite(actor(a),w,id,key,tag);}
 @PostMapping("/workspaces/{w}/members/{id}/revoke") public WorkspaceMembership.Outcome revoke(Authentication a,@PathVariable UUID w,@PathVariable UUID id,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag){return service.revokeMember(actor(a),w,id,key,tag);}
}
