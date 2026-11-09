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
 public record RequestCode(@NotNull @Pattern(regexp="mobile|email") String channel,
   @NotBlank @Size(max=254) String destination,
   @NotNull @Pattern(regexp="sign_in") String purpose,
   @NotBlank @Size(max=100) String displayName){}
 public record Verify(@NotNull @Pattern(regexp="[0-9]{6}",message="Enter the six-digit code.") String code){}
 @GetMapping("/auth/csrf") public Csrf csrf(CsrfToken token,HttpServletRequest request){request.getSession().setMaxInactiveInterval(request.getUserPrincipal()==null?300:1800);return new Csrf(token.getToken(),token.getHeaderName());}
 @GetMapping("/auth/session") public Session session(Authentication auth){return new Session(auth!=null,config.sandbox?"sandbox":"unavailable",auth==null?null:service.account(UUID.fromString(auth.getName())));}
 @PostMapping(value="/auth/challenges",consumes="application/json") public ResponseEntity<ChallengeService.Challenge> request(@Valid @RequestBody RequestCode body,@RequestHeader("Idempotency-Key") UUID key,HttpServletRequest q){
   var result=service.request(q.getSession().getId(),key,body.channel(),body.destination(),body.displayName());
   return ResponseEntity.accepted().header("Location","/api/v1/auth/challenges/"+result.id()).body(result);
 }
 @GetMapping("/auth/challenges/{id}") public ChallengeService.Challenge status(@PathVariable UUID id,HttpServletRequest q){return service.status(id,q.getSession().getId());}
 @PostMapping(value="/auth/challenges/{id}/verify",consumes="application/json") public Session verify(@PathVariable UUID id,@Valid @RequestBody Verify body,HttpServletRequest q,HttpServletResponse r){
   var proof=service.verify(id,q.getSession().getId(),body.code());
   return establish(proof,q,r);
 }
 private Session establish(ChallengeService.VerifiedAccount proof,HttpServletRequest q,HttpServletResponse r){
   var account=proof.account();
   q.changeSessionId();q.getSession().setMaxInactiveInterval(1800);q.getSession().setAttribute("authenticatedAt",java.time.Instant.now().getEpochSecond());
   controls.registerSession(account.id(),q.getSession().getId(),q.getHeader("User-Agent"),proof.generation());
   new HttpSessionCsrfTokenRepository().saveToken(null,q,r);
   var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(account.id().toString(),null,List.of()));
   SecurityContextHolder.setContext(context);new HttpSessionSecurityContextRepository().saveContext(context,q,r);
   return new Session(true,config.sandbox?"sandbox":"unavailable",account);
 }
 @PostMapping("/auth/logout") public ResponseEntity<Void> logout(Authentication auth,HttpServletRequest q){
   UUID account=UUID.fromString(auth.getName());controls.logout(account,q.getSession().getId());
   q.getSession().invalidate();SecurityContextHolder.clearContext();return ResponseEntity.noContent().build();
 }
 public record RecoveryRequest(@NotNull @Pattern(regexp="mobile|email") String channel,@NotBlank @Size(max=254) String destination){}
 public record RecoveryProof(@NotNull UUID challengeId,@NotNull @Pattern(regexp="[0-9]{6}") String code,@NotNull @AssertTrue(message="Confirm ending existing sessions.") Boolean endExistingSessions){}
 private void anonymous(Authentication auth){if(auth!=null)throw new ApiProblem(409,"SIGN_OUT_FIRST","Sign out of this browser before starting account recovery.");}
 @PostMapping("/auth/recovery/challenges") public ResponseEntity<ChallengeService.RecoveryChallenge> recoveryRequest(Authentication auth,@Valid @RequestBody RecoveryRequest body,@RequestHeader("Idempotency-Key")UUID key,HttpServletRequest q){anonymous(auth);var c=service.requestRecovery(q.getSession().getId(),key,body.channel(),body.destination());return ResponseEntity.accepted().header("Location","/api/v1/auth/challenges/"+c.id()).body(c);}
 @PostMapping("/auth/recovery/verify") public Session recover(Authentication auth,@Valid @RequestBody RecoveryProof body,HttpServletRequest q,HttpServletResponse r){anonymous(auth);return establish(service.recover(body.challengeId(),q.getSession().getId(),body.code()),q,r);}

 public record RecoveryOutcomeRequest(@NotNull UUID challengeId,@NotBlank @Size(max=128) String outcomeToken){}
 @PostMapping("/auth/recovery/outcome") public ChallengeService.RecoveryOutcome recoveryOutcome(@Valid @RequestBody RecoveryOutcomeRequest body){return service.recoveryOutcome(body.challengeId(),body.outcomeToken());}

}
