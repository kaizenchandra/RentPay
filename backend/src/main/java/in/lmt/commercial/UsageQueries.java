package in.lmt.commercial;

import in.lmt.shared.ApiProblem;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Private commercial projections; caller establishes owner scope and a snapshot transaction. */
@Service @Transactional(propagation=Propagation.MANDATORY)
public class UsageQueries {
 private final RentableCapacity capacity;private final JdbcClient db;private final Entitlements entitlements;private final ManagerUsageEvidence evidence;
 public UsageQueries(JdbcClient db,Entitlements entitlements,ManagerUsageEvidence evidence,RentableCapacity capacity){this.capacity=capacity;this.db=db;this.entitlements=entitlements;this.evidence=evidence;}
 public record Quota(String resource,int allowance,Integer used,Integer reserved,Integer remaining,int excess,boolean available,Boolean reconciled,String definition,Reconciliation evidence){}
 public record Reconciliation(long activeManagers,long committedAllocations,long eventBalance,long inconsistentAllocations,long unmatchedEvents,int unexplainedReservations){}
 public record Snapshot(UUID workspaceId,String workspaceName,UUID planVersionId,String planName,String workspaceStatus,String subscriptionStatus,List<Entitlements.Decision> capabilities,List<Quota> quotas,Instant asOf,String coverage){}
 public record Event(UUID id,UUID allocationId,String kind,int quantity,Instant occurredAt){}
 public record Page(int limit,String nextCursor){}
 public record History(List<Event> items,Page page,int totalMatching,int netChange,LocalDate from,LocalDate to,String timezone,Instant asOf){}
 public Snapshot snapshot(UUID w){
  var row=db.sql("SELECT w.name,w.status,s.status AS subscription_status,s.plan_version_id,p.display_name FROM workspace w JOIN workspace_subscription s ON s.workspace_id=w.id JOIN plan_version p ON p.version_id=s.plan_version_id WHERE w.id=:w").param("w",w).query().singleRow();
  var records=evidence.read(w);
  long managers=db.sql("SELECT count(*) FROM workspace_member WHERE workspace_id=:w AND active AND role='MANAGER'").param("w",w).query(Long.class).single();
  var rentableStatus=capacity.status(w);
  var quotas=db.sql("SELECT resource,allowance,used,reserved FROM workspace_grant WHERE workspace_id=:w ORDER BY resource").param("w",w).query((r,n)->{
   String resource=r.getString("resource");int allowance=r.getInt("allowance"),used=r.getInt("used"),reserved=r.getInt("reserved");boolean manager=resource.equals("managers"),rentable=resource.equals("rentable_items"),available=manager||rentable;boolean reconciled=manager?records.matches(used,reserved)&&used==managers:rentable&&!rentableStatus.status().equals("RECONCILIATION_REQUIRED");
   return new Quota(resource,allowance,available?used:null,available?reserved:null,reconciled?Math.max(0,allowance-used-reserved):null,Math.max(0,used+reserved-allowance),available,available?reconciled:null,
    manager?"Current active MANAGER accounts per workspace. Owners and pending invitations excluded. No monthly reset; acceptance reserves and consumes atomically.":rentableStatus.definition(),
    manager?new Reconciliation(managers,records.committedAllocations(),records.eventBalance(),records.inconsistentAllocations(),records.unmatchedEvents(),reserved):null);
  }).list();
  return new Snapshot(w,(String)row.get("name"),(UUID)row.get("plan_version_id"),(String)row.get("display_name"),(String)row.get("status"),(String)row.get("subscription_status"),entitlements.evaluate(w),quotas,Instant.now(),"Sandbox membership and property setup capacity. Central evaluation combines plan bindings, active local add-on/time-limited inputs and commercial overrides before restriction and operational-availability gates. Local add-on inputs are not live purchases; renewal billing remains unavailable. Eligibility never establishes resource permission.");
 }
 public History history(UUID actor,UUID w,LocalDate from,LocalDate to,String cursor){
  LocalDate today=LocalDate.now(ZoneId.of("Asia/Kolkata"));if(to==null)to=today;if(from==null)from=to.minusDays(29);
  if(from.getYear()<1||to.getYear()>9998||from.isAfter(to)||java.time.temporal.ChronoUnit.DAYS.between(from,to)>365)throw new ApiProblem(422,"INVALID_DATE_RANGE","Choose an inclusive date range of no more than 366 days.","from");
  Instant start=from.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant(),end=to.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant();
  String binding=actor+":"+w+":"+from+":"+to;Instant after=start;UUID id=new UUID(0,0);
  if(cursor!=null)try{String[] parts=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8).split("/");if(parts.length!=3||!parts[0].equals(binding))throw new IllegalArgumentException();after=Instant.parse(parts[1]);id=UUID.fromString(parts[2]);}catch(RuntimeException e){throw new ApiProblem(422,"INVALID_CURSOR","Reload usage history after changing context or dates.","cursor");}
  var rows=db.sql("SELECT * FROM manager_usage_event WHERE workspace_id=:w AND occurred_at>=:start AND occurred_at<:end AND (occurred_at,id)>(:after,:id) ORDER BY occurred_at,id LIMIT 26")
   .param("w",w).param("start",java.sql.Timestamp.from(start)).param("end",java.sql.Timestamp.from(end)).param("after",java.sql.Timestamp.from(after)).param("id",id)
   .query((r,n)->new Event(r.getObject("id",UUID.class),r.getObject("allocation_id",UUID.class),r.getString("kind"),r.getInt("quantity"),r.getTimestamp("occurred_at").toInstant())).list();
  var aggregate=db.sql("SELECT count(*) AS count,coalesce(sum(quantity),0) AS net FROM manager_usage_event WHERE workspace_id=:w AND occurred_at>=:start AND occurred_at<:end")
   .param("w",w).param("start",java.sql.Timestamp.from(start)).param("end",java.sql.Timestamp.from(end)).query().singleRow();
  boolean more=rows.size()>25;var items=more?rows.subList(0,25):rows;String next=null;
  if(more){var last=items.getLast();next=Base64.getUrlEncoder().withoutPadding().encodeToString((binding+"/"+last.occurredAt()+"/"+last.id()).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  return new History(items,new Page(25,next),((Number)aggregate.get("count")).intValue(),((Number)aggregate.get("net")).intValue(),from,to,"Asia/Kolkata",Instant.now());
 }
}
