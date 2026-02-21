package io.github.scrvrdn.inventory.services.cleanup;

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
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.repositories.BookPersonRepository;
import io.github.scrvrdn.inventory.repositories.BookRepository;
import io.github.scrvrdn.inventory.repositories.PersonRepository;

@SpringBootTest
@ActiveProfiles("test")
public class PersonCleanupServiceIntegrationTests {

    private final JdbcTemplate jdbcTemplate;
    private final PersonCleanupService underTest;
    private final BookRepository bookRepository;
    private final PersonRepository personRepository;
    private final BookPersonRepository bookPersonRepository;

    @Autowired
    public PersonCleanupServiceIntegrationTests(
        final JdbcTemplate jdbcTemplate,
        final PersonCleanupService underTest,
        final BookRepository bookRepository,
        final PersonRepository personRepository,
        final BookPersonRepository bookPersonRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
        this.bookRepository = bookRepository;
        this.personRepository = personRepository;
        this.bookPersonRepository = bookPersonRepository;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "book_person", "published");
    }

    @Test
    public void testThatOnlyUnassignedPersonAreDeleted() {
        Person person1 = TestDataUtil.createTestPerson();
        personRepository.create(person1);

        Person person2 = TestDataUtil.createTestPerson2();
        personRepository.create(person2);

        Person person3 = TestDataUtil.createTestPerson3();
        personRepository.create(person3);

        Person person4 = TestDataUtil.createTestPerson4();
        personRepository.create(person4);

        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        bookPersonRepository.assignAuthorsToBook(book.getId(), List.of(person3.getId()));
        bookPersonRepository.assignEditorsToBook(book.getId(), List.of(person4.getId()));

        underTest.cleanupUnusedPersons();

        List<Person> result = personRepository.findAll();
        assertThat(result)
            .hasSize(2)
            .containsExactly(person3, person4);
    }
}
