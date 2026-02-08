package io.github.scrvrdn.inventory.repositories.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.mappers.EntryDtoExtractor;
import io.github.scrvrdn.inventory.mappers.EntryDtoListExtractor;
import io.github.scrvrdn.inventory.mappers.FlatEntryDtoRowMapper;

@ExtendWith(MockitoExtension.class)
public class EntryViewRepositoryImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EntryDtoExtractor entryDtoExtractor;

    @Mock
    private EntryDtoListExtractor entryDtoListExtractor;

    @Mock
    private FlatEntryDtoRowMapper flatEntryDtoRowMapper;

    @InjectMocks
    private EntryViewRepositoryImpl underTest;

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

                verify(jdbcTemplate).query(expectedSql, entryDtoExtractor, bookId);
    }

    @Test
    public void testThatFindAllGeneratesCorrectSql() {
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
                ORDER BY b."id", "book_person"."role", "book_person"."order_index";
                """;
        underTest.findAll();
        verify(jdbcTemplate).query(expectedSql, entryDtoListExtractor);
    }

    @Test
    public void testThatGetFlatEntryDtoByBookIdGeneratesCorrectSql() {
        long bookId = 1L;

        String expectedSql = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"    
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE b."id" = ?
                GROUP BY b."id";
                """;
            
            underTest.getFlatEntryDtoByBookId(bookId);
            verify(jdbcTemplate).query(expectedSql, flatEntryDtoRowMapper, bookId);
    }

    @Test
    public void testThatGetNextFlatEntryDtoAfterBookIdGeneratesCorrectSql() {
        long bookId = 1L;
        
        String expectedSql = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"    
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE b."id" > ?
                GROUP BY b."id"
                ORDER BY b."id"
                LIMIT 1;
                """;

            underTest.getNextFlatEntryDtoAfterBookId(bookId);
            verify(jdbcTemplate).query(expectedSql, flatEntryDtoRowMapper, bookId);
    }

    @Test
    public void testThatGetFlatEntryDtosGeneratesCorrectSql() {
        String expectedSql = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP BY b."id"
                ORDER BY b."id"
                LIMIT ?
                OFFSET ?;
                """;
        
        int numberOfEntries = 3;
        int fromRow = 4;
        underTest.getFlatEntryDtos(numberOfEntries, fromRow);
        verify(jdbcTemplate).query(expectedSql, flatEntryDtoRowMapper, numberOfEntries, fromRow);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThatFilterAndSortGeneratesCorrectSqlForData() {
        String expectedDataSql = """
                SELECT
                    b."id", b."title" AS "title", b."year" AS "year", b."shelf_mark" AS "shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP by b."id"
                HAVING 1 = 1 AND (
                    "title" LIKE ?
                    OR "year" LIKE ?
                    OR "shelf_mark" LIKE ?
                    OR "authors" LIKE ?
                    OR "editors" LIKE ?
                    OR "publisher" LIKE ?
                    OR "isbn10" LIKE ?
                    OR "isbn13" LIKE ?
                ) AND (
                    "title" LIKE ?
                    OR "year" LIKE ?
                    OR "shelf_mark" LIKE ?
                    OR "authors" LIKE ?
                    OR "editors" LIKE ?
                    OR "publisher" LIKE ?
                    OR "isbn10" LIKE ?
                    OR "isbn13" LIKE ?
                ) ORDER BY "authors"
                \nLIMIT ? OFFSET ?;
                """;
        
        int pageSize = 10;
        int pageIndex = 0;
       
        Object[] params1 = {"%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%",
                            "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%"};

        when(jdbcTemplate.queryForObject(Mockito.anyString(), any(RowMapper.class), eq(params1))).thenReturn(1);

        Object[] params2 = {"%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%",
                            "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%",
                            pageSize, (pageIndex * pageSize)    
                        };

        
        String sortBy = """
                "authors"
                """;

        underTest.getSortedAndFilteredEntries(pageSize, pageIndex, sortBy, new String[]{"edgar", "poe"});
        verify(jdbcTemplate).query(expectedDataSql, flatEntryDtoRowMapper, params2);
        
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThatFilterAndSortGeneratesCorrectSqlForRowCount() {
        String expectedRowCountSql = """
                SELECT COUNT(*),
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; '
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; '
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP by b."id"
                HAVING 1 = 1 AND (
                    "title" LIKE ?
                    OR "year" LIKE ?
                    OR "shelf_mark" LIKE ?
                    OR "authors" LIKE ?
                    OR "editors" LIKE ?
                    OR "publisher" LIKE ?
                    OR "isbn10" LIKE ?
                    OR "isbn13" LIKE ?
                ) AND (
                    "title" LIKE ?
                    OR "year" LIKE ?
                    OR "shelf_mark" LIKE ?
                    OR "authors" LIKE ?
                    OR "editors" LIKE ?
                    OR "publisher" LIKE ?
                    OR "isbn10" LIKE ?
                    OR "isbn13" LIKE ?
                );""";

        Object[] params = {"%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%",
                            "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%"};
        

        when(jdbcTemplate.queryForObject(eq(expectedRowCountSql), any(RowMapper.class),  eq(params))).thenReturn(1);

        Page result = underTest.getSortedAndFilteredEntries(10, 0, "authors", new String[]{"edgar", "poe"});
        assertThat(result.totalNumberOfRows()).isEqualTo(1);
        verify(jdbcTemplate).queryForObject(eq(expectedRowCountSql), any(RowMapper.class),  eq(params));
    }

    @Test
    public void testThatGetAllFlatEntryDtoGeneratesCorrectSql() {
        underTest.getAllFlatEntryDtos();

        String expectedSql = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP BY b."id";
                """;

        verify(jdbcTemplate).query(expectedSql, flatEntryDtoRowMapper);
    }
}
