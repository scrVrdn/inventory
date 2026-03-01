package io.github.scrvrdn.inventory.services.utility;

import java.io.IOException;
import java.nio.file.Path;

public interface BackupService {

    String createBackup() throws Exception;
    String createBackup(Path backupLocation) throws Exception;
    String revertToBackup(Path backupDir) throws Exception;
    Path getBackupDir() throws IOException;
}
