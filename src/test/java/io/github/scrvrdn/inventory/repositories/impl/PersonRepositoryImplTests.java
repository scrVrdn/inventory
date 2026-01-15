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
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.repositories.impl.PersonRepositoryImpl.PersonByBookIdRowMapper;
import io.github.scrvrdn.inventory.repositories.impl.PersonRepositoryImpl.PersonRowMapper;


@ExtendWith(MockitoExtension.class)
public class PersonRepositoryImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PersonRepositoryImpl underTest;

    @Test
    public void testThatCreatePersonGeneratesCorrectSql() throws SQLException {
        Person person = TestDataUtil.createTestPerson();
        ArgumentCaptor<PreparedStatementCreator> captor = ArgumentCaptor.forClass(PreparedStatementCreator.class);

        when(jdbcTemplate.update(captor.capture(), any(KeyHolder.class)))
            .thenAnswer(invocation -> {
                KeyHolder keyholder = invocation.getArgument(1);
                keyholder.getKeyList().add(Map.of("id", 1L));
                return 1;
            });

        underTest.create(person);
        
        String expectedSql = """
                INSERT OR IGNORE INTO "persons" ("last_name", "first_names")
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
    public void testThatFindByIdCreatesCorrectSql() {
        long id = 1;
        underTest.findById(id);

        String expectedSql = """
                SELECT * FROM "persons" WHERE "id" = ?;
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PersonRepositoryImpl.PersonRowMapper.class),
            eq(id)
        );
    }

    @Test
    public void testThatFindAuthorsByBookIdGeneratesCorrectSql() {
        underTest.findAuthorsGroupedByBookId();

        String expectedSql = """
                SELECT * FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'AUTHOR'
                ORDER BY "order_index";
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PersonByBookIdRowMapper.class)
        );
    }

    @Test
    public void testThatFindEditorsByBookIdGeneratesCorrectSql() {
        underTest.findEditorsGroupedByBookId();

        String expectedSql = """
                SELECT * FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'EDITOR'
                ORDER BY "order_index";
                """;

        verify(jdbcTemplate).query(
            eq(expectedSql),
            any(PersonByBookIdRowMapper.class)
        );
    }

    @Test
    public void testThatFindAllGeneratesCorrectSql() {
        underTest.findAll();

        String expectedSql = """
                SELECT * FROM "persons";
                """;

        verify(jdbcTemplate).query(
                eq(expectedSql),
                any(PersonRowMapper.class));
    }

    @Test
    public void testThatUpdateGeneratesCorrectSql() {
        Person person = TestDataUtil.createTestPerson();
        person.setId(1L);
        underTest.update(person);

        String expectedSql = """
                UPDATE "persons"
                SET "last_name" = ?, "first_names" = ?
                WHERE "id" = ?;
                """;

        verify(jdbcTemplate).update(
            expectedSql,
            person.getLastName(),
            person.getFirstNames(),
            person.getId()
        );
    }

    @Test
    public void testThatDeleteGeneratesCorrectSql() {
        long id = 1L;
        underTest.delete(id);

        String expectedSql = """
                DELETE FROM "persons" WHERE "id" = ?;
                """;

        verify(jdbcTemplate).update(expectedSql, id);
    }
}
