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
import io.github.scrvrdn.inventory.domain.Publisher;
import io.github.scrvrdn.inventory.repositories.impl.PublisherRepositoryImpl.PublisherByBookIdMapper;
import io.github.scrvrdn.inventory.repositories.impl.PublisherRepositoryImpl.PublisherRowMapper;

@ExtendWith(MockitoExtension.class)
public class PublisherRepositoryImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PublisherRepositoryImpl underTest;

    @Test
    public void testThatCreateGeneratesCorrectSql() throws SQLException {
        Publisher publisher = TestDataUtil.createTestPublisher();
        ArgumentCaptor<PreparedStatementCreator> captor = ArgumentCaptor.forClass(PreparedStatementCreator.class);

        when(jdbcTemplate.update(captor.capture(), any(KeyHolder.class)))
            .thenAnswer(invocation -> {
                KeyHolder keyHolder = invocation.getArgument(1);
                keyHolder.getKeyList().add(Map.of("id", 1L));
                return 1;
            });

        underTest.create(publisher);

        String expectedSql = """
                INSERT OR IGNORE INTO "publishers" ("name", "location")
                VALUES (?, ?);
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
        long id = 1L;
        underTest.findById(id);

        String expectedSql = """
                SELECT * FROM "publishers" WHERE "id" = ?;
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PublisherRowMapper.class),
            eq(id)
        );
    }

    @Test
    public void testThatFindAllGeneratesCorrectSql() {
        underTest.findAll();

        String expectedSql = """
                SELECT * FROM "publishers";
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PublisherRowMapper.class)
        );
    }

    @Test
    public void testThatFindPublisherGroupedByBookIdGeneratesCorrectSql() {
        underTest.findPublishersGroupedByBookId();

        String expectedSql = """
                SELECT * FROM "publishers"
                JOIN "published" ON "publishers"."id" = "published"."publisher_id";
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PublisherByBookIdMapper.class)
        );
    }

    @Test
    public void testThatUpdateGeneratesCorrectSql() {
        Publisher publisher = TestDataUtil.createTestPublisher();
        publisher.setId(1L);
        underTest.update(publisher);

        String expectedSql = """
                UPDATE "publishers"
                SET "name" = ?, "location" = ?
                WHERE "id" = ?;
                """;
        
        verify(jdbcTemplate).update(
            expectedSql,
            publisher.getName(),
            publisher.getLocation(),
            publisher.getId()
        );
    }

    @Test
    public void testThatDeleteGeneratesCorrectSql() {
        long id = 1L;
        underTest.delete(id);

        String expectedSql = """
                DELETE FROM "publishers" WHERE "id" = ?;
                """;
        
        verify(jdbcTemplate).update(
            expectedSql,
            id
        );
    }
}
