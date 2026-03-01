package io.github.scrvrdn.inventory.services.utility.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.function.Supplier;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.services.utility.BackupService;
import io.github.scrvrdn.inventory.services.utility.BackupValidationService;


@Service
public class BackupServiceImpl implements BackupService {

    private final JdbcTemplate jdbcTemplate;
    private final BackupValidationService backupValidationService;
    private final Supplier<String> backupFileNameSupplier;

    public BackupServiceImpl(final JdbcTemplate jdbcTemplate, final BackupValidationService backupValidationService, final Supplier<String> backupFileNameSupplier) {
        this.jdbcTemplate = jdbcTemplate;
        this.backupValidationService = backupValidationService;
        this.backupFileNameSupplier = backupFileNameSupplier;
    }
    
    @Override
    public String createBackup() throws Exception {
        Path backupLocation = getBackupDir();
        return createBackup(backupLocation);
    }

    @Override
    public String createBackup(Path backupLocation) throws Exception {
        try {
                Path backupDir = backupLocation.resolve(backupFileNameSupplier.get());
                
                boolean isValid = backupValidationService.runIntegrityCheck();
                if (!isValid) return "Backup failed: live DB corrupted";

                jdbcTemplate.execute("VACUUM INTO '" + backupDir.toString() + "';");

                boolean backupIsValid = backupValidationService.checkBackupIntegrity(backupDir);
                
                if (!backupIsValid) return "Created Backup file is corrupt";

                return "Backup successful: " + backupDir.getFileName();

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

    }

    public Path getBackupDir() throws IOException {
        Path backupDir = Paths.get(System.getProperty("user.home"), ".inventory", "backup");
        Files.createDirectories(backupDir);
        return backupDir;
    }
    
    @Override
    public String revertToBackup(Path backupDir) throws Exception {
        
        try {            
            boolean isValid = backupValidationService.validateBackup(backupDir);
            if (!isValid) return "Backup file is corrupt or not supported";

            String attachSql = String.format("ATTACH DATABASE '%s' AS backupdb;", backupDir.toString());
            jdbcTemplate.execute(attachSql);

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

            boolean validAfterIntegrityCheck = backupValidationService.runIntegrityCheck();
            if (!validAfterIntegrityCheck) return "Live DB corrupt after reverting to Backup";
            
            return "Successfully reverted to backup.";

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
