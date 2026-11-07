package in.lmt.security;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
final class SecurityProblems {
 static void write(HttpServletResponse r,int status,String code,String detail)throws IOException{
  r.setStatus(status);r.setContentType("application/problem+json");r.setHeader("Cache-Control","no-store");
  r.getWriter().write("{\"status\":"+status+",\"code\":\""+code+"\",\"detail\":\""+detail+"\",\"fieldErrors\":[],\"correlationId\":\""+UUID.randomUUID()+"\"}");
 }
}
