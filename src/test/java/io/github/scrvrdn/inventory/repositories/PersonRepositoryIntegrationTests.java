package io.github.scrvrdn.inventory.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class PersonRepositoryIntegrationTests {

    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository underTest;
    private final BookRepository bookRepository;

    @Autowired
    public PersonRepositoryIntegrationTests(final JdbcTemplate jdbcTemplate, final PersonRepository underTest, final BookRepository bookRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
        this.bookRepository = bookRepository;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "persons");
    }

    @Test
    public void testThatPersonCanBeCreatedAndRetrieved() {
        Person person = TestDataUtil.createTestPerson();
        underTest.create(person);

        Optional<Person> result = underTest.findById(person.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(person);
    }

    @Test
    public void testThatNoDuplicateIsCreated() {
        Person person1 = TestDataUtil.createTestPerson();
        underTest.create(person1);

        Person person2 = TestDataUtil.createTestPerson();
        underTest.create(person2);

        Optional<Person> result = underTest.findById(person2.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(person1);
    }

    @Test
    public void testThatAllPersonsCanBeCreated() {
        Person person1 = TestDataUtil.createTestPerson();
        Person person2 = TestDataUtil.createTestPerson2();
        Person person3 = TestDataUtil.createTestPerson3();
        Person person4 = TestDataUtil.createTestPerson4();
        underTest.createAll(List.of(person1, person2, person3, person4));

        List<Person> result = underTest.findAll();
        assertThat(result)
                .hasSize(4)
                .containsExactly(person1, person2, person3, person4);
    }

    @Test
    public void testThatAllAuthorsGroupedByBookIdCanBeRetrieved() {
        Person person1 = TestDataUtil.createTestPerson();
        Person person2 = TestDataUtil.createTestPerson2();
        Person person3 = TestDataUtil.createTestPerson3();
        Person person4 = TestDataUtil.createTestPerson4();
        underTest.createAll(List.of(person1, person2, person3, person4));

        Book book1 = TestDataUtil.createTestBook();
        bookRepository.create(book1);
        Book book2 = TestDataUtil.createTestBook2();
        bookRepository.create(book2);
        Book book3 = TestDataUtil.createTestBook3();
        bookRepository.create(book3);

        bookRepository.assignToAuthor(book1, List.of(person1, person2));
        bookRepository.assignToAuthor(book2, List.of(person1, person2, person3));
        bookRepository.assignToAuthor(book3, List.of(person4));

        Map<Long, List<Person>> result = underTest.findAuthorsGroupedByBookId();
        
        assertThat(result)
            .hasSize(3)
            .containsKeys(book1.getId(), book2.getId(), book3.getId())
            .containsExactly(
                Map.entry(book1.getId(), List.of(person1, person2)),
                Map.entry(book2.getId(), List.of(person1, person2, person3)),
                Map.entry(book3.getId(), List.of(person4))
            );
    }

    @Test
    public void testThatAllEditorsGroupedByBookIdCanBeRetrieved() {
        Person person1 = TestDataUtil.createTestPerson();
        Person person2 = TestDataUtil.createTestPerson2();
        Person person3 = TestDataUtil.createTestPerson3();
        Person person4 = TestDataUtil.createTestPerson4();
        underTest.createAll(List.of(person1, person2, person3, person4));

        Book book1 = TestDataUtil.createTestBook();
        bookRepository.create(book1);
        Book book2 = TestDataUtil.createTestBook2();
        bookRepository.create(book2);
        Book book3 = TestDataUtil.createTestBook3();
        bookRepository.create(book3);

        bookRepository.assignToEditor(book1, List.of(person1));
        bookRepository.assignToEditor(book2, List.of(person1, person2));
        bookRepository.assignToEditor(book3, List.of(person1, person2, person3, person4));

        Map<Long, List<Person>> result = underTest.findEditorsGroupedByBookId();

        assertThat(result)
            .hasSize(3)
            .containsExactly(
                Map.entry(book1.getId(), List.of(person1)),
                Map.entry(book2.getId(), List.of(person1, person2)),
                Map.entry(book3.getId(), List.of(person1, person2, person3, person4))
            );
    }

    @Test
    public void testThatAllPersonsCanBeRetrieved() {
        Person person1 = TestDataUtil.createTestPerson();
        underTest.create(person1);

        Person person2 = TestDataUtil.createTestPerson2();
        underTest.create(person2);

        Person person3 = TestDataUtil.createTestPerson3();
        underTest.create(person3);

        List<Person> result = underTest.findAll();
        assertThat(result)
            .hasSize(3)
            .containsExactly(person1, person2, person3);
    }

    

    @Test
    public void testThatPersonCanBeUpdated() {
        Person person = TestDataUtil.createTestPerson();
        underTest.create(person);
        person.setLastName("UPDATED");
        person.setFirstNames("UPDATED");
        
        underTest.update(person);
        Optional<Person> result = underTest.findById(person.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(person);
    }

    @Test
    public void testThatPersonCanBeDeleted() {
        Person person = TestDataUtil.createTestPerson();
        underTest.create(person);

        underTest.delete(person.getId());

        Optional<Person> result = underTest.findById(person.getId());
        assertThat(result).isEmpty();
    }    
}
