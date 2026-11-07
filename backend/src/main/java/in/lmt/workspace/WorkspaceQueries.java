package in.lmt.workspace;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class WorkspaceQueries {
 private final JdbcClient db;private final WorkspaceCommands authority;
 public WorkspaceQueries(JdbcClient db,WorkspaceCommands authority){this.db=db;this.authority=authority;}
 public record Context(UUID id,String name,String role,String mode){}
 public record Grant(String resource,int allowance,int used,int reserved){}
 public record Setup(UUID id,String name,String city,String state,String mode,String status,UUID planVersionId,String planName,List<Grant> grants,List<String> completedSteps,List<String> pendingSteps,List<String> notifications){}
 public record Page(int limit,String nextCursor){}
 public record Contexts(List<Context> items,Page page,int totalMatching,java.time.Instant asOf){}
 @Transactional(readOnly=true) public Contexts contexts(UUID actor,String cursor){
  authority.scope(actor);UUID after=new UUID(0,0);
  if(cursor!=null){try{String decoded=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8);String[] parts=decoded.split(":");if(parts.length!=2||!parts[0].equals(actor.toString()))throw new IllegalArgumentException();after=UUID.fromString(parts[1]);}catch(IllegalArgumentException e){throw new in.lmt.shared.ApiProblem(422,"INVALID_CURSOR","Reload your workspace list.","cursor");}}
  var rows=db.sql("SELECT w.id,w.name,m.role,w.mode FROM workspace w JOIN workspace_member m ON m.workspace_id=w.id WHERE m.account_id=:a AND m.active AND w.id>:after ORDER BY w.id LIMIT 101").param("a",actor).param("after",after).query((r,n)->new Context(r.getObject(1,UUID.class),r.getString(2),r.getString(3),r.getString(4))).list();
  boolean more=rows.size()>100;var items=more?rows.subList(0,100):rows;
  String next=more?Base64.getUrlEncoder().withoutPadding().encodeToString((actor+":"+items.getLast().id()).getBytes(java.nio.charset.StandardCharsets.UTF_8)):null;
  int total=db.sql("SELECT count(*) FROM workspace_member WHERE account_id=:a AND active").param("a",actor).query(Integer.class).single();
  return new Contexts(items,new Page(100,next),total,java.time.Instant.now());
 }
 @Transactional(readOnly=true) public Setup setup(UUID actor,UUID id){
  authority.requireOwner(id,actor);
  var w=db.sql("SELECT w.*,s.plan_version_id,p.display_name FROM workspace w JOIN workspace_subscription s ON s.workspace_id=w.id JOIN plan_version p ON p.version_id=s.plan_version_id WHERE w.id=:id").param("id",id).query().singleRow();
  var grants=db.sql("SELECT resource,allowance,used,reserved FROM workspace_grant WHERE workspace_id=:id ORDER BY resource").param("id",id).query((r,n)->new Grant(r.getString(1),r.getInt(2),r.getInt(3),r.getInt(4))).list();
  var notices=db.sql("SELECT message FROM workspace_notification WHERE workspace_id=:id ORDER BY created_at,event_id LIMIT 20").param("id",id).query(String.class).list();
  return new Setup(id,(String)w.get("name"),(String)w.get("city"),(String)w.get("state"),(String)w.get("mode"),(String)w.get("status"),(UUID)w.get("plan_version_id"),(String)w.get("display_name"),grants,List.of("workspace_profile","free_subscription"),List.of("first_property","first_unit","optional_existing_tenant","rent_and_charges","notification_preferences"),notices);
 }
}
