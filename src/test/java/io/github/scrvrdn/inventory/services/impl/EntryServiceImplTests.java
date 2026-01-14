package io.github.scrvrdn.inventory.services.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.scrvrdn.inventory.services.impl.EntryServiceImpl.EntryRowRowMapper;

@ExtendWith(MockitoExtension.class)
public class EntryServiceImplTests {
    
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EntryServiceImpl underTest;


    @Test
    public void testThatGetEntryRowGeneratesCorrectSql() {
        long bookId = 1L;
        underTest.getEntryRow(bookId);

        String expectedSql = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(a."author_name", '; ') AS "authors",
                    GROUP_CONCAT(e."editor_name", '; ') AS "editors",                    
                    p."location" || ': ' || p."name" AS "publisher"
                FROM "books" b
                LEFT JOIN (
                    SELECT DISTINCT "book_id", "last_name" || ', ' || "first_names" AS "author_name"
                    FROM "authored"
                    JOIN "persons" ON "authored"."person_id" = "persons"."id"
                    ORDER BY "author_name"
                ) a ON b."id" = a."book_id"
                LEFT JOIN (
                    SELECT DISTINCT "book_id", "last_name" || ', ' || "first_names" AS "editor_name"
                    FROM "edited"
                    JOIN "persons" ON "edited"."person_id" = "persons"."id"
                    ORDER BY "editor_name"
                ) e ON b."id" = e."book_id"                
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" p ON "published"."publisher_id" = p."id"
                WHERE b."id" = ?
                GROUP BY b."id";
                """;
           
                verify(jdbcTemplate).query(eq(expectedSql), any(EntryRowRowMapper.class), eq(bookId));
    }

    @Test
    public void testThatGetAllEntryRowsGeneratesCorrectSql() {
        underTest.getAllEntryRows();

        String expectedSql = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(a."author_name", '; ') AS "authors",
                    GROUP_CONCAT(e."editor_name", '; ') AS "editors",
                    p."location" || ': ' || p."name" AS "publisher"
                FROM "books" b
                LEFT JOIN (
                    SELECT DISTINCT "book_id", "last_name" || ', ' || "first_names" AS "author_name"
                    FROM "authored"
                    JOIN "persons" ON "authored"."person_id" = "persons"."id"
                    ORDER BY "author_name"
                ) a ON b."id" = a."book_id"
                LEFT JOIN (
                    SELECT DISTINCT "book_id", "last_name" || ', ' || "first_names" AS "editor_name"
                    FROM "edited"
                    JOIN "persons" ON "edited"."person_id" = "persons"."id"
                    ORDER BY "editor_name"
                ) e ON b."id" = e."book_id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" p ON "published"."publisher_id" = p."id"
                GROUP BY b."id";
                """;

        verify(jdbcTemplate).query(eq(expectedSql), any(EntryRowRowMapper.class));
    }

}
