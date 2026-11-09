package in.lmt.commercial;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service
public class EditorialPlanCapabilities {
 private final JdbcClient db;public EditorialPlanCapabilities(JdbcClient db){this.db=db;}
 @Transactional(propagation=Propagation.MANDATORY)
 public void inherit(UUID source,UUID target){
  db.sql("INSERT INTO commercial_plan_capability(policy_version,capability,plan_version_id) SELECT policy_version,capability,:target FROM commercial_plan_capability WHERE plan_version_id=:source ON CONFLICT DO NOTHING").param("target",target).param("source",source).update();
 }
}
