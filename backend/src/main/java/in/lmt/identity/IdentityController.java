package in.lmt.identity;
import in.lmt.shared.*;
import java.util.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@RestController @RequestMapping("/api/v1")
public class IdentityController {
 private final ChallengeService service;private final IdentityConfig config;private final Audit audit;private final AccountControls controls;
 public IdentityController(ChallengeService service,IdentityConfig config,Audit audit,AccountControls controls){this.service=service;this.config=config;this.audit=audit;this.controls=controls;}
 public record Csrf(String token,String headerName){}
 public record Session(boolean authenticated,String mode,ChallengeService.Account account){}
 public record RequestCode(@NotNull @Pattern(regexp="mobile") String channel,
   @NotNull @Pattern(regexp="\\+91[6-9][0-9]{9}",message="Use an Indian mobile number such as +919876543210.") String destination,
   @NotNull @Pattern(regexp="sign_in") String purpose,
   @NotBlank @Size(max=100) String displayName){}
 public record Verify(@NotNull @Pattern(regexp="[0-9]{6}",message="Enter the six-digit code.") String code){}
 @GetMapping("/auth/csrf") public Csrf csrf(CsrfToken token,HttpServletRequest request){request.getSession().setMaxInactiveInterval(request.getUserPrincipal()==null?300:1800);return new Csrf(token.getToken(),token.getHeaderName());}
 @GetMapping("/auth/session") public Session session(Authentication auth){return new Session(auth!=null,config.sandbox?"sandbox":"unavailable",auth==null?null:service.account(UUID.fromString(auth.getName())));}
 @PostMapping(value="/auth/challenges",consumes="application/json") public ResponseEntity<ChallengeService.Challenge> request(@Valid @RequestBody RequestCode body,@RequestHeader("Idempotency-Key") UUID key,HttpServletRequest q){
   var result=service.request(q.getSession().getId(),key,body.destination(),body.displayName());
   return ResponseEntity.accepted().header("Location","/api/v1/auth/challenges/"+result.id()).body(result);
 }
 @GetMapping("/auth/challenges/{id}") public ChallengeService.Challenge status(@PathVariable UUID id,HttpServletRequest q){return service.status(id,q.getSession().getId());}
 @PostMapping(value="/auth/challenges/{id}/verify",consumes="application/json") public Session verify(@PathVariable UUID id,@Valid @RequestBody Verify body,HttpServletRequest q,HttpServletResponse r){
   var account=service.verify(id,q.getSession().getId(),body.code());
   q.changeSessionId();q.getSession().setMaxInactiveInterval(1800);q.getSession().setAttribute("authenticatedAt",java.time.Instant.now().getEpochSecond());
   controls.registerSession(account.id(),q.getSession().getId(),q.getHeader("User-Agent"));
   new HttpSessionCsrfTokenRepository().saveToken(null,q,r);
   var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(account.id().toString(),null,List.of()));
   SecurityContextHolder.setContext(context);new HttpSessionSecurityContextRepository().saveContext(context,q,r);
   return new Session(true,config.sandbox?"sandbox":"unavailable",account);
 }
 @PostMapping("/auth/logout") public ResponseEntity<Void> logout(Authentication auth,HttpServletRequest q){
   UUID account=UUID.fromString(auth.getName());controls.logout(account,q.getSession().getId());
   q.getSession().invalidate();SecurityContextHolder.clearContext();return ResponseEntity.noContent().build();
 }
}
