package in.lmt.identity;
import in.lmt.shared.Audit;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/** Internal interface: caller must authorize and commit the membership transition in this transaction. */
@Service
public class WorkspaceAccessNotices {
 private final JdbcClient db;private final Audit audit;
 public WorkspaceAccessNotices(JdbcClient db,Audit audit){this.db=db;this.audit=audit;}
 @Transactional(propagation=Propagation.MANDATORY)
 public void record(UUID actor,UUID recipient,UUID workspace,UUID event,String workspaceName,boolean granted){
  String message=granted?"You accepted manager membership in “"+workspaceName+"”. Property, signing and spending authority require separate grants.":"Your manager membership in “"+workspaceName+"” was revoked. This notice remains available; it does not restore workspace access.";
  persist(actor,recipient,workspace,event,message,"workspace_access",granted?"notification.workspace_access_granted":"notification.workspace_access_revoked");
 }
 @Transactional(propagation=Propagation.MANDATORY)
 public void invitationOutcome(UUID actor,UUID recipient,UUID workspace,UUID invitation,String workspaceName,boolean accepted){
  UUID event=UUID.nameUUIDFromBytes(("workspace.invitation_outcome:"+invitation).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  String message="Your manager invitation for “"+workspaceName+"” was "+(accepted?"accepted. Manager membership was granted; property, signing and spending authority require separate grants.":"declined. No membership or manager slot was allocated.")+" Invitation reference: "+invitation+".";
  persist(actor,recipient,workspace,event,message,"workspace_invitation",accepted?"notification.invitation_accepted":"notification.invitation_declined");
 }
 @Transactional(propagation=Propagation.MANDATORY)
 public void invitationExpired(UUID recipient,UUID workspace,UUID invitation,String workspaceName){
  UUID event=UUID.nameUUIDFromBytes(("workspace.invitation_expired:"+invitation).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  String message="Your manager invitation for “"+workspaceName+"” expired without acceptance. No membership or manager slot was allocated. Review current capacity before sending a new invitation. Invitation reference: "+invitation+".";
  persist(null,recipient,workspace,event,message,"workspace_invitation","notification.invitation_expired");
 }
 private void persist(UUID actor,UUID recipient,UUID workspace,UUID event,String message,String category,String action){
  String previous=db.sql("SELECT current_setting('lmt.account_id',true)").query(String.class).optional().orElse("");
  try{
   db.sql("SELECT set_config('lmt.account_id',:account,true)").param("account",recipient.toString()).query().singleRow();
   int inserted=db.sql("INSERT INTO account_notification(id,account_id,message,category,source_event_id) VALUES(:id,:account,:message,:category,:event) ON CONFLICT DO NOTHING")
    .param("id",UUID.randomUUID()).param("account",recipient).param("message",message).param("category",category).param("event",event).update();
   if(inserted>0)audit.record(actor,workspace,action,event);
  }finally{db.sql("SELECT set_config('lmt.account_id',:account,true)").param("account",previous).query().singleRow();}
 }
}
