package io.github.scrvrdn.inventory.services.utility;

import java.nio.file.Path;
import java.sql.SQLException;

public interface BackupValidationService {
    
    public boolean runIntegrityCheck();
    public boolean validateBackup(Path backupDir) throws Exception;    
    public boolean checkBackupIntegrity(Path backupDir) throws SQLException;
}
