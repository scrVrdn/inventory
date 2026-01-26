package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.Person;

@SpringBootTest
public class BookPersonRepositoryIntegrationTests {
    private final JdbcTemplate jdbcTemplate;
    private final BookPersonRepository underTest;
    private final BookRepository bookRepository;
    private final PersonRepository personRepository;

    @Autowired
    public BookPersonRepositoryIntegrationTests(final JdbcTemplate jdbcTemplate, final BookPersonRepository underTest, final BookRepository bookRepository, final PersonRepository personRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
        this.bookRepository = bookRepository;
        this.personRepository = personRepository;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "book_person", "published");
    }

    @Test
    public void testThatAuthorsCanBeAssignedToBookAndRetrieved() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Person author1 = TestDataUtil.createTestPerson();
        Person author2 = TestDataUtil.createTestPerson2();
        Person author3 = TestDataUtil.createTestPerson3();
        
        personRepository.create(author3);
        personRepository.create(author2);
        personRepository.create(author1);

        List<Long> authorIds = List.of(author1.getId(), author2.getId(), author3.getId());

        underTest.assignAuthorsToBook(book.getId(), authorIds);

        List<Person> result = underTest.findAuthorsByBookId(book.getId());
        assertThat(result)
            .hasSize(3)
            .containsExactly(author1, author2, author3);
    
    }

    @Test
    public void testThatAuthorIdsCanBeRetrievedByBookId() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Person author1 = TestDataUtil.createTestPerson();
        Person author2 = TestDataUtil.createTestPerson2();
        Person author3 = TestDataUtil.createTestPerson3();
        
        personRepository.create(author3);
        personRepository.create(author2);
        personRepository.create(author1);

        long bookId = book.getId();
        List<Long> authorIds = List.of(author1.getId(), author2.getId(), author3.getId());        
        underTest.assignAuthorsToBook(bookId, authorIds);

        List<Long> result = underTest.findAuthorIdsByBookId(bookId);
        assertThat(result)
                .hasSize(3)
                .containsExactly(author1.getId(), author2.getId(), author3.getId());
    }

    @Test
    public void testThatAllAuthorsGroupedByBookIdCanBeRetrieved() {
        Person person1 = TestDataUtil.createTestPerson();
        Person person2 = TestDataUtil.createTestPerson2();
        Person person3 = TestDataUtil.createTestPerson3();
        Person person4 = TestDataUtil.createTestPerson4();
        personRepository.createAll(List.of(person1, person2, person3, person4));

        Book book1 = TestDataUtil.createTestBook();
        bookRepository.create(book1);
        Book book2 = TestDataUtil.createTestBook2();
        bookRepository.create(book2);
        Book book3 = TestDataUtil.createTestBook3();
        bookRepository.create(book3);

        underTest.assignAuthorsToBook(book1.getId(), List.of(person1.getId(), person2.getId()));
        underTest.assignAuthorsToBook(book2.getId(), List.of(person1.getId(), person2.getId(), person3.getId()));
        underTest.assignAuthorsToBook(book3.getId(), List.of(person4.getId()));

        Map<Long, List<Person>> result = underTest.findAllAuthorsGroupedByBookId();
        
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
    public void testThatAuthorsCanBeRemovedFromBook() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Person author1 = TestDataUtil.createTestPerson();
        Person author2 = TestDataUtil.createTestPerson2();
        Person author3 = TestDataUtil.createTestPerson3();
        
        personRepository.create(author3);
        personRepository.create(author2);
        personRepository.create(author1);

        List<Long> authorIds = List.of(author1.getId(), author2.getId(), author3.getId());

        underTest.assignAuthorsToBook(book.getId(), authorIds);
        underTest.removeAuthorsFromBook(book.getId(), List.of(author1.getId(), author3.getId()));
    
        List<Person> result = underTest.findAuthorsByBookId(book.getId());
        assertThat(result)
            .hasSize(1)
            .containsExactly(author2);
    }

    @Test
    public void testThatEditorsCanBeAssignedAndRetrieved() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Person editor1 = TestDataUtil.createTestPerson();
        Person editor2 = TestDataUtil.createTestPerson2();
        Person editor3 = TestDataUtil.createTestPerson3();
        
        personRepository.create(editor3);
        personRepository.create(editor2);
        personRepository.create(editor1);

        List<Long> authorIds = List.of(editor1.getId(), editor2.getId(), editor3.getId());

        underTest.assignAuthorsToBook(book.getId(), authorIds);

        List<Person> result = underTest.findAuthorsByBookId(book.getId());
        assertThat(result)
            .hasSize(3)
            .containsExactly(editor1, editor2, editor3);
    }

    @Test
    public void testThatEditorIdsCanBeRetrievedByBookId() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Person editor1 = TestDataUtil.createTestPerson();
        Person editor2 = TestDataUtil.createTestPerson2();
        Person editor3 = TestDataUtil.createTestPerson3();
        
        personRepository.create(editor3);
        personRepository.create(editor2);
        personRepository.create(editor1);

        long bookId = book.getId();
        List<Long> authorIds = List.of(editor1.getId(), editor2.getId(), editor3.getId());        
        underTest.assignEditorsToBook(bookId, authorIds);

        List<Long> result = underTest.findEditorIdsByBookId(bookId);
        assertThat(result)
                .hasSize(3)
                .containsExactly(editor1.getId(), editor2.getId(), editor3.getId());
    }

    @Test
    public void testThatAllEditorsGroupedByBookIdCanBeRetrieved() {
        Person person1 = TestDataUtil.createTestPerson();
        Person person2 = TestDataUtil.createTestPerson2();
        Person person3 = TestDataUtil.createTestPerson3();
        Person person4 = TestDataUtil.createTestPerson4();
        personRepository.createAll(List.of(person1, person2, person3, person4));

        Book book1 = TestDataUtil.createTestBook();
        bookRepository.create(book1);
        Book book2 = TestDataUtil.createTestBook2();
        bookRepository.create(book2);
        Book book3 = TestDataUtil.createTestBook3();
        bookRepository.create(book3);

        underTest.assignEditorsToBook(book1.getId(), List.of(person1.getId()));
        underTest.assignEditorsToBook(book2.getId(), List.of(person1.getId(), person2.getId()));
        underTest.assignEditorsToBook(book3.getId(), List.of(person1.getId(), person2.getId(), person3.getId(), person4.getId()));
        
        Map<Long, List<Person>> result = underTest.findAllEditorsGroupedByBookId();

        assertThat(result)
            .hasSize(3)
            .containsExactly(
                Map.entry(book1.getId(), List.of(person1)),
                Map.entry(book2.getId(), List.of(person1, person2)),
                Map.entry(book3.getId(), List.of(person1, person2, person3, person4))
            );
    }
    
    @Test
    public void testThatEditorsCanBeRemovedFromBook() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Person editor1 = TestDataUtil.createTestPerson();
        Person editor2 = TestDataUtil.createTestPerson2();
        Person editor3 = TestDataUtil.createTestPerson3();
        
        personRepository.create(editor3);
        personRepository.create(editor2);
        personRepository.create(editor1);

        List<Long> authorIds = List.of(editor1.getId(), editor2.getId(), editor3.getId());

        underTest.assignAuthorsToBook(book.getId(), authorIds);
        underTest.removeAuthorsFromBook(book.getId(), List.of(editor1.getId(), editor3.getId()));
    
        List<Person> result = underTest.findAuthorsByBookId(book.getId());
        assertThat(result)
            .hasSize(1)
            .containsExactly(editor2);
    }

    @Test
    public void testThatOrderIndexIsCorrectlyUpdated() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Person author1 = TestDataUtil.createTestPerson();
        Person author2 = TestDataUtil.createTestPerson2();
        Person author3 = TestDataUtil.createTestPerson3();
        
        personRepository.create(author3);
        personRepository.create(author2);
        personRepository.create(author1);

        List<Long> authorIds = List.of(author1.getId(), author2.getId(), author3.getId());

        underTest.assignAuthorsToBook(book.getId(), authorIds);

        List<Person> result = underTest.findAuthorsByBookId(book.getId());
        assertThat(result)
            .hasSize(3)
            .containsExactly(author1, author2, author3);

        List<Long> newOrder = List.of(author3.getId(), author1.getId(), author2.getId());
        underTest.assignAuthorsToBook(book.getId(), newOrder);
        List<Person> newOrderResult = underTest.findAuthorsByBookId(book.getId());
        assertThat(newOrderResult)
            .hasSize(3)
            .containsExactly(author3, author1, author2);
    }
}
