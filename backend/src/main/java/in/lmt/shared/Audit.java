package in.lmt.shared;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
@Component
public class Audit {
 private final JdbcClient db;
 public Audit(JdbcClient db){this.db=db;}
 public void record(UUID actor,UUID scope,String action,UUID resource){db.sql("INSERT INTO domain_audit(id,actor_id,scope_id,action,resource_id) VALUES(:id,:actor,:scope,:action,:resource)").param("id",UUID.randomUUID()).param("actor",actor).param("scope",scope).param("action",action).param("resource",resource).update();}
}
