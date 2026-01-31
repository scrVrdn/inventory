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
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.mappers.BookRowMapper;

@ExtendWith(MockitoExtension.class)
public class BookRepositoryImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private BookRowMapper bookRowMapper;

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
            expectedSql,
            bookRowMapper,
            id
        );

    }

    @Test
    public void testThatFindAllGeneratesCorrectSql() {
        underTest.findAll();

        String expectedSql = """
                SELECT * FROM "books";
                """;

        verify(jdbcTemplate).query(
            expectedSql,
            bookRowMapper
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
    public void testThatNumberOfRowsGeneratesCorrectSql() {
        
        String expectedSql = """
                SELECT "total_rows" FROM "row_counters" WHERE "table_name" = 'books';
                """;

        when(jdbcTemplate.queryForObject(expectedSql, int.class)).thenReturn(1);

        underTest.numberOfRows();
        verify(jdbcTemplate).queryForObject(expectedSql, int.class);
    }

}
