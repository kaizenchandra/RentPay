package in.lmt.workspace;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import in.lmt.identity.WorkspaceAccessNotices;
import in.lmt.shared.Audit;
@Service
public class InvitationExpiry {
 private final JdbcClient db;private final WorkspaceAccessNotices notices;private final Audit audit;
 public InvitationExpiry(JdbcClient db,WorkspaceAccessNotices notices,Audit audit){this.db=db;this.notices=notices;this.audit=audit;}
 /** One bounded database reconciliation transaction; no external delivery side effects. */
 @Transactional public int expireDue(){
  var rows=db.sql("SELECT * FROM lmt_expire_due_invitations()").query().listOfRows();
  for(var row:rows){UUID id=(UUID)row.get("invitation_id"),workspace=(UUID)row.get("workspace_id"),inviter=(UUID)row.get("inviter_id");
   audit.record(null,workspace,"workspace.invitation_expired",id);
   if(db.sql("SELECT lmt_is_workspace_owner(:w,:a)").param("w",workspace).param("a",inviter).query(Boolean.class).single())notices.invitationExpired(inviter,workspace,id,(String)row.get("workspace_name"));
   else audit.record(null,workspace,"notification.invitation_expiry_suppressed",id);
  }
  return rows.size();
 }
}
