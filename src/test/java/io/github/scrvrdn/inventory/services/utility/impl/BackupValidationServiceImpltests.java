package io.github.scrvrdn.inventory.services.utility.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class BackupValidationServiceImpltests {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BackupValidationServiceImpl underTest;

    @Test
    public void testThatRunsIntegrityCheck() {
        String expectedSql = "PRAGMA integrity_check";
        when(jdbcTemplate.queryForObject(expectedSql, String.class)).thenReturn("ok");

        boolean result = underTest.runIntegrityCheck();
        verify(jdbcTemplate).queryForObject(expectedSql, String.class);
        assertThat(result).isTrue();
    }
}
