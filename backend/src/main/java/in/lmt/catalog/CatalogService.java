package in.lmt.catalog;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final JdbcClient jdbc;
    public CatalogService(JdbcClient jdbc) { this.jdbc = jdbc; }
    public record Plan(UUID versionId, String code, String name, int version, Instant effectiveAt,
                       boolean baselineOnly, Integer rentableLimit, Integer managerLimit,
                       String amount, String currency, String disclosure) {}
    public record Page(int limit, String nextCursor) {}
    public record Catalog(List<Plan> items, Page page, int totalMatching, Instant asOf) {}

    @Transactional(readOnly = true)
    public Catalog list(String audience, String interval) {
        // One database timestamp and statement snapshot for filtering and disclosure freshness.
        Instant asOf = jdbc.sql("SELECT CURRENT_TIMESTAMP").query(java.time.OffsetDateTime.class)
                .single().toInstant();
        List<Plan> plans = jdbc.sql("""
                SELECT * FROM (
                    SELECT DISTINCT ON (code) * FROM plan_version
                    WHERE audience = :audience AND publication_state = 'published' AND effective_at <= :asOf
                    ORDER BY code, effective_at DESC
                ) active ORDER BY rentable_limit NULLS LAST, code
                """).param("audience", audience).param("asOf", java.time.OffsetDateTime.ofInstant(asOf, java.time.ZoneOffset.UTC))
                .query((rs, row) -> {
                    var amount = rs.getBigDecimal(interval.equals("annual") ? "annual_amount" : "monthly_amount");
                    return new Plan(rs.getObject("version_id", UUID.class), rs.getString("code"),
                        rs.getString("display_name"), rs.getInt("version_number"),
                        rs.getTimestamp("effective_at").toInstant(), rs.getBoolean("baseline_only"),
                        rs.getObject("rentable_limit", Integer.class), rs.getObject("manager_limit", Integer.class),
                        amount == null ? null : amount.toPlainString(), rs.getString("currency"),
                        rs.getString("commercial_disclosure"));
                }).list();
        return new Catalog(plans, new Page(100, null), plans.size(), asOf);
    }
}
