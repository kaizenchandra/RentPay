package in.lmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.flywaydb.core.Flyway;
import java.sql.DriverManager;
import java.sql.SQLException;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class LmtApplication {
    public static void main(String[] args) {
        String process = System.getenv("LMT_PROCESS");
        if (process != null) {
            var flyway = Flyway.configure()
                .dataSource(System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"))
                .locations("classpath:db/migration").ignoreMigrationPatterns(new String[0]).load();
            if ("migrate".equals(process)) {
                flyway.migrate();
                grantRuntimeHistoryRead();
                return;
            }
            // Runtime credentials cannot migrate. Refuse incompatible or incomplete schemas.
            flyway.validate();
            if (flyway.info().pending().length != 0) {
                throw new IllegalStateException("Runtime image and database migrations must match");
            }
        }
        SpringApplication.run(LmtApplication.class, args);
    }

    private static void grantRuntimeHistoryRead() {
        try (var connection = DriverManager.getConnection(System.getenv("DB_URL"),
                System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
             var statement = connection.createStatement()) {
            statement.execute("GRANT SELECT ON flyway_schema_history TO lmt_runtime");
        } catch (SQLException exception) {
            throw new IllegalStateException("Migration history grant failed", exception);
        }
    }
}
