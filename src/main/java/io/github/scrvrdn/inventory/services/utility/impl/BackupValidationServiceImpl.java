package io.github.scrvrdn.inventory.services.utility.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.services.utility.BackupValidationService;

@Service
public class BackupValidationServiceImpl implements BackupValidationService {

    @Value("${app.db.meta.app-id}")
    private String expectedAppId;

    @Value("${app.db.meta.schema-version}")
    private Integer expectedSchemaVersion;

    private final JdbcTemplate jdbcTemplate;

    public BackupValidationServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean runIntegrityCheck() {
        String result = jdbcTemplate.queryForObject("PRAGMA integrity_check", String.class);
        return result.equals("ok");
    }

    @Override
    public boolean checkBackupIntegrity(Path backupPath) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + backupPath.toString());) {
                boolean isValid = checkBackupIntegrity(conn);
                return isValid;
        } catch (Exception e) {
            throw new RuntimeException("Backup integrity check failed.", e);
        }
    }

    private boolean checkBackupIntegrity(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("PRAGMA integrity_check")) {
                String result = rs.next() ? rs.getString(1) : "unknown";
                return result.equals("ok");
            }
        }            
    }

    @Override
    public boolean validateBackup(Path backupDir) throws Exception {
        try {
            boolean isValidSQLiteFile = checkIfValidSQLiteFile(backupDir);
            if (!isValidSQLiteFile) return false;
           
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + backupDir.toString())) {
                boolean isValid = checkBackupIntegrity(conn);
                if (!isValid) return false;

                boolean metaDataIsValid = validateMetaData(conn);
                if (!metaDataIsValid) return false;

                return true;
            }            
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Backup validation failed.", e);
        }        
        
    }

    
    private boolean checkIfValidSQLiteFile(Path backupDir) throws Exception {
        try {
            byte[] header = Files.newInputStream(backupDir).readNBytes(16);
            String headerStr = new String(header, StandardCharsets.UTF_8);
            return headerStr.startsWith("SQLite format 3");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("File validation failed.", e);
        }
    }

    
    private boolean validateMetaData(Connection conn) throws SQLException {
        String sql = """
               SELECT CASE
                    WHEN "app_id" = ? AND "schema_version" = ?
                    THEN 1
                    ELSE 0
                END AS "is_valid_backup"
                FROM "app_meta" WHERE "id" = 1;                
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, expectedAppId);
            stmt.setInt(2, expectedSchemaVersion);
            
            try (ResultSet rs = stmt.executeQuery()) {
                Integer result = rs.next() ? rs.getInt(1) : 0;
                return result == 1;
            }
        }
    }
    
}
