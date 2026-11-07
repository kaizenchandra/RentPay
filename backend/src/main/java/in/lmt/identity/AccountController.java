package in.lmt.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class AccountController {
    private final AccountControls controls;
    public AccountController(AccountControls controls) { this.controls=controls; }
    public record ChangeProfile(@NotBlank @Size(max=100) @Pattern(regexp="[^\\p{Cntrl}]*",message="Do not include control characters.") String displayName) {}

    @GetMapping("/profile")
    public ResponseEntity<AccountControls.Profile> profile(Authentication auth) {
        var result=controls.profile(UUID.fromString(auth.getName()));
        return ResponseEntity.ok().eTag(result.etag()).body(result);
    }
    @PatchMapping(value="/profile",consumes="application/json")
    public ResponseEntity<AccountControls.Profile> update(Authentication auth,@Valid @RequestBody ChangeProfile body,
            @RequestHeader(value="If-Match",required=false) String expected,@RequestHeader("Idempotency-Key") UUID key) {
        var result=controls.updateProfile(UUID.fromString(auth.getName()),key,expected,body.displayName());
        return ResponseEntity.ok().eTag(result.etag()).body(result);
    }
    @GetMapping("/sessions")
    public AccountControls.Sessions sessions(Authentication auth,HttpServletRequest request,@RequestParam(required=false) String cursor) {
        return controls.sessions(UUID.fromString(auth.getName()),request.getSession().getId(),cursor);
    }
    @PostMapping("/sessions/{id}/revoke")
    public AccountControls.Revoked revoke(Authentication auth,HttpServletRequest request,@PathVariable UUID id,
            @RequestHeader(value="If-Match",required=false) String expected,@RequestHeader("Idempotency-Key") UUID key) {
        return controls.revoke(UUID.fromString(auth.getName()),request.getSession().getId(),id,key,expected);
    }
    @GetMapping("/security-notifications")
    public AccountControls.Notices notifications(Authentication auth,@RequestParam(required=false) String cursor) {
        return controls.notices(UUID.fromString(auth.getName()),cursor);
    }
}
