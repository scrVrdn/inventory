package io.github.scrvrdn.inventory.services.utility;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
public class BackupServiceIntegrationTests {

    @Value("${app.db.meta.app-id}")
    private String appId;

    @Value("${app.db.meta.schema-version}")
    private Integer schemaVersion;
    
    @TempDir
    private Path tempDir;

    private final JdbcTemplate jdbcTemplate;
    private final BackupService underTest;

    @Autowired
    public BackupServiceIntegrationTests(final JdbcTemplate jdbcTemplate, final BackupService underTest) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers");
    }

    @Test
    public void testThatCreatesBackup() throws Exception {
        String insertSql = """
                INSERT INTO "books" ("id", "title")
                VALUES (1, 'SUCCESS');
                """;
        
        jdbcTemplate.update(insertSql);

        String backupFileName = underTest.createBackup(tempDir).substring("Backup successful: ".length());
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve(backupFileName));
            Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT title FROM books WHERE id = 1;")) {
                String result = rs.next() ? rs.getString("title") : "FAILURE";
                assertThat(result).isEqualTo("SUCCESS");
            }
        }

    }

    @Test
    public void testThatSyncsFromBackup() throws Exception {
        Path backupPath = setupBackupDb();

        String result1 = underTest.revertToBackup(backupPath);
        String result2 = jdbcTemplate.queryForObject("SELECT title FROM books WHERE id = 1;", String.class);

        assertThat(result1).isEqualTo("Successfully reverted to backup.");
        assertThat(result2).isEqualTo("SUCCESS");

    }

    private Path setupBackupDb() throws Exception {
        Path backupPath = tempDir.resolve("test.db.bak");
        String url = "jdbc:sqlite:" + backupPath.toString();

        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS "app_meta"(
                            "id" INTEGER CHECK("id" = 1),
                            "app_id" TEXT NOT NULL,
                            "schema_version" INTEGER NOT NULL,
                            "created_at" TEXT NOT NULL,
                            PRIMARY KEY("id")
                        );
                """);

                stmt.executeUpdate("""
                        CREATE TABLE "books" (
                            "id" INTEGER,
                            "title" TEXT,
                            "year" INTEGER,
                            "isbn10" TEXT UNIQUE,
                            "isbn13" TEXT UNIQUE,
                            "shelf_mark" TEXT,
                            PRIMARY KEY("id")
                        );
                """);

                stmt.executeUpdate(String.format("""
                        INSERT OR IGNORE INTO "app_meta" ("id", "app_id", "schema_version", "created_at")
                        VALUES (1, '%s', %d, CURRENT_TIMESTAMP);

                       
                    """, appId, schemaVersion));

                stmt.executeUpdate("""
                        INSERT INTO "books" ("id", "title")
                        VALUES (1, 'SUCCESS');
                """);
                                
            }
        }

        return backupPath;
    }   

}
