package in.lmt.security;

import in.lmt.identity.IdentityConfig;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class SecurityConfiguration {
 @Bean org.springframework.security.core.userdetails.UserDetailsService noPasswordAccounts(){return username->{throw new org.springframework.security.core.userdetails.UsernameNotFoundException("Password login is unavailable");};}
 @Bean DefaultCookieSerializer cookieSerializer(IdentityConfig config){
   var c=new DefaultCookieSerializer();c.setCookieName(config.sandbox?"lmt_dev_session":"__Host-lmt_session");
   c.setUseSecureCookie(!config.sandbox);c.setUseHttpOnlyCookie(true);c.setSameSite("Lax");c.setCookiePath("/");c.setUseBase64Encoding(false);return c;
 }
 @Bean SecurityFilterChain security(HttpSecurity http,IdentityConfig config,in.lmt.identity.AccountControls controls) throws Exception {
   var csrf=new HttpSessionCsrfTokenRepository();csrf.setHeaderName("X-CSRF-Token");
   return http.csrf(c->c.csrfTokenRepository(csrf).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
     .requestCache(c->c.requestCache(new NullRequestCache())).logout(c->c.disable())
     .addFilterBefore(new OriginFilter(config,controls),CsrfFilter.class)
     .authorizeHttpRequests(a->a
       .requestMatchers(HttpMethod.GET,"/api/v1/public/plans","/api/v1/public/plans/*","/actuator/health","/api/v1/auth/session","/api/v1/auth/csrf","/api/v1/auth/challenges/*").permitAll()
       .requestMatchers(HttpMethod.POST,"/api/v1/auth/challenges","/api/v1/auth/challenges/*/verify").permitAll()
       .requestMatchers("/api/v1/workspaces/*/operations","/api/v1/operations/*","/api/v1/operations/*/retry-failed","/api/v1/operations/*/cancel-request","/api/v1/workspaces/*/entitlements","/api/v1/workspaces/*/usage","/api/v1/workspaces/*/usage/history","/api/v1/workspaces/*/membership","/api/v1/workspaces/*/members","/api/v1/workspaces/*/members/*/revoke","/api/v1/workspaces/*/invitations","/api/v1/workspaces/*/invitations/*","/api/v1/workspaces/*/invitations/*/revoke","/api/v1/me/invitations","/api/v1/me/invitations/*/accept","/api/v1/me/invitations/*/decline","/api/v1/me/profile","/api/v1/me/sessions","/api/v1/me/sessions/*/revoke","/api/v1/me/security-notifications","/api/v1/me/contexts","/api/v1/auth/logout","/api/v1/workspaces","/api/v1/workspaces/*/setup").authenticated()
       .anyRequest().denyAll())
     .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->SecurityProblems.write(r,401,"UNAUTHENTICATED","Sign in to continue."))
       .accessDeniedHandler((q,r,x)->SecurityProblems.write(r,403,"FORBIDDEN","Access denied or security token expired. Reload and try again.")))
     .build();
 }
}
