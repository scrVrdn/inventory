package io.github.scrvrdn.inventory.repositories.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.PageRequest;
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
    public void testThatGetSortedEntriesGeneratesCorrectSql() {
        String expectedSql = """
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
            GROUP BY b."id"
            ORDER BY "authors" COLLATE NOCASE DESC, b."id" DESC
            LIMIT ? OFFSET ?;
        """;

        int pageIndex = 0;
        int pageSize = 10;
        PageRequest request = new PageRequest(pageIndex, pageSize, null, "\"authors\"", "DESC", true);
        underTest.getSortedEntries(request);

        verify(jdbcTemplate).query(
            argThat(TestDataUtil.sqlEquals(expectedSql)),
            eq(flatEntryDtoRowMapper),
            eq(request.pageSize()),
            eq(request.pageIndex() * request.pageSize())
        );
            
    }

    @Test
    public void testThatFilterAndSortGeneratesCorrectSql() {
        String filterSql = getFilteredEntriesSql();
        String sortSql = getSortedEntriesSql();
        int pageSize = 10;
        int pageIndex = 0;
       
        Object[] params = {"%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%", "%edgar%",
                            "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%", "%poe%"};

        List<Long> filteredIds = List.of(1L, 2L);
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class), eq(params))).thenReturn(filteredIds);
        
        PageRequest request = new PageRequest(pageIndex, pageSize, "edgar poe", "\"authors\"", "ASC", true);
        Object[] params2 = {1L, 2L, request.pageSize(), request.pageIndex() * request.pageSize()};

        underTest.getSortedAndFilteredEntries(request);
        
        verify(jdbcTemplate).queryForList(argThat(TestDataUtil.sqlEquals(filterSql)), eq(Long.class), eq(params));
        verify(jdbcTemplate).query(
            argThat(TestDataUtil.sqlEquals(sortSql)),
            eq(flatEntryDtoRowMapper),
            eq(params2)
        );
    }

    private String getFilteredEntriesSql() {
        String sql = """
                SELECT b."id"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE 1 = 1 AND (
                    b."title" LIKE ?
                    OR b."year" LIKE ?
                    OR b."shelf_mark" LIKE ?
                    OR b."isbn10" LIKE ?
                    OR b."isbn13" LIKE ?
                    OR p."last_name" LIKE ?
                    OR p."first_names" LIKE ?
                    OR "publishers"."name" LIKE ?
                    OR "publishers"."location" LIKE ?
                ) AND (
                    b."title" LIKE ?
                    OR b."year" LIKE ?
                    OR b."shelf_mark" LIKE ?
                    OR b."isbn10" LIKE ?
                    OR b."isbn13" LIKE ?
                    OR p."last_name" LIKE ?
                    OR p."first_names" LIKE ?
                    OR "publishers"."name" LIKE ?
                    OR "publishers"."location" LIKE ?
                ) GROUP BY b.\"id\";
                """;
        return sql;
    }

    private String getSortedEntriesSql() {
        String sql = """
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
                WHERE b."id" IN (?, ?)
                GROUP BY b."id"
                ORDER BY "authors" COLLATE NOCASE ASC, b."id" ASC
                LIMIT ? OFFSET ?;
                """;

        return sql;
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
