package io.github.scrvrdn.inventory.repositories.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import io.github.scrvrdn.inventory.mappers.PersonByBookIdRowMapper;
import io.github.scrvrdn.inventory.mappers.PersonRowMapper;


@ExtendWith(MockitoExtension.class)
public class BookPersonRepositoryImplTests {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PersonRowMapper personRowMapper;

    @Mock
    private PersonByBookIdRowMapper personByBookIdRowMapper;

    @InjectMocks
    private BookPersonRepositoryImpl underTest;

    @Test
    public void testThatAssignAuthorsGeneratesCorrectSql() {
        long bookId = 1L;
        List<Long> authorIds = List.of(1L, 2L, 3L);
        
        String expectedSql = """
                INSERT INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, 'AUTHOR', ?)
                ON CONFLICT ("book_id", "person_id", "role") DO UPDATE
                SET "order_index" = "excluded"."order_index";
                """;


        underTest.assignAuthorsToBook(bookId, authorIds);
        verify(jdbcTemplate).batchUpdate(
                            eq(expectedSql),
                            any(BatchPreparedStatementSetter.class)
                            );
    }

    @Test
    public void testThatFindAuthorsByBookIdGenereatesCorrectSql() {
        long bookId = 1L;

        String expectedSql = """
                SELECT * FROM "book_person"
                JOIN "persons" ON "book_person"."person_id" = "persons"."id"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;

        underTest.findAuthorsByBookId(bookId);

        verify(jdbcTemplate).query(expectedSql, personRowMapper, bookId);
    }

    @Test
    public void testThatFindAuthorIdsByBookIdGeneratesCorrectSql() {
        long bookId = 1L;

        String expectedSql = """
                SELECT "person_id" FROM "book_person"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;

        underTest.findAuthorIdsByBookId(bookId);
        
        verify(jdbcTemplate).queryForList(expectedSql, Long.class, bookId);
    }

    @Test
    public void testThatFindAuthorsGroupedByBookIdGeneratesCorrectSql() {
        underTest.findAllAuthorsGroupedByBookId();

        String expectedSql = """
                SELECT * FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'AUTHOR'
                ORDER BY "order_index";
                """;

        verify(jdbcTemplate).query(expectedSql, personByBookIdRowMapper);
    }

    @Test
    public void testThatRemoveAuthorsGeneratesCorrectSql() {
        long bookid = 1L;
        List<Long> authorIds = List.of(1L, 2L, 3L);

        String expectedSql = """
                DELETE FROM "book_person"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?
                AND "person_id" = ?;
                """;

        underTest.removeAuthorsFromBook(bookid, authorIds);
        
        verify(jdbcTemplate).batchUpdate(
            eq(expectedSql),
            eq(authorIds),
            eq(authorIds.size()),
            Mockito.<ParameterizedPreparedStatementSetter<Long>>any());
    }

    @Test
    public void testThatAssignEditorsGeneratesCorrectSql() {
        long bookId = 1L;
        List<Long> editorIds = List.of(1L, 2L, 3L);

        String expectedSql = """
                INSERT INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, 'EDITOR', ?)
                ON CONFLICT ("book_id", "person_id", "role") DO UPDATE
                SET "order_index" = "excluded"."order_index";
                """;

        underTest.assignEditorsToBook(bookId, editorIds);

        verify(jdbcTemplate).batchUpdate(eq(expectedSql), any(BatchPreparedStatementSetter.class));
    }

    @Test
    public void testThatFindEditorsByBookIdGeneratesCorrectSql() {
        long bookId = 1L;

        String expectedSql = """
                SELECT * FROM "book_person"
                JOIN "persons" ON "book_person"."person_id" = "persons"."id"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                ORDER BY "order_index"; 
                """;

        underTest.findEditorsByBookId(bookId);

        verify(jdbcTemplate).query(expectedSql, personRowMapper, bookId);
    }

    @Test
    public void testThatFindEditorIdsByBookIdGeneratesCorrectSql() {
        long bookId = 1L;

        String expectedSql = """
                SELECT "person_id" FROM "book_person"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;
        underTest.findEditorIdsByBookId(bookId);

        verify(jdbcTemplate).queryForList(expectedSql, Long.class, bookId);
    }

    @Test
    public void testThatFindEditorsGroupedByBookIdGeneratesCorrectSql() {
        String expectedSql = """
                SELECT * FROM "book_person"
                JOIN "persons" ON "book_person"."person_id" = "persons"."id"
                WHERE "role" = 'EDITOR'
                ORDER BY "order_index";
                """;
        underTest.findAllEditorsGroupedByBookId();
        verify(jdbcTemplate).query(expectedSql, personByBookIdRowMapper);
    }

    @Test
    public void testThatRemoveEditorsGeneratesCorrectSql() {
        long bookId = 1L;
        List<Long> editorIds = List.of(1L, 2L, 3L);

        String expectedSql = """
                DELETE FROM "book_person"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                AND "person_id" = ?;
                """;
        underTest.removeEditorsFromBook(bookId, editorIds);
        verify(jdbcTemplate).batchUpdate(
                            eq(expectedSql),
                            eq(editorIds),
                            eq(editorIds.size()),
                            Mockito.<ParameterizedPreparedStatementSetter<Long>>any());
    }

}
