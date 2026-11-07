package in.lmt.catalog;

import static org.assertj.core.api.Assertions.*;
import java.net.URI;
import java.net.http.*;
import java.sql.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogIntegrationTest {
    static final PostgreSQLContainer db = new PostgreSQLContainer("postgres:17.11");
    static {
        db.start();
        try (var c = DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword()); var s = c.createStatement()) {
            s.execute("CREATE ROLE lmt_runtime LOGIN PASSWORD 'runtime_test'");
        } catch (SQLException e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", db::getJdbcUrl);
        r.add("spring.datasource.username", () -> "lmt_runtime");
        r.add("spring.datasource.password", () -> "runtime_test");
        r.add("spring.flyway.user", db::getUsername);
        r.add("spring.flyway.password", db::getPassword);
    }
    @LocalServerPort int port;
    final HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    JsonNode json(HttpResponse<String> response) { return new ObjectMapper().readTree(response.body()); }
    void sql(String sql) throws SQLException {
        try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement()){s.execute(sql);}
    }
    @Test @Order(1) void baselineIsPublicAndMoneyIsDecimalString() throws Exception {
        var res=get("/api/v1/public/plans");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.headers().firstValue("cache-control")).contains("no-store");
        var body=json(res);
        assertThat(body.get("items").size()).isEqualTo(5);
        assertThat(body.get("totalMatching").asInt()).isEqualTo(5);
        assertThat(body.get("items").get(0).get("amount").asString()).isEqualTo("0.00");
        assertThat(body.get("items").get(1).get("amount").isNull()).isTrue();
        assertThat(body.get("items").get(4).get("rentableLimit").isNull()).isTrue();
        assertThat(res.body()).doesNotContain("publication_state", "actor");
    }
    @Test @Order(1) void filtersAndPrivacySafeNotFound() throws Exception {
        assertThat(json(get("/api/v1/public/plans?audience=consumer")).get("items").size()).isZero();
        assertThat(get("/api/v1/public/plans?interval=weekly").statusCode()).isEqualTo(422);
        assertThat(json(get("/api/v1/public/plans?audience=other")).get("fieldErrors").get(0).get("field").asString()).isEqualTo("audience");
        assertThat(get("/api/v1/public/plans/NOT_PUBLIC").statusCode()).isEqualTo(404);
        assertThat(get("/api/v1/admin/plans").statusCode()).isEqualTo(401);
    }
    @Test @Order(9) void excludesDraftAndFutureAndSelectsEffectiveVersion() throws Exception {
        {
            sql("INSERT INTO plan_version SELECT '10000000-0000-4000-8000-000000000001', audience, 'TEST_PLAN', 1, 'Test published', 'published', '2020-01-01', true, 7, 2, 10.25, 100.50, currency, commercial_disclosure FROM plan_version WHERE code='FREE' AND version_number=1");
            sql("INSERT INTO plan_version SELECT '10000000-0000-4000-8000-000000000002', audience, 'TEST_PLAN', 2, 'Secret draft', 'draft', '2021-01-01', true, 7, 2, 99, 99, currency, commercial_disclosure FROM plan_version WHERE code='FREE' AND version_number=1");
            sql("INSERT INTO plan_version SELECT '10000000-0000-4000-8000-000000000003', audience, 'TEST_PLAN', 3, 'Secret future', 'published', '2999-01-01', true, 7, 2, 99, 99, currency, commercial_disclosure FROM plan_version WHERE code='FREE' AND version_number=1");
            var annual=get("/api/v1/public/plans/TEST_PLAN?interval=annual");
            assertThat(annual.statusCode()).isEqualTo(200);
            assertThat(json(annual).get("amount").asString()).isEqualTo("100.50");
            assertThat(get("/api/v1/public/plans").body()).doesNotContain("Secret draft","Secret future");
        }
    }

    @Test @Order(1) void publishedTermsAreImmutableAndRuntimeCannotWrite() throws Exception {
        assertThatThrownBy(() -> sql("UPDATE plan_version SET monthly_amount=5 WHERE code='FREE' AND version_number=1"))
            .isInstanceOf(SQLException.class).hasMessageContaining("immutable");
        assertThatThrownBy(() -> sql("DELETE FROM plan_version WHERE code='SILVER'"))
            .isInstanceOf(SQLException.class).hasMessageContaining("immutable");
        try(var c=DriverManager.getConnection(db.getJdbcUrl(),"lmt_runtime","runtime_test");var s=c.createStatement()) {
            assertThatThrownBy(() -> s.execute("UPDATE plan_version SET display_name='changed'"))
                .isInstanceOf(SQLException.class).hasMessageContaining("permission denied");
        }
        try(var c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());var s=c.createStatement();var rs=s.executeQuery("SELECT count(*) FROM catalog_audit")) {
            rs.next(); assertThat(rs.getInt(1)).isEqualTo(6);
        }
    }
}
