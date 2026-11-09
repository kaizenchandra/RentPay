package in.lmt.catalog.admin;
import in.lmt.shared.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
@Service
public class OfferingAvailability {
 private final JdbcClient db;private final Audit audit;private final ObjectMapper json;
 public OfferingAvailability(JdbcClient db,Audit audit,ObjectMapper json){this.db=db;this.audit=audit;this.json=json;}
 public record View(PlanReview.Version selectedVersion,String audience,String code,boolean available,String etag,boolean protectedOffering,boolean canChange){}
 public record Input(@NotNull Boolean available,@NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*") String reason){}
 private boolean authorize(UUID actor,boolean write){
  var grant=db.sql("SELECT can_manage_offering FROM commercial_review_grant WHERE account_id=:a AND NOT revoked AND valid_until>clock_timestamp()").param("a",actor).query(Boolean.class).optional();
  if(grant.isEmpty()||(write&&!grant.get()))throw new ApiProblem(403,"COMMERCIAL_OFFERING_AUTHORITY_REQUIRED",write?"An active offering-availability grant is required.":"An active commercial-review grant is required.");return grant.get();
 }
 private View view(UUID id,boolean canChange){
  var plan=db.sql("SELECT * FROM plan_version WHERE version_id=:id AND publication_state='published'").param("id",id).query(PlanReview::project).optional().orElseThrow(()->new ApiProblem(404,"PLAN_NOT_FOUND","A published version is required to select the offering."));
  String audience=db.sql("SELECT audience FROM plan_version WHERE version_id=:id").param("id",id).query(String.class).single();
  var state=db.sql("SELECT available,revision FROM plan_offering_availability WHERE audience=:a AND code=:c").param("a",audience).param("c",plan.code()).query().listOfRows();
  boolean available=state.isEmpty()||(Boolean)state.getFirst().get("available");int revision=state.isEmpty()?0:(Integer)state.getFirst().get("revision");
  return new View(plan,audience,plan.code(),available,"\"offering-"+audience+"-"+plan.code()+"-"+revision+"\"",audience.equals("workspace")&&plan.code().equals("FREE"),canChange);
 }
 @Transactional public View read(UUID actor,UUID id){var result=view(id,authorize(actor,false));audit.record(actor,null,"PLAN_OFFERING_REVIEWED",id);return result;}
 @Transactional public View change(UUID actor,UUID id,UUID key,String tag,Input input){
  db.sql("SELECT pg_advisory_xact_lock(hashtext('commercial-grant'),hashtext(:a))").param("a",actor.toString()).query().singleRow();authorize(actor,true);
  if(tag==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review current offering availability before confirming.");
  if(input.reason().strip().length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Explain the availability change in at least 10 characters.","reason");
  String body=json.writeValueAsString(List.of(id,tag,input));
  var prior=db.sql("SELECT request_body,result::text FROM plan_offering_command WHERE actor_id=:a AND request_key=:k").param("a",actor).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){var saved=prior.getFirst();if(!body.equals(saved.get("request_body")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key was used for another availability confirmation.");return json.readValue((String)saved.get("result"),View.class);}
  var selected=view(id,true);db.sql("SELECT pg_advisory_xact_lock(hashtext('plan-draft'),hashtext(:scope))").param("scope",selected.audience()+":"+selected.code()).query().singleRow();
  var before=view(id,true);authorize(actor,true);
  if(!before.etag().equals(tag))throw new ApiProblem(412,"OFFERING_STALE","Offering availability changed. Close, refresh and review again.");
  if(before.protectedOffering()&&!input.available())throw new ApiProblem(409,"ESSENTIAL_PLAN_REQUIRED","Free workspace provisioning must remain available. This operation cannot withdraw Free.");
  if(before.available()==input.available())throw new ApiProblem(409,"OFFERING_UNCHANGED","The offering is already in that availability state.");
  db.sql("INSERT INTO plan_offering_availability(audience,code,anchor_version_id,available,revision) VALUES(:a,:c,:id,:value,1) ON CONFLICT(audience,code) DO UPDATE SET available=EXCLUDED.available,revision=plan_offering_availability.revision+1,updated_at=CURRENT_TIMESTAMP").param("a",before.audience()).param("c",before.code()).param("id",id).param("value",input.available()).update();
  var result=view(id,true);
  db.sql("INSERT INTO plan_offering_command(actor_id,request_key,selected_version_id,request_body,before_state,result,reason) VALUES(:a,:k,:id,:b,:before::jsonb,:result::jsonb,:r)").param("a",actor).param("k",key).param("id",id).param("b",body).param("before",json.writeValueAsString(before)).param("result",json.writeValueAsString(result)).param("r",input.reason().strip()).update();
  audit.record(actor,null,input.available()?"PLAN_OFFERING_RESTORED":"PLAN_OFFERING_WITHDRAWN",id);return result;
 }
}
