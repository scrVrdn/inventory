package io.github.scrvrdn.inventory.services.cleanup.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.services.cleanup.PublisherCleanupService;

@Service
public class PublisherCleanupServiceImpl implements PublisherCleanupService {

    private final JdbcTemplate jdbcTemplate;

    public PublisherCleanupServiceImpl (final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanupUnusedPublishers() {
        String query = """
                DELETE FROM "publishers"
                WHERE NOT EXISTS (
                    SELECT 1 FROM "published"
                    WHERE "published"."publisher_id" = "publishers"."id"
                );
                """;
        
        jdbcTemplate.update(query);
    }
}
