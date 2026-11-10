package in.lmt.security;
import in.lmt.identity.IdentityConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
class OriginFilter extends OncePerRequestFilter {
 private final IdentityConfig config;private final in.lmt.identity.AccountControls controls;
 OriginFilter(IdentityConfig config,in.lmt.identity.AccountControls controls){this.config=config;this.controls=controls;}
 @Override protected void doFilterInternal(HttpServletRequest q,HttpServletResponse r,FilterChain chain)throws ServletException,IOException{
   if(q.getRequestURI().startsWith("/api/v1/")&&!q.getRequestURI().startsWith("/api/v1/public/"))r.setHeader("Cache-Control","no-store");
   var session=q.getSession(false);
   if(!config.sandbox && !config.live && SecurityContextHolder.getContext().getAuthentication()!=null){if(session!=null)session.invalidate();session=null;SecurityContextHolder.clearContext();}
   if(session!=null && session.getAttribute("authenticatedAt") instanceof Long at && Instant.now().getEpochSecond()-at>=43200){session.invalidate();session=null;SecurityContextHolder.clearContext();}
   var auth=SecurityContextHolder.getContext().getAuthentication();
   if(session!=null && auth!=null && (!controls.sessionAllowed(java.util.UUID.fromString(auth.getName()),session.getId())||!controls.identityModeAllowed(java.util.UUID.fromString(auth.getName()),config.mode))){
     session.invalidate();SecurityContextHolder.clearContext();
   }
   if(!java.util.Set.of("GET","HEAD","OPTIONS").contains(q.getMethod())&&(q.getHeader("Origin")==null||!config.origins.contains(q.getHeader("Origin")))){SecurityProblems.write(r,403,"ORIGIN_DENIED","The request origin is not allowed.");return;}
   chain.doFilter(q,r);
 }
}
