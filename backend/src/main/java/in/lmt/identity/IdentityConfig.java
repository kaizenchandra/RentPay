package in.lmt.identity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class IdentityConfig {
 public final boolean localContainers; public final int hourlyLimit; public final boolean sandbox; public final boolean live; public final String mode; public final String secret; public final String inbox;
 public final Set<String> origins;
 public IdentityConfig(Environment env,@Value("${lmt.identity.mode:unavailable}") String mode,
   @Value("${lmt.identity.otp-secret:${lmt.identity.sandbox-secret:}}") String secret,@Value("${lmt.identity.inbox:}") String inbox,
   @Value("${lmt.security.allowed-origins:https://localhost}") String origins, @Value("${server.address:}") String bind){
   this.mode=mode;live=mode.equals("live");hourlyLimit=env.getProperty("lmt.identity.hourly-limit",Integer.class,0);
   if(!Set.of("sandbox","live","unavailable").contains(mode))throw new IllegalStateException("Unsupported identity mode");
   sandbox=mode.equals("sandbox");localContainers=env.getProperty("lmt.identity.local-containers",Boolean.class,false);if(localContainers&&!sandbox)throw new IllegalStateException("Local inbox containers require sandbox identity");this.secret=secret;this.inbox=inbox;
   this.origins=Set.of(origins.split(","));
   boolean containerLocal=env.getProperty("lmt.container-sandbox",Boolean.class,false)&&bind.equals("0.0.0.0")&&this.origins.stream().allMatch(o->{try{var uri=java.net.URI.create(o);return uri.getScheme().equals("http")&&Set.of("localhost","127.0.0.1").contains(uri.getHost());}catch(Exception invalid){return false;}});
   if(sandbox && (!Set.of(env.getActiveProfiles()).equals(Set.of("local")) || secret.length()<32 || inbox.isBlank() || (!Set.of("127.0.0.1","::1","localhost").contains(bind)&&!containerLocal)))
     throw new IllegalStateException("Sandbox identity requires only the local profile, a private inbox and a secret of at least 32 characters");
   if(live&&(hourlyLimit<1||secret.length()<32||Set.of(env.getActiveProfiles()).contains("local")))throw new IllegalStateException("Live identity needs a separate strong OTP secret and cannot use the local profile");
   if(!sandbox && this.origins.stream().anyMatch(o->{try{var uri=java.net.URI.create(o);return !"https".equals(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null||!uri.getPath().isEmpty();}catch(Exception invalid){return true;}})) throw new IllegalStateException("Production origins require HTTPS");
 }
}
