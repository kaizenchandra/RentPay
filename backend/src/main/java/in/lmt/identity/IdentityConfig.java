package in.lmt.identity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class IdentityConfig {
 public final boolean sandbox; public final String secret; public final String inbox;
 public final Set<String> origins;
 public IdentityConfig(Environment env,@Value("${lmt.identity.mode:unavailable}") String mode,
   @Value("${lmt.identity.sandbox-secret:}") String secret,@Value("${lmt.identity.inbox:}") String inbox,
   @Value("${lmt.security.allowed-origins:https://localhost}") String origins, @Value("${server.address:}") String bind){
   sandbox=mode.equals("sandbox");this.secret=secret;this.inbox=inbox;
   this.origins=Set.of(origins.split(","));
   if(sandbox && (!Set.of(env.getActiveProfiles()).equals(Set.of("local")) || secret.length()<32 || inbox.isBlank() || !Set.of("127.0.0.1","::1","localhost").contains(bind)))
     throw new IllegalStateException("Sandbox identity requires only the local profile, a private inbox and a secret of at least 32 characters");
   if(!sandbox && this.origins.stream().anyMatch(o->!o.startsWith("https://"))) throw new IllegalStateException("Production origins require HTTPS");
 }
}
