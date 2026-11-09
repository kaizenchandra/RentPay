package in.lmt.identity;
import java.util.UUID;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/me/reauthentication")
public class ReauthenticationController {
 private final SessionReauthentication service;public ReauthenticationController(SessionReauthentication service){this.service=service;}
 public record Request(@NotNull @Pattern(regexp="email|mobile")String channel){}
 public record Verify(@NotNull UUID challengeId,@NotNull @Pattern(regexp="[0-9]{6}")String code){}
 @PostMapping("/challenges") public ResponseEntity<ChallengeService.Challenge> request(Authentication a,HttpServletRequest q,@RequestHeader("Idempotency-Key")UUID key,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Request body){var c=service.request(UUID.fromString(a.getName()),q.getSession().getId(),key,tag,body.channel());return ResponseEntity.accepted().header("Location","/api/v1/auth/challenges/"+c.id()).body(c);}
 @PostMapping("/verify") public SessionReauthentication.Result verify(Authentication a,HttpServletRequest q,HttpServletResponse r,@RequestHeader(value="If-Match",required=false)String tag,@Valid @RequestBody Verify body){String previous=q.getSession().getId();var result=service.verify(UUID.fromString(a.getName()),previous,tag,body.challengeId(),body.code(),q::changeSessionId);new HttpSessionCsrfTokenRepository().saveToken(null,q,r);return result;}
}
