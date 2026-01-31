package io.github.scrvrdn.inventory.services.facade.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.mappers.EntryDtoExtractor;
import io.github.scrvrdn.inventory.mappers.FlatEntryDtoRowMapper;
import io.github.scrvrdn.inventory.services.BookService;

@ExtendWith(MockitoExtension.class)
public class EntryServiceImplTests {
    
    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private BookService bookService;

    @Mock
    private EntryDtoExtractor entryDtoExtractor;

    @Mock
    private FlatEntryDtoRowMapper flatEntryDtoRowMapper;

    @Spy
    @InjectMocks
    private EntryServiceImpl underTest;

    @Test
    public void testThatCreateEmptyEntryCallsBookRepository() {
        Book mockBook = mock(Book.class);
        when(mockBook.getId()).thenReturn(1L);        
        when(underTest.createEmptyBook()).thenReturn(mockBook);

        FlatEntryDto expected = new FlatEntryDto(1L, null, null, null, null, null, null);

        Optional<FlatEntryDto> result = underTest.createEmptyEntry();        

        assertThat(result.get()).isEqualTo(expected);
        verify(bookService).create(mockBook);
    }

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
    public void testThatGetFlatEntryDtoGeneratesCorrectSql() {
        long bookId = 1L;
        underTest.getFlatEntryDto(bookId);

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
           
                verify(jdbcTemplate).query(expectedSql, flatEntryDtoRowMapper, bookId);
    }

    @Test
    public void testThatGetNFlatEntryDtosGeneratesCorrectSql() {
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
                HAVING b."id" > ?
                LIMIT ?;
                """;
        
        int numberOfEntries = 3;
        long fromRow = 4;
        underTest.getFlatEntryDtos(fromRow, numberOfEntries);
        verify(jdbcTemplate).query(expectedSql, flatEntryDtoRowMapper, fromRow, numberOfEntries);
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

    @Test
    public void testThatDeleteCallsBookService() {
        long bookId = 1L;
        underTest.delete(bookId);
        verify(bookService).delete(bookId);
    }

    @Test
    public void testThatNumberOfRowsCallsBookService() {
        underTest.numberOfRows();
        verify(bookService).numberOfRows();
    }

}
