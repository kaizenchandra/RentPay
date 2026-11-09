package in.lmt.workspace;

import in.lmt.commercial.ManagerCapacity;
import in.lmt.identity.ChallengeService;
import in.lmt.shared.*;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Account-scoped invitation and owner-scoped membership application use cases. */
@Service
@Transactional
public class WorkspaceMembership {
 private final JdbcClient db; private final WorkspaceCommands authority; private final ManagerCapacity capacity; private final in.lmt.commercial.Entitlements entitlements;
 private final Audit audit; private final ObjectMapper json; private final int lifetime;
 public WorkspaceMembership(JdbcClient db,WorkspaceCommands authority,ManagerCapacity capacity,in.lmt.commercial.Entitlements entitlements,Audit audit,ObjectMapper json,
   @Value("${lmt.workspace.invitation-hours:168}") int lifetime){
  this.db=db;this.authority=authority;this.capacity=capacity;this.entitlements=entitlements;this.audit=audit;this.json=json;
  if(lifetime<1||lifetime>720)throw new IllegalArgumentException("Invitation lifetime must be 1–720 hours");this.lifetime=lifetime;
 }
 public record Member(UUID id,String displayName,String role,boolean active,int version,String etag,Instant joinedAt){}
 public record Invitation(UUID id,UUID workspaceId,String workspaceName,String maskedMobile,String channel,String maskedDestination,String role,String scope,String status,Instant expiresAt,int version,String etag){}
 public record Page(int limit,String nextCursor){}
 public record Invitations(List<Invitation> items,Page page,int totalMatching,Instant asOf){}
 public record Members(List<Member> items,Page page,int totalMatching,Instant asOf,String workspaceName,int managerAllowance,int managersUsed,int invitationHours){}
 public record Context(UUID id,String name,String role,String mode,String scope){}
 public record Outcome(UUID workspaceId,UUID resourceId,String status){}
 private static String etag(String kind,UUID id,int version){return "\""+kind+"-"+id+"-v"+version+"\"";}
 private void match(String expected,String actual){if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Reload before confirming.");if(!expected.equals(actual))throw new ApiProblem(412,"VERSION_STALE","This record changed. Reload and review it.");}
 private void lock(UUID actor,UUID workspace){authority.scope(actor);db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k","membership:"+workspace).query().singleRow();}
 private Optional<Outcome> replay(UUID actor,UUID w,String op,UUID key,String hash){
  var prior=db.sql("SELECT request_hash,result::text FROM membership_command WHERE actor_id=:a AND workspace_id=:w AND operation=:o AND request_key=:k")
   .param("a",actor).param("w",w).param("o",op).param("k",key).query().listOfRows().stream().findFirst();
  if(prior.isEmpty())return Optional.empty();if(!hash.equals(prior.get().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different confirmation.");
  return Optional.of(json.readValue((String)prior.get().get("result"),Outcome.class));
 }
 private Outcome record(UUID actor,UUID w,String op,UUID key,String hash,UUID resource,String status){
  Outcome result=new Outcome(w,resource,status);
  db.sql("INSERT INTO membership_command(actor_id,workspace_id,operation,request_key,request_hash,result) VALUES(:a,:w,:o,:k,:h,CAST(:r AS jsonb))")
   .param("a",actor).param("w",w).param("o",op).param("k",key).param("h",hash).param("r",json.writeValueAsString(result)).update();
  audit.record(actor,w,op,resource);return result;
 }
 private String openWorkspace(UUID actor,UUID w){
  authority.requireOwner(w,actor);
  var row=db.sql("SELECT name,status,mode FROM workspace WHERE id=:w").param("w",w).query().singleRow();
  if(!row.get("mode").equals("sandbox"))throw new ApiProblem(403,"VERIFICATION_REQUIRED","Live invitations require approved identity verification.");
  entitlements.require(w,"WORKSPACE_MEMBER_ADD");
  return (String)row.get("name");
 }
 private UUID after(String cursor,String context){
  if(cursor==null)return new UUID(0,0);
  try{String[] bits=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8).split("/");if(bits.length!=2||!bits[0].equals(context))throw new IllegalArgumentException();return UUID.fromString(bits[1]);}
  catch(IllegalArgumentException e){throw new ApiProblem(422,"INVALID_CURSOR","Reload this list.","cursor");}
 }
 private Page page(boolean more,UUID last,String context){return new Page(25,more?Base64.getUrlEncoder().withoutPadding().encodeToString((context+"/"+last).getBytes(java.nio.charset.StandardCharsets.UTF_8)):null);}
 private Invitation invitation(java.sql.ResultSet r)throws java.sql.SQLException {
  UUID id=r.getObject("id",UUID.class);int v=r.getInt("version");String mobile=r.getString("mobile"),email=r.getString("email");String masked=mobile==null?ChallengeService.maskEmail(email):"+91••••••"+mobile.substring(mobile.length()-4);String status=r.getString("status");
  if(status.equals("accepted")&&!r.getBoolean("membership_active"))status="access_revoked";
  if(status.equals("pending")&&!r.getTimestamp("expires_at").toInstant().isAfter(Instant.now()))status="expired";
  return new Invitation(id,r.getObject("workspace_id",UUID.class),r.getString("workspace_name"),mobile==null?null:masked,mobile==null?"email":"mobile",masked,"MANAGER","membership_only",status,r.getTimestamp("expires_at").toInstant(),v,etag("invitation",id,v));
 }
 public Context context(UUID actor,UUID w){
  authority.scope(actor);
  return db.sql("SELECT w.id,w.name,w.mode,m.role FROM workspace w JOIN workspace_member m ON m.workspace_id=w.id WHERE w.id=:w AND m.account_id=:a AND m.active")
   .param("w",w).param("a",actor).query((r,n)->new Context(w,r.getString("name"),r.getString("role"),r.getString("mode"),r.getString("role").equals("OWNER")?"workspace_administration":"membership_only")).optional()
   .orElseThrow(()->new ApiProblem(404,"WORKSPACE_NOT_FOUND","Workspace is unavailable."));
 }
 @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
 public Members members(UUID actor,UUID w,String cursor){
  authority.requireOwner(w,actor);String context=actor+":"+w+":members";
  var rows=db.sql("SELECT m.*,u.display_name FROM workspace_member m JOIN user_account u ON u.id=m.account_id WHERE m.workspace_id=:w AND m.id>:after ORDER BY m.id LIMIT 26")
   .param("w",w).param("after",after(cursor,context)).query((r,n)->new Member(r.getObject("id",UUID.class),r.getString("display_name"),r.getString("role"),r.getBoolean("active"),r.getInt("version"),etag("member",r.getObject("id",UUID.class),r.getInt("version")),r.getTimestamp("joined_at").toInstant())).list();
  boolean more=rows.size()>25;var items=more?rows.subList(0,25):rows;
  var grant=db.sql("SELECT allowance,used FROM workspace_grant WHERE workspace_id=:w AND resource='managers'").param("w",w).query().singleRow();
  String name=db.sql("SELECT name FROM workspace WHERE id=:w").param("w",w).query(String.class).single();
  int total=db.sql("SELECT count(*) FROM workspace_member WHERE workspace_id=:w").param("w",w).query(Integer.class).single();
  return new Members(items,page(more,items.isEmpty()?new UUID(0,0):items.getLast().id(),context),total,Instant.now(),name,((Number)grant.get("allowance")).intValue(),((Number)grant.get("used")).intValue(),lifetime);
 }
 @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
 public Invitations invitations(UUID actor,UUID workspace,String cursor){
  authority.scope(actor);if(workspace!=null)authority.requireOwner(workspace,actor);
  String context=actor+":"+(workspace==null?"inbox":workspace);
  String where=workspace==null?"(mobile=(SELECT mobile FROM user_account WHERE id=:selector) OR email=(SELECT email FROM user_account WHERE id=:selector))":"workspace_id=:selector";
  Object selector=workspace==null?actor:workspace;
  var rows=db.sql("SELECT *,EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=workspace_invitation.workspace_id AND m.account_id=workspace_invitation.accepted_by AND m.active) AS membership_active FROM workspace_invitation WHERE "+where+" AND id>:after ORDER BY id LIMIT 26").param("selector",selector).param("after",after(cursor,context)).query((r,n)->invitation(r)).list();
  boolean more=rows.size()>25;var items=more?rows.subList(0,25):rows;
  int total=db.sql("SELECT count(*) FROM workspace_invitation WHERE "+where).param("selector",selector).query(Integer.class).single();
  return new Invitations(items,page(more,items.isEmpty()?new UUID(0,0):items.getLast().id(),context),total,Instant.now());
 }
 public Invitation invitationDetail(UUID actor,UUID w,UUID id){
  authority.requireOwner(w,actor);
  return db.sql("SELECT *,EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=workspace_invitation.workspace_id AND m.account_id=workspace_invitation.accepted_by AND m.active) AS membership_active FROM workspace_invitation WHERE workspace_id=:w AND id=:id")
   .param("w",w).param("id",id).query((r,n)->invitation(r)).optional().orElseThrow(()->new ApiProblem(404,"INVITATION_NOT_FOUND","Invitation is unavailable."));
 }
 public Outcome invite(UUID actor,UUID w,UUID key,String mobile,String email){
  if((mobile==null)==(email==null))throw new ApiProblem(422,"INVITATION_CONTACT_REQUIRED","Supply exactly one mobile number or email address.","email");
  String channel=mobile==null?"email":"mobile",contact;
  try{contact=ChallengeService.normalize(channel,mobile==null?email:mobile);}catch(ApiProblem invalid){throw new ApiProblem(422,"INVALID_CONTACT","Enter a supported contact for this invitation.",channel);}
  lock(actor,w);String name=openWorkspace(actor,w);String hash=ChallengeService.hash(channel.equals("mobile")?contact:"email:"+contact);
  var prior=replay(actor,w,"workspace.invited",key,hash);if(prior.isPresent())return prior.get();
  boolean member=db.sql("SELECT EXISTS(SELECT 1 FROM workspace_member m JOIN user_account u ON u.id=m.account_id WHERE m.workspace_id=:w AND m.active AND u."+channel+"=:contact)").param("w",w).param("contact",contact).query(Boolean.class).single();
  if(member)throw new ApiProblem(409,"ALREADY_MEMBER","This contact already has active workspace membership.");
  boolean pending=db.sql("SELECT EXISTS(SELECT 1 FROM workspace_invitation WHERE workspace_id=:w AND "+channel+"=:contact AND status='pending' AND expires_at>CURRENT_TIMESTAMP)").param("w",w).param("contact",contact).query(Boolean.class).single();
  if(pending)throw new ApiProblem(409,"INVITATION_PENDING","An unexpired invitation already exists for this contact.");
  UUID id=UUID.randomUUID();db.sql("INSERT INTO workspace_invitation(id,workspace_id,workspace_name,invited_by,mobile,email,expires_at) VALUES(:id,:w,:name,:a,:mobile,:email,CURRENT_TIMESTAMP+(:hours * interval '1 hour'))")
   .param("id",id).param("w",w).param("name",name).param("a",actor).param("mobile",channel.equals("mobile")?contact:null,java.sql.Types.VARCHAR).param("email",channel.equals("email")?contact:null,java.sql.Types.VARCHAR).param("hours",lifetime).update();
  return record(actor,w,"workspace.invited",key,hash,id,"invited");
 }
 private Map<String,Object> recipient(UUID actor,UUID id){
  authority.scope(actor);
  return db.sql("SELECT * FROM workspace_invitation WHERE id=:id AND (mobile=(SELECT mobile FROM user_account WHERE id=:a) OR email=(SELECT email FROM user_account WHERE id=:a))")
   .param("id",id).param("a",actor).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"INVITATION_NOT_FOUND","Invitation is unavailable."));
 }
 public Outcome respond(UUID actor,UUID id,UUID key,String expected,boolean accept){
  var initial=recipient(actor,id);UUID w=(UUID)initial.get("workspace_id");lock(actor,w);var row=recipient(actor,id);
  String op=accept?"workspace.invitation_accepted":"workspace.invitation_declined",hash=ChallengeService.hash(id+":"+expected);
  var prior=replay(actor,w,op,key,hash);if(prior.isPresent()){
   if(accept&&!db.sql("SELECT EXISTS(SELECT 1 FROM workspace_member WHERE workspace_id=:w AND account_id=:a AND active)").param("w",w).param("a",actor).query(Boolean.class).single())throw new ApiProblem(404,"MEMBERSHIP_UNAVAILABLE","Membership is no longer active.");
   return prior.get();
  }
  match(expected,etag("invitation",id,((Number)row.get("version")).intValue()));
  if(!row.get("status").equals("pending")||!((java.sql.Timestamp)row.get("expires_at")).toInstant().isAfter(Instant.now()))throw new ApiProblem(409,"INVITATION_UNAVAILABLE","Invitation is no longer pending or has expired. Ask the owner for a new invitation.");
  if(accept){
   if(!db.sql("SELECT lmt_is_workspace_owner(:w,:a)").param("w",w).param("a",row.get("invited_by")).query(Boolean.class).single())throw new ApiProblem(409,"INVITER_REVOKED","The inviting owner's authority is no longer active.");
   // Self-scoped insert first: grant access exists only inside this transaction until quota succeeds.
   var existing=db.sql("SELECT id,active,role FROM workspace_member WHERE workspace_id=:w AND account_id=:a FOR UPDATE").param("w",w).param("a",actor).query().listOfRows();
   UUID member;
   if(existing.isEmpty()){
    member=UUID.randomUUID();db.sql("INSERT INTO workspace_member(id,workspace_id,account_id,role) VALUES(:id,:w,:a,'MANAGER')").param("id",member).param("w",w).param("a",actor).update();
   }else{
    var m=existing.getFirst();if((Boolean)m.get("active")||!m.get("role").equals("MANAGER"))throw new ApiProblem(409,"ALREADY_MEMBER","Existing membership cannot be replaced by this invitation.");
    member=(UUID)m.get("id");db.sql("UPDATE workspace_member SET active=true,version=version+1,joined_at=CURRENT_TIMESTAMP WHERE id=:id").param("id",member).update();
   }
   var state=db.sql("SELECT status,mode FROM workspace WHERE id=:w").param("w",w).query().singleRow();
   if(!state.get("mode").equals("sandbox"))throw new ApiProblem(409,"WORKSPACE_RESTRICTED","This workspace cannot add managers.");
   entitlements.require(w,"WORKSPACE_MEMBER_ADD");
   capacity.consume(w,member,id);
  }
  db.sql("UPDATE workspace_invitation SET status=:s,accepted_by=:a,version=version+1 WHERE id=:id")
   .param("s",accept?"accepted":"declined").param("a",accept?actor:null,java.sql.Types.OTHER).param("id",id).update();
  return record(actor,w,op,key,hash,id,accept?"accepted":"declined");
 }
 public Outcome revokeInvite(UUID actor,UUID w,UUID id,UUID key,String expected){
  lock(actor,w);authority.requireOwner(w,actor);String hash=ChallengeService.hash(id+":"+expected);
  var prior=replay(actor,w,"workspace.invitation_revoked",key,hash);if(prior.isPresent())return prior.get();
  var row=db.sql("SELECT status,version FROM workspace_invitation WHERE id=:id AND workspace_id=:w").param("id",id).param("w",w).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"INVITATION_NOT_FOUND","Invitation is unavailable."));
  match(expected,etag("invitation",id,((Number)row.get("version")).intValue()));
  if(!row.get("status").equals("pending"))throw new ApiProblem(409,"INVITATION_UNAVAILABLE","This invitation is no longer pending.");
  db.sql("UPDATE workspace_invitation SET status='revoked',version=version+1 WHERE id=:id").param("id",id).update();
  return record(actor,w,"workspace.invitation_revoked",key,hash,id,"revoked");
 }
 public Outcome revokeMember(UUID actor,UUID w,UUID id,UUID key,String expected){
  lock(actor,w);authority.requireOwner(w,actor);String hash=ChallengeService.hash(id+":"+expected);
  var prior=replay(actor,w,"workspace.member_revoked",key,hash);if(prior.isPresent())return prior.get();
  var row=db.sql("SELECT * FROM workspace_member WHERE workspace_id=:w AND id=:id FOR UPDATE").param("w",w).param("id",id).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"MEMBER_NOT_FOUND","Membership is unavailable."));
  match(expected,etag("member",id,((Number)row.get("version")).intValue()));
  if(!row.get("role").equals("MANAGER"))throw new ApiProblem(409,"OWNER_PROTECTED","Owner membership cannot be revoked through this manager operation.");
  if(!(Boolean)row.get("active"))throw new ApiProblem(409,"MEMBERSHIP_UNAVAILABLE","Membership is already inactive.");
  capacity.release(w,id);
  db.sql("UPDATE workspace_member SET active=false,version=version+1 WHERE id=:id").param("id",id).update();
  return record(actor,w,"workspace.member_revoked",key,hash,id,"revoked");
 }
}
