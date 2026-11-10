package in.lmt.identity;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/me/identity-links")
public class IdentityLinkController {
 private final IdentityLinks links;public IdentityLinkController(IdentityLinks links){this.links=links;}
 public record Request(@Pattern(regexp="email|mobile")String channel,@NotBlank @Size(max=254)String destination){}
 public record Confirm(@NotNull UUID challengeId,@NotNull @Pattern(regexp="[0-9]{6}")String code){}
 @PostMapping("/challenges") public ResponseEntity<ChallengeService.Challenge> request(Authentication a,HttpServletRequest q,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Request body){var result=links.request(UUID.fromString(a.getName()),q.getSession().getId(),key,tag,body.channel(),body.destination());return ResponseEntity.accepted().header("Location","/api/v1/auth/challenges/"+result.id()).body(result);}
 @PostMapping public ResponseEntity<AccountControls.Profile> confirm(Authentication a,HttpServletRequest q,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Confirm body){var result=links.confirm(UUID.fromString(a.getName()),q.getSession().getId(),key,tag,body.challengeId(),body.code());return ResponseEntity.ok().eTag(result.etag()).body(result);}
 public record Unlink(@NotNull @Pattern(regexp="email|mobile")String channel){}
 @PostMapping("/unlink") public ResponseEntity<AccountControls.Profile> unlink(Authentication a,HttpServletRequest q,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Unlink body){var result=links.unlink(UUID.fromString(a.getName()),q.getSession().getId(),key,tag,body.channel());return ResponseEntity.ok().eTag(result.etag()).body(result);}
}
