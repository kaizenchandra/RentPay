package in.lmt.catalog.admin;

import in.lmt.shared.*;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanReview {
 private final JdbcClient db;
 private final Audit audit;
 public PlanReview(JdbcClient db, Audit audit){this.db=db;this.audit=audit;}
 public record Version(UUID id,String etag,String code,String name,int version,String state,Instant effectiveAt,
                       boolean baselineOnly,Integer rentableLimit,Integer managerLimit,String monthlyAmount,
                       String annualAmount,String currency,String disclosure){}
 public record Page(int limit,String nextCursor){}
 public record Review(String audience,List<Version> items,Page page,int totalMatching,Instant asOf,Instant accessExpiresAt,boolean canCreateDraft,boolean canPublish,boolean canEditDraft,boolean canDiscardDraft){}
 public static Version project(java.sql.ResultSet r,int n)throws java.sql.SQLException {
  return new Version(r.getObject("version_id",UUID.class),PlanDrafts.tag(r.getObject("version_id",UUID.class),r.getInt("row_revision")),r.getString("code"),r.getString("display_name"),r.getInt("version_number"),r.getString("publication_state"),r.getTimestamp("effective_at").toInstant(),r.getBoolean("baseline_only"),r.getObject("rentable_limit",Integer.class),r.getObject("manager_limit",Integer.class),r.getBigDecimal("monthly_amount")==null?null:r.getBigDecimal("monthly_amount").toPlainString(),r.getBigDecimal("annual_amount")==null?null:r.getBigDecimal("annual_amount").toPlainString(),r.getString("currency"),r.getString("commercial_disclosure"));
 }
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
 public Review list(UUID actor,String audience,UUID after){
  var expiry=db.sql("SELECT valid_until FROM commercial_review_grant WHERE account_id=:actor AND NOT revoked AND valid_until>clock_timestamp()")
   .param("actor",actor).query(java.time.OffsetDateTime.class).optional()
   .orElseThrow(()->new ApiProblem(403,"COMMERCIAL_REVIEW_REQUIRED","An active commercial-review grant is required. Workspace ownership does not grant platform access."));
  if(!Set.of("workspace","consumer").contains(audience))throw new ApiProblem(422,"VALIDATION_FAILED","Choose workspace or consumer.","audience");
  var now=db.sql("SELECT CURRENT_TIMESTAMP").query(java.time.OffsetDateTime.class).single();
  var rows=db.sql("SELECT * FROM plan_version WHERE audience=:audience AND (:after::uuid IS NULL OR version_id>:after::uuid) ORDER BY version_id LIMIT 26")
   .param("audience",audience).param("after",after).query(PlanReview::project).list();
  int count=db.sql("SELECT count(*) FROM plan_version WHERE audience=:a").param("a",audience).query(Integer.class).single();
  boolean more=rows.size()>25;var items=List.copyOf(rows.subList(0,Math.min(25,rows.size())));
  audit.record(actor,null,"COMMERCIAL_"+audience.toUpperCase(Locale.ROOT)+"_VERSIONS_REVIEWED",actor);
  return new Review(audience,items,new Page(25,more?items.getLast().id().toString():null),count,now.toInstant(),expiry.toInstant(),db.sql("SELECT can_create_draft FROM commercial_review_grant WHERE account_id=:a").param("a",actor).query(Boolean.class).single(),db.sql("SELECT can_publish FROM commercial_review_grant WHERE account_id=:a").param("a",actor).query(Boolean.class).single(),db.sql("SELECT can_edit_draft FROM commercial_review_grant WHERE account_id=:a").param("a",actor).query(Boolean.class).single(),db.sql("SELECT can_discard_draft FROM commercial_review_grant WHERE account_id=:a").param("a",actor).query(Boolean.class).single());
 }
}
