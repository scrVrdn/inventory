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
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"    
                    ) AS "editors",
                    "publishers"."location" || ': ' || "publishers"."name" AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
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
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    "publishers"."location" || ': ' || "publishers"."name" AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP BY b."id";
                """;

        verify(jdbcTemplate).query(eq(expectedSql), any(EntryRowRowMapper.class));
    }

}
