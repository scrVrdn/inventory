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

import io.github.scrvrdn.inventory.services.impl.EntryServiceImpl.EntryDtoExtractor;
import io.github.scrvrdn.inventory.services.impl.EntryServiceImpl.FlatEntryDtoRowMapper;

@ExtendWith(MockitoExtension.class)
public class EntryServiceImplTests {
    
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EntryServiceImpl underTest;

    @Test
    public void testThatFindByIdGeneratesCorrectSql() {
        long bookId = 1L;
        underTest.findById(bookId);

        String expectedSql = """
                SELECT
                    b."id" AS "id", b."title", b."year", b."isbn10", b."isbn13", b."shelf_mark",
                    a."id" AS "author_id", a."last_name" AS "author_last_name", a."first_names" AS "author_first_names",
                    e."id" AS "editor_id", e."last_name" AS "editor_last_name", e."first_names" AS "editor_first_names",
                    "publishers"."id" AS "publisher_id", "publishers"."location" AS "publisher_location", "publishers"."name" AS "publisher_name"
                    FROM "books" b
                    LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                    LEFT JOIN "persons" a ON "book_person"."person_id" = a."id" AND "book_person"."role" = 'AUTHOR'
                    LEFT JOIN "persons" e ON "book_person"."person_id" = e."id" AND "book_person"."role" = 'EDITOR'
                    LEFT JOIN "published" ON b."id" = "published"."book_id"
                    LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                    WHERE b."id" = ?
                    ORDER BY "book_person"."role", "book_person"."order_index";
                """;

                verify(jdbcTemplate).query(eq(expectedSql), any(EntryDtoExtractor.class), eq(bookId));
    }

    @Test
    public void testThatGetFlatEntryDtoGeneratesCorrectSql() {
        long bookId = 1L;
        underTest.getFlatEntryDto(bookId);

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
           
                verify(jdbcTemplate).query(eq(expectedSql), any(FlatEntryDtoRowMapper.class), eq(bookId));
    }

    @Test
    public void testThatGetAllFlatEntryDtoGeneratesCorrectSql() {
        underTest.getAllFlatEntryDto();

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

        verify(jdbcTemplate).query(eq(expectedSql), any(FlatEntryDtoRowMapper.class));
    }

}
