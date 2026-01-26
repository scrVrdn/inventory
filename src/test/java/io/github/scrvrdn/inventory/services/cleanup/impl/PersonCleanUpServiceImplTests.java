package io.github.scrvrdn.inventory.services.cleanup.impl;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class PersonCleanUpServiceImplTests {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PersonCleanupServiceImpl underTest;

    @Test
    public void testThatCleanupUnassignedPersonsGeneratesCorrectSql() {
        String expectedSql = """
                DELETE FROM "persons"
                WHERE NOT EXISTS (
                    SELECT 1 FROM "book_person"
                    WHERE "book_person"."person_id" = "persons"."id"
                );
                """;
        
        underTest.cleanupUnusedPersons();
        
        verify(jdbcTemplate).update(expectedSql);
    }
}
