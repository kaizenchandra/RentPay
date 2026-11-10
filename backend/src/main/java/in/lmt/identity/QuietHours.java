package in.lmt.identity;
import java.time.*;
/** India presentation policy: start inclusive, end exclusive, no all-day ambiguity. */
public final class QuietHours {
 private QuietHours(){}
 public static Instant resumeAt(Instant now,boolean enabled,String start,String end){
  if(!enabled)return null;
  var zone=ZoneId.of("Asia/Kolkata");var local=now.atZone(zone);var time=local.toLocalTime();var from=LocalTime.parse(start);var until=LocalTime.parse(end);
  boolean overnight=from.isAfter(until);
  boolean quiet=overnight?(!time.isBefore(from)||time.isBefore(until)):(!time.isBefore(from)&&time.isBefore(until));
  if(!quiet)return null;
  var day=local.toLocalDate();if(overnight&&!time.isBefore(from))day=day.plusDays(1);
  return day.atTime(until).atZone(zone).toInstant();
 }
}
