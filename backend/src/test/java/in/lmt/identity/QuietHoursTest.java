package in.lmt.identity;
import static org.assertj.core.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class QuietHoursTest {
 @Test void overnightUsesIndiaDateAndHalfOpenBoundaries(){
  assertThat(QuietHours.resumeAt(Instant.parse("2026-11-09T16:29:59.999Z"),true,"22:00","07:00")).isNull();
  assertThat(QuietHours.resumeAt(Instant.parse("2026-11-09T16:30:00Z"),true,"22:00","07:00")).isEqualTo(Instant.parse("2026-11-10T01:30:00Z"));
  assertThat(QuietHours.resumeAt(Instant.parse("2026-11-09T20:30:00Z"),true,"22:00","07:00")).isEqualTo(Instant.parse("2026-11-10T01:30:00Z"));
  assertThat(QuietHours.resumeAt(Instant.parse("2026-11-10T01:30:00Z"),true,"22:00","07:00")).isNull();
 }
 @Test void sameDayAndDisabledIntervals(){
  assertThat(QuietHours.resumeAt(Instant.parse("2026-11-09T03:30:00Z"),true,"09:00","17:00")).isEqualTo(Instant.parse("2026-11-09T11:30:00Z"));
  assertThat(QuietHours.resumeAt(Instant.parse("2026-11-09T11:30:00Z"),true,"09:00","17:00")).isNull();
  assertThat(QuietHours.resumeAt(Instant.parse("2026-11-09T04:30:00Z"),false,"09:00","17:00")).isNull();
 }
}
