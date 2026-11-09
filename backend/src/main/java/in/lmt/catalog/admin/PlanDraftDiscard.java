package in.lmt.catalog.admin;
import in.lmt.shared.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
@Service
public class PlanDraftDiscard {
 private final JdbcClient db;private final Audit audit;private final ObjectMapper json;
 public PlanDraftDiscard(JdbcClient db,Audit audit,ObjectMapper json){this.db=db;this.audit=audit;this.json=json;}
 public record Input(@NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*") String reason){}
 public record Result(UUID id,String state,String etag){}
 @Transactional public Result discard(UUID actor,UUID draft,UUID key,String tag,Input input){
  db.sql("SELECT pg_advisory_xact_lock(hashtext('commercial-grant'),hashtext(:a))").param("a",actor.toString()).query().singleRow();authorize(actor);
  if(tag==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review the current draft before discard.");
  if(input.reason().strip().length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Explain discard in at least 10 characters.","reason");
  String body=json.writeValueAsString(List.of(draft,tag,input));
  var prior=db.sql("SELECT request_body,result::text FROM plan_draft_discard_command WHERE actor_id=:a AND request_key=:k").param("a",actor).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){var saved=prior.getFirst();if(!body.equals(saved.get("request_body")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key was used for a different discard confirmation.");return json.readValue((String)saved.get("result"),Result.class);}
  var rows=db.sql("SELECT p.audience,p.code FROM plan_version p JOIN plan_draft_command c ON c.draft_id=p.version_id WHERE p.version_id=:id").param("id",draft).query().listOfRows();
  if(rows.isEmpty())throw new ApiProblem(404,"PLAN_DRAFT_NOT_FOUND","A source-backed draft is required.");
  var row=rows.getFirst();db.sql("SELECT pg_advisory_xact_lock(hashtext('plan-draft'),hashtext(:scope))").param("scope",row.get("audience")+":"+row.get("code")).query().singleRow();
  var before=db.sql("SELECT * FROM plan_version WHERE version_id=:id FOR UPDATE").param("id",draft).query(PlanReview::project).single();authorize(actor);
  if(!before.state().equals("draft"))throw new ApiProblem(409,"PLAN_DRAFT_NOT_DISCARDABLE","Only an active unpublished draft can be discarded.");
  if(!before.etag().equals(tag))throw new ApiProblem(412,"PLAN_DRAFT_STALE","The draft changed. Close this confirmation, refresh and review it again.");
  db.sql("UPDATE plan_version SET publication_state='discarded' WHERE version_id=:id").param("id",draft).update();
  var after=db.sql("SELECT * FROM plan_version WHERE version_id=:id").param("id",draft).query(PlanReview::project).single();var result=new Result(draft,"discarded",after.etag());
  db.sql("INSERT INTO plan_draft_discard_command(actor_id,request_key,draft_id,request_body,before_terms,result,reason) VALUES(:a,:k,:id,:b,:before::jsonb,:result::jsonb,:r)").param("a",actor).param("k",key).param("id",draft).param("b",body).param("before",json.writeValueAsString(before)).param("result",json.writeValueAsString(result)).param("r",input.reason().strip()).update();
  audit.record(actor,null,"PLAN_DRAFT_DISCARDED",draft);return result;
 }
 private void authorize(UUID actor){if(!db.sql("SELECT EXISTS(SELECT 1 FROM commercial_review_grant WHERE account_id=:a AND NOT revoked AND can_discard_draft AND valid_until>clock_timestamp())").param("a",actor).query(Boolean.class).single())throw new ApiProblem(403,"COMMERCIAL_DRAFT_DISCARD_REQUIRED","An active draft-discard grant is required.");}
}
