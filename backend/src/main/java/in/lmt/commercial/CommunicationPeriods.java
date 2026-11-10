package in.lmt.commercial;
import in.lmt.workspace.WorkspaceCommands;
import in.lmt.shared.ApiProblem;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
public class CommunicationPeriods {
 private final JdbcClient db;private final WorkspaceCommands scope;
 public CommunicationPeriods(JdbcClient db,WorkspaceCommands scope){this.db=db;this.scope=scope;}
 public record Period(UUID id,UUID planVersionId,String channel,String metric,int allowance,int used,int reserved,int remaining,int unknown,int failed,Instant startsAt,Instant endsAt,String state){}
 public record Page(List<Period> items,String nextCursor,long totalMatching,LocalDate from,LocalDate to,String timezone,Instant asOf,String definition){}
 public Page read(UUID actor,UUID w,LocalDate from,LocalDate to,String cursor){scope.requireOwner(w,actor);LocalDate today=LocalDate.now(ZoneId.of("Asia/Kolkata"));if(to==null)to=today;if(from==null)from=to.minusDays(29);if(from.getYear()<1||to.getYear()>9998||from.isAfter(to)||java.time.temporal.ChronoUnit.DAYS.between(from,to)>365)throw new ApiProblem(422,"INVALID_DATE_RANGE","Choose at most 366 inclusive calendar days.","from");
  Instant start=from.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant(),end=to.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant(),after=end;UUID afterId=new UUID(-1,-1);String binding=actor+":"+w+":"+from+":"+to;
  if(cursor!=null)try{String[] parts=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8).split("/");if(parts.length!=3||!parts[0].equals(binding))throw new IllegalArgumentException();after=Instant.parse(parts[1]);afterId=UUID.fromString(parts[2]);}catch(RuntimeException invalid){throw new ApiProblem(422,"INVALID_CURSOR","Reload periods after changing workspace or dates.","cursor");}
  String filter="c.workspace_id=:w AND c.starts_at<:end AND c.ends_at>:start";Instant now=Instant.now();
  var rows=db.sql("SELECT c.*,coalesce((SELECT sum(e.quantity) FROM communication_usage_event e WHERE e.contract_id=c.id AND e.metric=c.metric),0) AS used,(SELECT count(*) FROM communication_operation o WHERE o.contract_id=c.id AND o.status IN('queued','dispatching','unknown')) AS reserved,(SELECT count(*) FROM communication_operation o WHERE o.contract_id=c.id AND o.status='unknown') AS unknown,(SELECT count(*) FROM communication_operation o WHERE o.contract_id=c.id AND o.status IN('failed','cancelled')) AS failed FROM communication_contract c WHERE "+filter+" AND (c.starts_at,c.id)<(:after,:id) ORDER BY c.starts_at DESC,c.id DESC LIMIT 26").param("w",w).param("start",java.sql.Timestamp.from(start)).param("end",java.sql.Timestamp.from(end)).param("after",java.sql.Timestamp.from(after)).param("id",afterId).query((r,n)->{Instant begins=r.getTimestamp("starts_at").toInstant(),ends=r.getTimestamp("ends_at").toInstant();int allowance=r.getInt("allowance"),used=r.getInt("used"),reserved=r.getInt("reserved");return new Period(r.getObject("id",UUID.class),r.getObject("plan_version_id",UUID.class),r.getString("channel"),r.getString("metric"),allowance,used,reserved,Math.max(0,allowance-used-reserved),r.getInt("unknown"),r.getInt("failed"),begins,ends,now.isBefore(begins)?"future":now.isBefore(ends)?"current":"ended");}).list();
  long total=db.sql("SELECT count(*) FROM communication_contract c WHERE "+filter).param("w",w).param("start",java.sql.Timestamp.from(start)).param("end",java.sql.Timestamp.from(end)).query(Long.class).single();boolean more=rows.size()>25;var items=more?rows.subList(0,25):rows;String next=null;if(more){var last=items.getLast();next=Base64.getUrlEncoder().withoutPadding().encodeToString((binding+"/"+last.startsAt()+"/"+last.id()).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  return new Page(items,next,total,from,to,"Asia/Kolkata",now,"Configured periods overlapping the selected dates. Counts cover each complete contract period, not a prorated date slice. Accepted usage is sourced from the selected metric's immutable events; reservations include unknown outcomes. Late acceptance remains attributed to its original contract. Expired periods retain usage and held reservations. Overages/prices are not configured and no forecast is inferred.");
 }
}
