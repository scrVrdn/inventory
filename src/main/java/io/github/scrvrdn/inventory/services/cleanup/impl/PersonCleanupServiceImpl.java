package io.github.scrvrdn.inventory.services.cleanup.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.scrvrdn.inventory.services.cleanup.PersonCleanupService;

@Service
public class PersonCleanupServiceImpl implements PersonCleanupService {

    private final JdbcTemplate jdbcTemplate;
    
    public PersonCleanupServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public void cleanupUnusedPersons() {
        String query = """
                DELETE FROM "persons"
                WHERE NOT EXISTS (
                    SELECT 1 FROM "book_person"
                    WHERE "book_person"."person_id" = "persons"."id"
                );
                """;

        jdbcTemplate.update(query);
                
    }
    
}
