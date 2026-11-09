package in.lmt.catalog.admin;
import in.lmt.shared.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class PlanDrafts {
 private final JdbcClient db;private final Audit audit;private final ObjectMapper json;
 public PlanDrafts(JdbcClient db,Audit audit,ObjectMapper json){this.db=db;this.audit=audit;this.json=json;}
 public record Input(@NotBlank @Size(max=100) @Pattern(regexp="[^\\p{Cntrl}]*") String name,
  @NotNull OffsetDateTime effectiveAt,@NotBlank @Size(max=2000) @Pattern(regexp="[^\\p{Cntrl}]*") String disclosure,
  @NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*") String reason){}
 public record Result(UUID id,UUID sourceId,String state){}
 public static String tag(UUID id,int revision){return "\"plan-"+id+"-"+revision+"\"";}
 @Transactional public Result create(UUID actor,UUID source,UUID key,String etag,Input input){
  db.sql("SELECT pg_advisory_xact_lock(hashtext('commercial-grant'),hashtext(:actor))").param("actor",actor.toString()).query().singleRow();
  if(!db.sql("SELECT EXISTS(SELECT 1 FROM commercial_review_grant WHERE account_id=:a AND NOT revoked AND can_create_draft AND valid_until>clock_timestamp())").param("a",actor).query(Boolean.class).single())
   throw new ApiProblem(403,"COMMERCIAL_DRAFT_AUTHORITY_REQUIRED","An active draft-creation grant is required.");
  if(input.reason().strip().length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Explain the draft reason in at least 10 characters.","reason");
  if(etag==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review the source version before creating a draft.");
  var body=json.writeValueAsString(List.of(source.toString(),etag,input));
  var previous=db.sql("SELECT * FROM plan_draft_command WHERE actor_id=:a AND request_key=:k").param("a",actor).param("k",key).query().listOfRows();
  if(!previous.isEmpty()){
   var saved=previous.getFirst();if(!body.equals(saved.get("request_body")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key was used for different draft terms.");
   return new Result((UUID)saved.get("draft_id"),(UUID)saved.get("source_id"),"draft");
  }
  var original=db.sql("SELECT * FROM plan_version WHERE version_id=:id AND publication_state='published'").param("id",source).query().listOfRows();
  if(original.isEmpty())throw new ApiProblem(404,"PLAN_SOURCE_NOT_FOUND","A published source version is required.");
  if(!tag(source,(Integer)original.getFirst().get("row_revision")).equals(etag))throw new ApiProblem(412,"PLAN_SOURCE_STALE","Review the source version again.");
  var plan=original.getFirst();String scope=plan.get("audience")+":"+plan.get("code");
  db.sql("SELECT pg_advisory_xact_lock(hashtext('plan-draft'),hashtext(:scope))").param("scope",scope).query().singleRow();
  if(db.sql("SELECT EXISTS(SELECT 1 FROM plan_version WHERE audience=:a AND code=:c AND effective_at=:t)").param("a",plan.get("audience")).param("c",plan.get("code")).param("t",input.effectiveAt()).query(Boolean.class).single())throw new ApiProblem(409,"PLAN_EFFECTIVE_DATE_CONFLICT","Another version already uses this effective instant. Choose a different instant.");
  if(!db.sql("SELECT :t > clock_timestamp()").param("t",input.effectiveAt()).query(Boolean.class).single())throw new ApiProblem(422,"VALIDATION_FAILED","Choose a future effective instant.","effectiveAt");
  UUID id=UUID.randomUUID();
  db.sql("""
   INSERT INTO plan_version(version_id,audience,code,version_number,display_name,publication_state,effective_at,baseline_only,rentable_limit,manager_limit,monthly_amount,annual_amount,currency,commercial_disclosure)
   SELECT :id,p.audience,p.code,(SELECT max(version_number)+1 FROM plan_version WHERE audience=p.audience AND code=p.code),:name,'draft',:effective,p.baseline_only,p.rentable_limit,p.manager_limit,p.monthly_amount,p.annual_amount,p.currency,:disclosure
   FROM plan_version p WHERE p.version_id=:source
   """).param("id",id).param("name",input.name().strip()).param("effective",input.effectiveAt()).param("disclosure",input.disclosure().strip()).param("source",source).update();
  db.sql("INSERT INTO plan_draft_command(actor_id,request_key,source_id,request_body,draft_id,reason) VALUES(:a,:k,:s,:b,:d,:r)").param("a",actor).param("k",key).param("s",source).param("b",body).param("d",id).param("r",input.reason().strip()).update();
  audit.record(actor,null,"PLAN_DRAFT_CREATED",id);return new Result(id,source,"draft");
 }
}
