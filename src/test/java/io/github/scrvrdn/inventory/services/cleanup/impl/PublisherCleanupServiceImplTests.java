package io.github.scrvrdn.inventory.services.cleanup.impl;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;


@ExtendWith(MockitoExtension.class)
public class PublisherCleanupServiceImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PublisherCleanupServiceImpl underTest;

    @Test
    public void testThatCleanUpUnusedPublishersGeneratesCorrectSql() {
        String expectedSql = """
                DELETE FROM "publishers"
                WHERE NOT EXISTS (
                    SELECT 1 FROM "published"
                    WHERE "published"."publisher_id" = "publishers"."id"
                );
                """;

        underTest.cleanupUnusedPublishers();

        verify(jdbcTemplate).update(expectedSql);
    }
}
