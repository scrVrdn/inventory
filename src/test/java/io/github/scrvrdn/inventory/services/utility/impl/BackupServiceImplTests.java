package io.github.scrvrdn.inventory.services.utility.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class BackupServiceImplTests {

    @TempDir
    private Path tempDir;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private BackupValidationServiceImpl validationService;

    @Mock
    private Supplier<String> backupFileNameSupplier;
    
    @Spy
    @InjectMocks
    private BackupServiceImpl underTest;

    @Test
    public void testThatCreatesBackupAndValidates() throws Exception {
        String expectedFileName = "backup-20260226-111500.db.bak";
        when(backupFileNameSupplier.get()).thenReturn(expectedFileName);

        Path expectedBackupPath = tempDir.resolve(expectedFileName);
        when(underTest.getBackupDir()).thenReturn(tempDir);
        
        when(validationService.runIntegrityCheck()).thenReturn(true);
        when(validationService.checkBackupIntegrity(expectedBackupPath)).thenReturn(true);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);


        String result = underTest.createBackup();
        
        
        assertThat(result).isEqualTo("Backup successful: " + expectedFileName);
        

        verify(validationService).runIntegrityCheck();

        verify(jdbcTemplate).execute(sqlCaptor.capture());
        assertThat(sqlCaptor.getValue()).contains("VACUUM INTO", expectedBackupPath.toAbsolutePath().toString());

        verify(validationService).checkBackupIntegrity(expectedBackupPath);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThatValidatesAndRevertsToBackup() throws Exception {
        String expectedFileName = "backup-20260226-111500.db.bak";
        Path expectedBackupPath = tempDir.resolve(expectedFileName);
        

        when(validationService.validateBackup(expectedBackupPath)).thenReturn(true);
        when(validationService.runIntegrityCheck()).thenReturn(true);
        
       
        String result = underTest.revertToBackup(expectedBackupPath);
        
        assertThat(result).isEqualTo("Successfully reverted to backup.");

        verify(validationService).validateBackup(expectedBackupPath);
        verify(jdbcTemplate).execute(any(ConnectionCallback.class));
        verify(validationService).runIntegrityCheck();

    }
}
