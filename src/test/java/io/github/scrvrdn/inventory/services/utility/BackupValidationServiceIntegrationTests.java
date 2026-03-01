package io.github.scrvrdn.inventory.services.utility;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

@SpringBootTest
@ActiveProfiles("test")
public class BackupValidationServiceIntegrationTests {
    @Value("${app.db.meta.app-id}")
    private String appId;

    @Value("${app.db.meta.schema-version}")
    private Integer schemaVersion;

    @TempDir
    private Path tempDir;

    private final JdbcTemplate jdbcTemplate;

    private final BackupValidationService underTest;

    @Autowired
    public BackupValidationServiceIntegrationTests(final JdbcTemplate jdbcTemplate, final BackupValidationService underTest) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers");
    }

    @Test
    public void testThatValidatesBackup() throws Exception {
        Path backupDir = setupBackupDb();
        boolean result = underTest.validateBackup(backupDir);
        assertThat(result).isTrue();
    }

    private Path setupBackupDb() throws Exception {
        Path backupDir = tempDir.resolve("test.db.bak");
        String url = "jdbc:sqlite:" + backupDir.toString();

        String createSql = """
                        CREATE TABLE IF NOT EXISTS "app_meta"(
                            "id" INTEGER CHECK("id" = 1),
                            "app_id" TEXT NOT NULL,
                            "schema_version" INTEGER NOT NULL,
                            "created_at" TEXT NOT NULL,
                            PRIMARY KEY("id")
                        );
                        """;

        String insertSql = String.format("""
                        INSERT OR IGNORE INTO "app_meta" ("id", "app_id", "schema_version", "created_at")
                        VALUES (1, '%s', %d, CURRENT_TIMESTAMP);
                    """, appId, schemaVersion);

        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createSql);
                stmt.executeUpdate(insertSql);
            }
        }

        return backupDir;
    }

    @Test
    public void testThatRecognizesInvalidBackupFiles() throws Exception {
        File notASqliteFile = File.createTempFile("notASqliteFile", "bak");
        Path noSqliteBackupDir = tempDir.resolve(notASqliteFile.toString());

        Path notAValidBackupFile = tempDir.resolve("notAValidBackupFile.db.bak");
        String validSqliteButInvalidBackupFile = "jdbc:sqlite:" + notAValidBackupFile.toString();
        try (Connection conn = DriverManager.getConnection(validSqliteButInvalidBackupFile)) {}

        boolean noSqliteResult = underTest.validateBackup(noSqliteBackupDir);
        boolean noValidBackupFileResult = underTest.validateBackup(notAValidBackupFile);

        assertThat(noSqliteResult).isFalse();
        assertThat(noValidBackupFileResult).isFalse();
    }

}
