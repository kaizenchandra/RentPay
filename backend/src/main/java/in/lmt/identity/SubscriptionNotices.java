package in.lmt.identity;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
/** Caller authorizes recipient and commits the subscription transition in the same transaction. */
@Service
public class SubscriptionNotices {
 private final JdbcClient db;public SubscriptionNotices(JdbcClient db){this.db=db;}
 @Transactional(propagation=Propagation.MANDATORY)
 public void record(UUID recipient,UUID workspace,UUID request,String outcome,int version,boolean cancellation){
  UUID event=UUID.nameUUIDFromBytes(("subscription.downgrade:"+request+":"+outcome+":"+version).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  String previous=db.sql("SELECT current_setting('lmt.account_id',true)").query(String.class).single();
  try{db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",recipient.toString()).query().singleRow();db.sql("INSERT INTO account_notification(id,account_id,message,category,source_event_id) VALUES(:id,:a,:message,'workspace_subscription',:event) ON CONFLICT DO NOTHING").param("id",UUID.randomUUID()).param("a",recipient).param("message","Local workspace "+(cancellation?"cancellation":"downgrade")+" "+(cancellation&&outcome.equals("cancel")?"withdrawn":outcome)+". Request "+request+" in workspace "+workspace+". Existing records are retained; review current status and quota usage in the subscription dashboard.").param("event",event).update();}
  finally{db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",previous).query().singleRow();}
 }
}
