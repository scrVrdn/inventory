package io.github.scrvrdn.inventory.repositories.impl;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.scrvrdn.inventory.mappers.PublisherByBookIdMapper;
import io.github.scrvrdn.inventory.mappers.PublisherRowMapper;

@ExtendWith(MockitoExtension.class)
public class BookPublisherRepositoryImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PublisherRowMapper publisherRowMapper;

    @Mock
    private PublisherByBookIdMapper publisherByBookIdMapper;

    @InjectMocks
    private BookPublisherRepositoryImpl underTest;

    @Test
    public void testThatAssignPublisherToBookGeneratesCorrectSql() {
        long bookId = 1L;
        long publisherId = 1L;

        String expectedSql = """
                INSERT INTO "published" ("book_id", "publisher_id")
                VALUES (?, ?)
                ON CONFLICT ("book_id") DO UPDATE
                SET "publisher_id" = "excluded"."publisher_id";
                """;

        underTest.assignPublisherToBook(bookId, publisherId);

        verify(jdbcTemplate).update(expectedSql, bookId, publisherId);
    }

    @Test
    public void testThatFindPublisherGeneratesCorrectSql() {
        long bookId = 1L;       

        String expectedSql = """
                SELECT "publishers"."id", "name", "location" FROM "publishers"
                JOIN "published" ON "publishers"."id" = "published"."publisher_id"
                WHERE "published"."book_id" = ?;
                """;
        underTest.findPublisherByBookId(bookId);

        verify(jdbcTemplate).query(expectedSql, publisherRowMapper, bookId);
    }

    @Test
    public void testThatFindPublisherIdGeneratesCorrectSql() {
        long bookId = 1L;

        String expectedSql = """
                SELECT "publisher_id" FROM "published"
                WHERE "book_id" = ?;
                """;
        underTest.findPublisherIdByBookId(bookId);

        verify(jdbcTemplate).queryForList(expectedSql, Long.class, bookId);
    }

    @Test
    public void testThatRemovePublisherGeneratesCorrectSql() {
        long bookId = 1L;
        long publisherId = 1L;

        underTest.removePublisherFromBook(bookId, publisherId);

        String expectedSql = """
                DELETE FROM "published"
                WHERE "book_id" = ? AND "publisher_id" = ?;
                """;

        verify(jdbcTemplate).update(expectedSql, bookId, publisherId);
    }

    @Test
    public void testThatFindPublisherGroupedByBookIdGeneratesCorrectSql() {
        underTest.findPublishersGroupedByBookId();

        String expectedSql = """
                SELECT * FROM "publishers"
                JOIN "published" ON "publishers"."id" = "published"."publisher_id";
                """;

        verify(jdbcTemplate).query(expectedSql, publisherByBookIdMapper);
    }
}
