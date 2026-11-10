package in.lmt.workspace;
import in.lmt.shared.*;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
@Service
public class WorkspaceCommands {
 private final JdbcClient db;
 public WorkspaceCommands(JdbcClient db){this.db=db;}
 public void scope(UUID account){db.sql("SELECT set_config('lmt.account_id',:id,true)").param("id",account.toString()).query().singleRow();}
 public void create(UUID id,UUID actor,String name,String type,String city,String state,String mode){
  db.sql("INSERT INTO workspace(id,name,customer_type,city,state,mode) VALUES(:id,:n,:t,:c,:s,:m)").param("id",id).param("n",name).param("t",type).param("c",city).param("s",state).param("m",mode).update();
  db.sql("INSERT INTO workspace_member(workspace_id,account_id,role) VALUES(:w,:a,'OWNER')").param("w",id).param("a",actor).update();
  db.sql("INSERT INTO workspace_settings(workspace_id) VALUES(:w)").param("w",id).update();
  db.sql("INSERT INTO workspace_consent(workspace_id,account_id,policy_version) VALUES(:w,:a,'sandbox-use-v1')").param("w",id).param("a",actor).update();
 }
 public void requireMember(UUID id,UUID actor){scope(actor);if(!db.sql("SELECT EXISTS(SELECT 1 FROM workspace_member WHERE workspace_id=:w AND account_id=:a AND active)").param("w",id).param("a",actor).query(Boolean.class).single())throw new ApiProblem(404,"WORKSPACE_NOT_FOUND","Workspace is unavailable.");}
 public void requireOwner(UUID id,UUID actor){
  scope(actor);
  if(!db.sql("SELECT EXISTS(SELECT 1 FROM workspace_member WHERE workspace_id=:w AND account_id=:a AND active AND role='OWNER')").param("w",id).param("a",actor).query(Boolean.class).single())throw new ApiProblem(404,"WORKSPACE_NOT_FOUND","Workspace is unavailable.");
 }
}
