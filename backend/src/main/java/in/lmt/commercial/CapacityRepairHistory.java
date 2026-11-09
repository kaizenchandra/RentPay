package in.lmt.commercial;
import in.lmt.shared.ApiProblem;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Owner-scoped projection only; command hashes, operator identity and raw JSON stay private. */
@Service @Transactional(propagation=Propagation.MANDATORY)
public class CapacityRepairHistory {
 private final JdbcClient db;
 public CapacityRepairHistory(JdbcClient db){this.db=db;}
 public record Entry(int revision,String reason,int previousUsed,int previousReserved,int correctedUsed,int correctedReserved,int allowance,Instant occurredAt){}
 public record Page(int limit,String nextCursor){}
 public record History(List<Entry> items,Page page,int totalMatching,LocalDate from,LocalDate to,String timezone,Instant asOf){}
 public History read(UUID actor,UUID workspace,LocalDate from,LocalDate to,String cursor){
  if(to==null)to=LocalDate.now(ZoneId.of("Asia/Kolkata"));if(from==null)from=to.minusDays(29);
  if(from.getYear()<1||to.getYear()>9998||from.isAfter(to)||java.time.temporal.ChronoUnit.DAYS.between(from,to)>365)throw new ApiProblem(422,"INVALID_DATE_RANGE","Choose an inclusive date range of no more than 366 days.","from");
  String binding="repair-history:"+actor+":"+workspace+":"+from+":"+to;long before=Long.MAX_VALUE;
  if(cursor!=null)try{String[] p=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8).split("/");if(p.length!=2||!p[0].equals(binding))throw new IllegalArgumentException();before=Long.parseLong(p[1]);if(before<1||before>Integer.MAX_VALUE)throw new IllegalArgumentException();}catch(RuntimeException e){throw new ApiProblem(422,"INVALID_CURSOR","Reload repair history after changing workspace or dates.","cursor");}
  var start=java.sql.Timestamp.from(from.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());var end=java.sql.Timestamp.from(to.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
  var rows=db.sql("""
   SELECT (result->>'revision')::integer AS revision,reason,
    (before_state->>'used')::integer AS previous_used,(before_state->>'reserved')::integer AS previous_reserved,
    (result->>'used')::integer AS corrected_used,(result->>'reserved')::integer AS corrected_reserved,
    (result->>'allowance')::integer AS allowance,created_at
   FROM capacity_repair_command WHERE workspace_id=:w AND created_at>=:start AND created_at<:end
    AND (result->>'revision')::integer<:before ORDER BY (result->>'revision')::integer DESC LIMIT 26
   """).param("w",workspace).param("start",start).param("end",end).param("before",before)
   .query((r,n)->new Entry(r.getInt("revision"),r.getString("reason"),r.getInt("previous_used"),r.getInt("previous_reserved"),r.getInt("corrected_used"),r.getInt("corrected_reserved"),r.getInt("allowance"),r.getTimestamp("created_at").toInstant())).list();
  int total=db.sql("SELECT count(*) FROM capacity_repair_command WHERE workspace_id=:w AND created_at>=:start AND created_at<:end").param("w",workspace).param("start",start).param("end",end).query(Integer.class).single();
  boolean more=rows.size()>25;var items=more?rows.subList(0,25):rows;String next=more?Base64.getUrlEncoder().withoutPadding().encodeToString((binding+"/"+items.getLast().revision()).getBytes(java.nio.charset.StandardCharsets.UTF_8)):null;
  return new History(items,new Page(25,next),total,from,to,"Asia/Kolkata",Instant.now());
 }
}
