package in.lmt.catalog.admin;
import in.lmt.shared.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
@Service
public class PlanDraftEdits {
 private final JdbcClient db;private final Audit audit;private final ObjectMapper json;
 public PlanDraftEdits(JdbcClient db,Audit audit,ObjectMapper json){this.db=db;this.audit=audit;this.json=json;}
 public record Result(UUID id,UUID sourceId,String state,String etag){}
 @Transactional public Result update(UUID actor,UUID draft,UUID key,String tag,PlanDrafts.Input input){
  db.sql("SELECT pg_advisory_xact_lock(hashtext('commercial-grant'),hashtext(:a))").param("a",actor.toString()).query().singleRow();
  authorize(actor);
  if(tag==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review the current draft before editing.");
  if(input.reason().strip().length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Explain the edit in at least 10 characters.","reason");
  String body=json.writeValueAsString(List.of(draft,tag,input));
  var previous=db.sql("SELECT request_body,result::text FROM plan_draft_edit_command WHERE actor_id=:a AND request_key=:k").param("a",actor).param("k",key).query().listOfRows();
  if(!previous.isEmpty()){var saved=previous.getFirst();if(!body.equals(saved.get("request_body")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key was used for different draft edits.");return json.readValue((String)saved.get("result"),Result.class);}
  var rows=db.sql("SELECT p.audience,p.code,c.source_id FROM plan_version p JOIN plan_draft_command c ON c.draft_id=p.version_id WHERE p.version_id=:id").param("id",draft).query().listOfRows();
  if(rows.isEmpty())throw new ApiProblem(404,"PLAN_DRAFT_NOT_FOUND","A source-backed draft is required.");
  var row=rows.getFirst();
  db.sql("SELECT pg_advisory_xact_lock(hashtext('plan-draft'),hashtext(:scope))").param("scope",row.get("audience")+":"+row.get("code")).query().singleRow();
  var before=db.sql("SELECT * FROM plan_version WHERE version_id=:id FOR UPDATE").param("id",draft).query(PlanReview::project).single();
  authorize(actor);
  if(!before.state().equals("draft"))throw new ApiProblem(409,"PLAN_DRAFT_NOT_EDITABLE","Published or discarded versions cannot be edited. Create a new draft from the latest source.");
  if(!before.etag().equals(tag))throw new ApiProblem(412,"PLAN_DRAFT_STALE","The draft changed. Close this proposal, refresh versions and review the current terms.");
  if(!db.sql("SELECT :t>clock_timestamp()").param("t",input.effectiveAt()).query(Boolean.class).single())throw new ApiProblem(422,"VALIDATION_FAILED","Choose a future effective instant.","effectiveAt");
  if(db.sql("SELECT EXISTS(SELECT 1 FROM plan_version WHERE audience=:a AND code=:c AND version_id<>:id AND effective_at=:t)").param("a",row.get("audience")).param("c",before.code()).param("id",draft).param("t",input.effectiveAt()).query(Boolean.class).single())throw new ApiProblem(409,"PLAN_EFFECTIVE_DATE_CONFLICT","Another version already uses this effective instant.");
  db.sql("UPDATE plan_version SET display_name=:name,effective_at=:t,commercial_disclosure=:d WHERE version_id=:id").param("name",input.name().strip()).param("t",input.effectiveAt()).param("d",input.disclosure().strip()).param("id",draft).update();
  var after=db.sql("SELECT * FROM plan_version WHERE version_id=:id").param("id",draft).query(PlanReview::project).single();
  var result=new Result(draft,(UUID)row.get("source_id"),"draft",after.etag());
  db.sql("INSERT INTO plan_draft_edit_command(actor_id,request_key,draft_id,request_body,before_terms,after_terms,result,reason) VALUES(:a,:k,:d,:b,:before::jsonb,:after::jsonb,:result::jsonb,:reason)").param("a",actor).param("k",key).param("d",draft).param("b",body).param("before",json.writeValueAsString(before)).param("after",json.writeValueAsString(after)).param("result",json.writeValueAsString(result)).param("reason",input.reason().strip()).update();
  audit.record(actor,null,"PLAN_DRAFT_UPDATED",draft);return result;
 }
 private void authorize(UUID actor){if(!db.sql("SELECT EXISTS(SELECT 1 FROM commercial_review_grant WHERE account_id=:a AND NOT revoked AND can_edit_draft AND valid_until>clock_timestamp())").param("a",actor).query(Boolean.class).single())throw new ApiProblem(403,"COMMERCIAL_DRAFT_EDIT_REQUIRED","An active draft-editing grant is required.");}
}
