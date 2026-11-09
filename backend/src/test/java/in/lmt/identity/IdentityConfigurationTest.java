package in.lmt.identity;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
class IdentityConfigurationTest {
 @Test void rejectsSandboxInProductionOrNonLoopback(){
  var env=new MockEnvironment();env.setActiveProfiles("production");
  assertThatThrownBy(()->new IdentityConfig(env,"sandbox","x".repeat(32),"/tmp/test","http://localhost:3000","127.0.0.1")).isInstanceOf(IllegalStateException.class);
  env.setActiveProfiles("local","production");
  assertThatThrownBy(()->new IdentityConfig(env,"sandbox","x".repeat(32),"/tmp/test","http://localhost:3000","127.0.0.1")).isInstanceOf(IllegalStateException.class);
  env.setActiveProfiles("local");
  assertThatThrownBy(()->new IdentityConfig(env,"sandbox","x".repeat(32),"/tmp/test","http://localhost:3000","0.0.0.0")).isInstanceOf(IllegalStateException.class);
  assertThatThrownBy(()->new IdentityConfig(env,"unavailable","","","http://localhost:3000","127.0.0.1")).isInstanceOf(IllegalStateException.class);
 }
 @Test void containerExceptionRequiresExplicitLocalProfileAndLoopbackOrigin(){
  var env=new MockEnvironment().withProperty("lmt.container-sandbox","true");env.setActiveProfiles("local");
  assertThatCode(()->new IdentityConfig(env,"sandbox","x".repeat(32),"/data/otp-inbox","http://localhost:3100","0.0.0.0")).doesNotThrowAnyException();
  assertThatThrownBy(()->new IdentityConfig(env,"sandbox","x".repeat(32),"/data/otp-inbox","http://example.com","0.0.0.0")).isInstanceOf(IllegalStateException.class);
  env.setActiveProfiles("production");
  assertThatThrownBy(()->new IdentityConfig(env,"sandbox","x".repeat(32),"/data/otp-inbox","http://localhost:3100","0.0.0.0")).isInstanceOf(IllegalStateException.class);
 }
}
