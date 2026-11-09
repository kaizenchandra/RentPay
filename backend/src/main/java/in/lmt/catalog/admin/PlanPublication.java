package in.lmt.catalog.admin;
import in.lmt.shared.*;
import in.lmt.commercial.EditorialPlanCapabilities;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class PlanPublication {
 private final JdbcClient db;private final Audit audit;private final EditorialPlanCapabilities capabilities;private final ObjectMapper json;
 public PlanPublication(JdbcClient db,Audit audit,EditorialPlanCapabilities capabilities,ObjectMapper json){this.db=db;this.audit=audit;this.capabilities=capabilities;this.json=json;}
 public record Preview(UUID id,PlanReview.Version draft,PlanReview.Version source,Instant expiresAt,boolean offeringAvailable){}
 public record Input(@NotNull UUID reviewId,@NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*") String reason){}
 public record Result(UUID id,String state,Instant effectiveAt,String etag){}
 private void authorize(UUID actor){
  db.sql("SELECT pg_advisory_xact_lock(hashtext('commercial-grant'),hashtext(:a))").param("a",actor.toString()).query().singleRow();
  if(!db.sql("SELECT EXISTS(SELECT 1 FROM commercial_review_grant WHERE account_id=:a AND NOT revoked AND can_publish AND valid_until>clock_timestamp())").param("a",actor).query(Boolean.class).single())throw new ApiProblem(403,"COMMERCIAL_PUBLICATION_REQUIRED","An active publication grant is required.");
 }
 private UUID source(UUID draft){return db.sql("SELECT source_id FROM plan_draft_command WHERE draft_id=:d").param("d",draft).query(UUID.class).optional().orElseThrow(()->new ApiProblem(409,"EDITORIAL_SOURCE_REQUIRED","Only source-backed editorial drafts can be published."));}
 private PlanReview.Version version(UUID id){return db.sql("SELECT * FROM plan_version WHERE version_id=:id").param("id",id).query(PlanReview::project).optional().orElseThrow(()->new ApiProblem(404,"PLAN_NOT_FOUND","Plan version is unavailable."));}
 private Map<String,Object> offering(UUID draft){return db.sql("SELECT COALESCE(o.available,true) AS available,COALESCE(o.revision,0) AS revision FROM plan_version p LEFT JOIN plan_offering_availability o ON o.audience=p.audience AND o.code=p.code WHERE p.version_id=:id").param("id",draft).query().singleRow();}
 private void validate(UUID draft,UUID source){
  var row=db.sql("SELECT audience,code FROM plan_version WHERE version_id=:d").param("d",draft).query().singleRow();
  db.sql("SELECT pg_advisory_xact_lock(hashtext('plan-draft'),hashtext(:scope))").param("scope",row.get("audience")+":"+row.get("code")).query().singleRow();
  // Keep the draft stable while validating, publishing and copying its source grants.
  db.sql("SELECT version_id FROM plan_version WHERE version_id=:d FOR UPDATE").param("d",draft).query(UUID.class).single();
  boolean valid=db.sql("""
   SELECT d.publication_state='draft' AND s.publication_state='published'
    AND d.audience=s.audience AND d.code=s.code AND d.currency=s.currency
    AND d.baseline_only=s.baseline_only AND d.rentable_limit IS NOT DISTINCT FROM s.rentable_limit
    AND d.manager_limit IS NOT DISTINCT FROM s.manager_limit AND d.monthly_amount IS NOT DISTINCT FROM s.monthly_amount
    AND d.annual_amount IS NOT DISTINCT FROM s.annual_amount
    AND d.effective_at>clock_timestamp() AND d.effective_at>s.effective_at
    AND s.version_id=(SELECT version_id FROM plan_version WHERE audience=d.audience AND code=d.code AND publication_state='published' ORDER BY effective_at DESC LIMIT 1)
   FROM plan_version d JOIN plan_version s ON s.version_id=:s WHERE d.version_id=:d
   """).param("d",draft).param("s",source).query(Boolean.class).single();
  if(!valid)throw new ApiProblem(409,"APPROVAL_STALE","The source, proposed date or inherited terms are no longer eligible. Review the latest published source and prepare a new draft.");
 }
 @Transactional public Preview preview(UUID actor,UUID draft){
  authorize(actor);version(draft);UUID source=source(draft);validate(draft,source);
  var candidate=version(draft);UUID id=UUID.randomUUID();
  Instant expiry=db.sql("SELECT clock_timestamp()+interval '5 minutes'").query(OffsetDateTime.class).single().toInstant();
  int revision=db.sql("SELECT row_revision FROM plan_version WHERE version_id=:d").param("d",draft).query(Integer.class).single();
  var availability=offering(draft);
  db.sql("INSERT INTO plan_activation_review(id,actor_id,draft_id,draft_revision,source_id,expires_at,offering_revision) VALUES(:id,:a,:d,:r,:s,:e,:o)").param("id",id).param("a",actor).param("d",draft).param("r",revision).param("s",source).param("e",OffsetDateTime.ofInstant(expiry,ZoneOffset.UTC)).param("o",availability.get("revision")).update();
  audit.record(actor,null,"PLAN_PUBLICATION_REVIEWED",draft);return new Preview(id,candidate,version(source),expiry,(Boolean)availability.get("available"));
 }
 @Transactional public Result activate(UUID actor,UUID draft,UUID key,String etag,Input input){
  authorize(actor);
  if(input.reason().strip().length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Explain publication in at least 10 characters.","reason");
  if(etag==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review the exact draft before publication.");
  String body=json.writeValueAsString(List.of(draft,etag,input));
  var prior=db.sql("SELECT request_body,result::text FROM plan_activation_command WHERE actor_id=:a AND request_key=:k").param("a",actor).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){var saved=prior.getFirst();if(!body.equals(saved.get("request_body")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key was used for a different publication confirmation.");return json.readValue((String)saved.get("result"),Result.class);}
  var reviews=db.sql("SELECT * FROM plan_activation_review WHERE id=:id AND actor_id=:a AND draft_id=:d AND expires_at>clock_timestamp()").param("id",input.reviewId()).param("a",actor).param("d",draft).query().listOfRows();
  if(reviews.isEmpty())throw new ApiProblem(409,"APPROVAL_STALE","The publication review is unavailable or expired. Review again.");
  var review=reviews.getFirst();UUID source=(UUID)review.get("source_id");validate(draft,source);
  authorize(actor);
  if(!db.sql("SELECT expires_at>clock_timestamp() FROM plan_activation_review WHERE id=:id").param("id",input.reviewId()).query(Boolean.class).single())throw new ApiProblem(409,"APPROVAL_STALE","The publication review expired while waiting. Review again.");
  var candidate=version(draft);
  if(!candidate.etag().equals(etag)||!etag.equals(PlanDrafts.tag(draft,(Integer)review.get("draft_revision"))))throw new ApiProblem(412,"APPROVAL_STALE","The draft changed after review. Review its current version.");
  if(!offering(draft).get("revision").equals(review.get("offering_revision")))throw new ApiProblem(409,"APPROVAL_STALE","Offering availability changed after review. Review the publication effect again.");
  capabilities.inherit(source,draft);
  db.sql("UPDATE plan_version SET publication_state='published' WHERE version_id=:d").param("d",draft).update();
  var published=version(draft);var result=new Result(draft,"published",published.effectiveAt(),published.etag());
  db.sql("INSERT INTO plan_activation_command(actor_id,request_key,draft_id,review_id,request_body,result,reason) VALUES(:a,:k,:d,:r,:b,:result::jsonb,:reason)").param("a",actor).param("k",key).param("d",draft).param("r",input.reviewId()).param("b",body).param("result",json.writeValueAsString(result)).param("reason",input.reason().strip()).update();
  audit.record(actor,null,"PLAN_VERSION_PUBLISHED",draft);return result;
 }
}
