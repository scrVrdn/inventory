package io.github.scrvrdn.inventory.services.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.Person;

@SpringBootTest
@ActiveProfiles("test")
public class PersonServiceIntegrationTests {

    private JdbcTemplate jdbcTemplate;

    private PersonService underTest;

    @Autowired
    public PersonServiceIntegrationTests(final JdbcTemplate jdbcTemplate, final PersonService underTest) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
    }


    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "book_person", "published");
    }

    @Test
    public void testThatUpdateAuthorsByNameReturnsListOfDistinctIds() {
        Person author1 = TestDataUtil.createTestPerson();
        Person author2 = TestDataUtil.createTestPerson2();
        Person author3 = TestDataUtil.createTestPerson3();

        List<Long> result = underTest.updateAuthorsByName(List.of(author1, author3, author1, author2, author2, author3));
        assertThat(result)
            .hasSize(3)
            .containsExactly(author1.getId(), author3.getId(), author2.getId());
    }

     @Test
    public void testThatUpdateEditorsByNameReturnsListOfDistinctIds() {
        Person editor1 = TestDataUtil.createTestPerson();
        Person editor2 = TestDataUtil.createTestPerson2();
        Person editor3 = TestDataUtil.createTestPerson3();

        List<Long> result = underTest.updateAuthorsByName(List.of(editor1, editor3, editor1, editor2, editor2, editor3));
        assertThat(result)
            .hasSize(3)
            .containsExactly(editor1.getId(), editor3.getId(), editor2.getId());
    }
}
