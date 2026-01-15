package io.github.scrvrdn.inventory.repositories.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.domain.Publisher;
import io.github.scrvrdn.inventory.repositories.impl.BookRepositoryImpl.BookRowMapper;
import io.github.scrvrdn.inventory.repositories.impl.PersonRepositoryImpl.PersonRowMapper;
import io.github.scrvrdn.inventory.repositories.impl.PublisherRepositoryImpl.PublisherRowMapper;

@ExtendWith(MockitoExtension.class)
public class BookRepositoryImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BookRepositoryImpl underTest;

    @Test
    public void testCreateBookGeneratesCorrectSql() throws SQLException{
        Book book = TestDataUtil.createTestBook();
        ArgumentCaptor<PreparedStatementCreator> captor = ArgumentCaptor.forClass(PreparedStatementCreator.class);

        when(jdbcTemplate.update(captor.capture(), any(KeyHolder.class)))
            .thenAnswer(invocation -> {
                KeyHolder keyHolder = invocation.getArgument(1);
                keyHolder.getKeyList().add(Map.of("id", 1L));
                return 1;
            });

        underTest.create(book);
        
        String expectedSql = """
                INSERT OR IGNORE INTO "books" ("title", "year", "isbn10", "isbn13", "shelf_mark")
                VALUES (?, ?, ?, ?, ?);
                """;
        
        PreparedStatementCreator psc = captor.getValue();
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
            .thenReturn(mockPreparedStatement);
        psc.createPreparedStatement(mockConnection);

        verify(mockConnection).prepareStatement(
            argThat(sql -> sql.equals(expectedSql)),
            eq(Statement.RETURN_GENERATED_KEYS)
        );
    }

    @Test
    public void testThatFindByIdGeneratesCorrectSql() {
        long id = 1;
        underTest.findById(id);

        String expectedSql = """
                SELECT * FROM "books" WHERE "id" = ?;
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(BookRowMapper.class),
            eq(id)
        );

    }

    @Test
    public void testThatFindAllGeneratesCorrectSql() {
        underTest.findAll();

        String expectedSql = """
                SELECT * FROM "books";
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(BookRowMapper.class)
        );
    }

    @Test
    public void testThatUpdateGeneratesCorrectSql() {
        Book book = TestDataUtil.createTestBook();
        book.setId(1L);

        underTest.update(book);

        String expectedSql = """
                UPDATE "books"
                SET "title" = ?, "year" = ?, "isbn10" = ?, "isbn13" = ?, "shelf_mark" = ?
                WHERE "id" = ?;
                """;

        verify(jdbcTemplate).update(
            expectedSql,
            book.getTitle(),
            book.getYear(),
            book.getIsbn10(),
            book.getIsbn13(),
            book.getShelfMark(),
            book.getId()
        );  
    }

    @Test
    public void testThatDeleteGeneratesCorrectSql() {
        long id = 1L;
        underTest.delete(id);
        String expectedSql = """
                DELETE FROM "books" WHERE "id" = ?;
                """;
        verify(jdbcTemplate).update(expectedSql, id);
    }

    @Test
    public void testThatAssignToAuthorGeneratesCorrectSql() {
        Book book = TestDataUtil.createTestBook();
        book.setId(1L);
        Person author = TestDataUtil.createTestPerson();
        author.setId(1L);
        underTest.assignToAuthor(book, author);
        
        String expectedSql = """
                INSERT INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, ?, ?);
                """;
        verify(jdbcTemplate).update(
            expectedSql,
            book.getId(),
            author.getId(),
            "AUTHOR",
            0
        );
    }

    @Test
    public void testThatAssignToEditorsGeneratesCorrectSql() {
        Book book = TestDataUtil.createTestBook();
        book.setId(1L);

        Person editor = TestDataUtil.createTestPerson3();
        editor.setId(3L);

        underTest.assignToEditor(book, editor);

        String expectedSql = """
                INSERT INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, ?, ?);
                """;

        verify(jdbcTemplate).update(
            expectedSql,
            book.getId(),
            editor.getId(),
            "EDITOR",
            0
        );
    }

    @Test
    public void testThatAssignToPublisherGeneratesCorrectSql() {
        Book book = TestDataUtil.createTestBook();
        book.setId(1L);

        Publisher publisher = TestDataUtil.createTestPublisher();
        publisher.setId(1L);

        underTest.assignToPublisher(book, publisher);

        String expectedSql = """
                INSERT INTO "published" ("book_id", "publisher_id")
                VALUES (?, ?);
                """;

        verify(jdbcTemplate).update(
            expectedSql,
            book.getId(),
            publisher.getId()
        );
    }

    @Test
    public void testThatFindAuthorsGeneratesCorrectSql() {
        Book book = TestDataUtil.createTestBook();
        book.setId(1L);
        underTest.findAuthors(book);

        String expectedSql = """
                SELECT "persons"."id", "last_name", "first_names" FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?;
                """;

        verify(jdbcTemplate).query(
                    eq(expectedSql),
                    any(PersonRowMapper.class),
                    eq(book.getId())
        );
    }

    @Test
    public void testThatFindEditorsGeneratesCorrectSql() {
        Book book = TestDataUtil.createTestBook();
        book.setId(1L);
        underTest.findEditors(book);

        String expectedSql = """
                SELECT "persons"."id", "last_name", "first_names" FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PersonRowMapper.class),
            eq(book.getId())
        );
    }

    @Test
    public void testThatFindPublisherGeneratesCorrectSql() {
        Book book = TestDataUtil. createTestBook();
        book.setId(1L);
        underTest.findPublisher(book);

        String expectedSql = """
                SELECT "publishers"."id", "name", "location" FROM "publishers"
                JOIN "published" ON "publishers"."id" = "published"."publisher_id"
                WHERE "published"."book_id" = ?;
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PublisherRowMapper.class),
            eq(book.getId())
        );
    }

}
