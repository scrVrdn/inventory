package io.github.scrvrdn.inventory.services.utility.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.services.utility.BackupService;


@Service
public class BackupServiceImpl implements BackupService {

    private final JdbcTemplate jdbcTemplate;
    
    @Value("${app.db.meta.app-id}")
    private String expectedAppId;

    @Value("${app.db.meta.schema-version}")
    private Integer expectedSchemaVersion;

    public BackupServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public String createBackup() {
            try {
                Path backupDir = getBackupDir().resolve("backup-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".db.bak");
                
                runIntegrityCheck();
                jdbcTemplate.execute("VACUUM INTO '" + backupDir.toAbsolutePath() + "';");
                checkBackupIntegrity(backupDir);

                return "Backup successful: " + backupDir.getFileName();

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        
    }

    private void runIntegrityCheck() {
        String result = jdbcTemplate.queryForObject("PRAGMA integrity_check", String.class);
        if (!result.equals("ok")) {
            throw new IllegalStateException("DB corrupt: " + result);
        }
    }

    public Path getBackupDir() throws IOException {
        Path backupDir = Paths.get(System.getProperty("user.home"), ".inventory", "backup");
        Files.createDirectories(backupDir);
        return backupDir;
    }

    private void checkBackupIntegrity(Path backupDir) throws SQLException {
        String result;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + backupDir.toAbsolutePath());
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA integrity_check");
            result = rs.next() ? rs.getString(1) : "unknown";
        }

        if (result == null || !result.equals("ok")) throw new IllegalStateException("Backup corrupt: " + result);
    }
    
    @Override
    public String revertToBackup(Path backupDir) {
        
        try {
            checkIfValidSQLiteFile(backupDir);
            checkBackupIntegrity(backupDir);

            String attachSql = String.format("ATTACH DATABASE '%s' AS backupdb;", backupDir.toAbsolutePath().toString());
            jdbcTemplate.execute(attachSql);
            validateMetaData(backupDir);

            jdbcTemplate.execute((Connection conn) -> {
                
                ResultSet tables = conn.createStatement().executeQuery("SELECT name FROM backupdb.sqlite_master WHERE type = 'table';");

                while (tables.next()) {
                    String tableName = tables.getString("name");
                    conn.createStatement().execute("DELETE FROM " + tableName);
                    conn.createStatement().execute("INSERT INTO " + tableName + " SELECT * FROM backupdb." + tableName);
                }

                conn.createStatement().execute("DETACH DATABASE backupdb;");
                return null;
            });

            runIntegrityCheck();
            
            return "Successfully reverted to backup.";


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
    }

    private void checkIfValidSQLiteFile(Path backupDir) throws Exception {
        
            byte[] header = Files.newInputStream(backupDir).readNBytes(16);
            String headerStr = new String(header, StandardCharsets.UTF_8);
            if (!headerStr.startsWith("SQLite format 3")) {
                throw new IllegalArgumentException("This backup file is not supported.");
            }
    }

    private void validateMetaData(Path backupDir) {
        String sql = """
               SELECT CASE
                    WHEN "app_id" = ? AND "schema_version" = ?
                    THEN 1
                    ELSE 0
                END AS "is_valid_backup"
                FROM backupdb."app_meta" WHERE "id" = 1;                
                """;
        try {
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class, expectedAppId, expectedSchemaVersion);
            if (result != 1) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("This is not a valid backup file.");
        }
        
    }

}
