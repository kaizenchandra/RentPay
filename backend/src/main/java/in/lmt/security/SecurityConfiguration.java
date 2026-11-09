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
 @Bean SecurityFilterChain security(HttpSecurity http,IdentityConfig config,in.lmt.identity.AccountControls controls,org.springframework.core.env.Environment env) throws Exception {
   if("worker".equals(env.getProperty("lmt.process")))return http.authorizeHttpRequests(a->a.requestMatchers(HttpMethod.GET,"/actuator/health").permitAll().anyRequest().denyAll()).build();
   var csrf=new HttpSessionCsrfTokenRepository();csrf.setHeaderName("X-CSRF-Token");
   return http.csrf(c->c.csrfTokenRepository(csrf).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
     .requestCache(c->c.requestCache(new NullRequestCache())).logout(c->c.disable())
     .addFilterBefore(new OriginFilter(config,controls),CsrfFilter.class)
     .authorizeHttpRequests(a->a
       .requestMatchers(HttpMethod.GET,"/api/v1/public/plans","/api/v1/public/plans/*","/actuator/health","/api/v1/auth/session","/api/v1/auth/csrf","/api/v1/auth/challenges/*").permitAll()
       .requestMatchers(HttpMethod.GET,"/api/v1/me/notifications").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/me/notifications/*/read","/api/v1/me/session-revocation-preview","/api/v1/me/session-revocations").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/me/identity-links","/api/v1/me/identity-links/challenges","/api/v1/me/reauthentication/challenges","/api/v1/me/reauthentication/verify").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/auth/challenges","/api/v1/auth/challenges/*/verify","/api/v1/auth/recovery/challenges","/api/v1/auth/recovery/verify","/api/v1/auth/recovery/outcome").permitAll()
       .requestMatchers(HttpMethod.GET,"/api/v1/admin/workspaces/*/member-entitlement","/api/v1/admin/workspaces/*/manager-capacity-repair").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/admin/workspaces/*/member-entitlement","/api/v1/admin/workspaces/*/manager-capacity-repair").authenticated()
       .requestMatchers(HttpMethod.GET,"/api/v1/documents/*/scan").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/documents/*/scan/retry","/api/v1/documents/*/scan/cancel").authenticated()
       .requestMatchers(HttpMethod.GET,"/api/v1/documents/*/storage").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/documents/*/storage/check","/api/v1/documents/*/storage/restore").authenticated()
       .requestMatchers(HttpMethod.GET,"/api/v1/documents/upload-intents","/api/v1/documents/*").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/documents/upload-intents","/api/v1/documents/*/finalize").authenticated()
       .requestMatchers(HttpMethod.GET,"/api/v1/admin/plan-versions/*/availability").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/admin/plan-versions/*/availability").authenticated()
       .requestMatchers(HttpMethod.PATCH,"/api/v1/admin/plan-versions/*").authenticated()
       .requestMatchers(HttpMethod.GET,"/api/v1/admin/plan-versions/*/activation-preview").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/admin/plan-versions/*/activate","/api/v1/admin/plan-versions/*/discard").authenticated()
       .requestMatchers(HttpMethod.POST,"/api/v1/admin/plans/*/versions").authenticated()
       .requestMatchers(HttpMethod.GET,"/api/v1/admin/plans").authenticated()
       .requestMatchers("/api/v1/workspaces/*/operations","/api/v1/operations/*","/api/v1/operations/*/retry-failed","/api/v1/operations/*/cancel-request","/api/v1/workspaces/*/entitlements","/api/v1/workspaces/*/usage","/api/v1/workspaces/*/usage/history","/api/v1/workspaces/*/usage/repairs","/api/v1/workspaces/*/membership","/api/v1/workspaces/*/members","/api/v1/workspaces/*/members/*/revoke","/api/v1/workspaces/*/invitations","/api/v1/workspaces/*/invitations/*","/api/v1/workspaces/*/invitations/*/revoke","/api/v1/me/invitations","/api/v1/me/invitations/*/accept","/api/v1/me/invitations/*/decline","/api/v1/me/profile","/api/v1/me/sessions","/api/v1/me/sessions/*/revoke","/api/v1/me/security-notifications","/api/v1/me/contexts","/api/v1/auth/logout","/api/v1/workspaces","/api/v1/workspaces/*/setup").authenticated()
       .anyRequest().denyAll())
     .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->SecurityProblems.write(r,401,"UNAUTHENTICATED","Sign in to continue."))
       .accessDeniedHandler((q,r,x)->SecurityProblems.write(r,403,"FORBIDDEN","Access denied or security token expired. Reload and try again.")))
     .build();
 }
}
