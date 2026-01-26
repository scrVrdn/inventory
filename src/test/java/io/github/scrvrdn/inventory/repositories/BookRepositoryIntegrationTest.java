package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

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
import io.github.scrvrdn.inventory.dto.Publisher;

@SpringBootTest
public class BookRepositoryIntegrationTest {

    private final JdbcTemplate jdbcTemplate;
    private final BookRepository underTest;
    private final PersonRepository personRepository;
    private final PublisherRepository publisherRepository;
    private final BookPersonRepository bookPersonRepository;
    private final BookPublisherRepository bookPublisherRepository;

    @Autowired
    public BookRepositoryIntegrationTest(
                final JdbcTemplate jdbcTemplate,
                final BookRepository underTest,
                final PersonRepository personRepository,
                final PublisherRepository publisherRepository,
                final BookPersonRepository bookPersonRepository,
                final BookPublisherRepository bookPublisherRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
        this.personRepository = personRepository;
        this.publisherRepository = publisherRepository;
        this.bookPersonRepository = bookPersonRepository;
        this.bookPublisherRepository = bookPublisherRepository;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "book_person", "published");
    }


    @Test
    public void testThatBookCanBeCreatedAndRetrieved() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        Optional<Book> result = underTest.findById(book.getId());        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(book);
    }

    @Test
    public void testThatNoDuplicateIsCreated() {
        Book book1 = TestDataUtil.createTestBook();
        underTest.create(book1);

        Book book2 = TestDataUtil.createTestBook();
        underTest.create(book2);

        Optional<Book> result = underTest.findById(book2.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(book1);
    }

    @Test
    public void testThatAllBooksCanBeRetrieved() {
        Book book1 = TestDataUtil.createTestBook();
        underTest.create(book1);

        Book book2 = TestDataUtil.createTestBook2();
        underTest.create(book2);

        Book book3 = TestDataUtil.createTestBook3();
        underTest.create(book3);

        List<Book> result = underTest.findAll();
        assertThat(result)
            .hasSize(3)
            .containsExactly(book1, book2, book3);
    }

    @Test
    public void testThatBookCanBeUpdated() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        book.setTitle("UPDATED");
        underTest.update(book);

        Optional<Book> result = underTest.findById(book.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(book);
    }

    @Test
    public void testThatBookCanBeDeleted() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        underTest.delete(book.getId());
        Optional<Book> result = underTest.findById(book.getId());
        assertThat(result).isEmpty();
    }

    @Test
    public void testThatBookDeletionDeletesRelationToAuthor() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        Person author = TestDataUtil.createTestPerson();
        personRepository.create(author);

        bookPersonRepository.assignAuthorsToBook(book.getId(), List.of(author.getId()));

        underTest.delete(book.getId());

        List<Person> result = bookPersonRepository.findAuthorsByBookId(book.getId());
        assertThat(result).isEmpty();
    }

    @Test
    public void testThatBookDeletionDeletesRelationToEditor() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        Person editor = TestDataUtil.createTestPerson3();
        
        personRepository.create(editor);   
        bookPersonRepository.assignEditorsToBook(book.getId(), List.of(editor.getId()));

        underTest.delete(book.getId());

        List<Person> result = bookPersonRepository.findEditorsByBookId(book.getId());
        assertThat(result).isEmpty();
    }

    @Test
    public void testThatBookDeletionDeletesRelationToPublisher() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        Publisher publisher = TestDataUtil.createTestPublisher();
        publisherRepository.create(publisher);

        bookPublisherRepository.assignPublisherToBook(book.getId(), publisher.getId());

        underTest.delete(book.getId());

        Optional<Publisher> result = bookPublisherRepository.findPublisherByBookId(book.getId());
        assertThat(result).isEmpty();
    }

    @Test
    public void testThatBookDeletionDoesNotDeleteAuthor() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        Person author = TestDataUtil.createTestPerson();
        personRepository.create(author);

        bookPersonRepository.assignAuthorsToBook(book.getId(), List.of(author.getId()));

        underTest.delete(book.getId());
        Optional<Person> result = personRepository.findById(author.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(author);
    }

    @Test
    public void testThatBookDeletionDoesNotDeleteEditor() {
        Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        Person editor = TestDataUtil.createTestPerson3();
        personRepository.create(editor);

        bookPersonRepository.assignEditorsToBook(book.getId(), List.of(editor.getId()));

        underTest.delete(book.getId());

        Optional<Person> result = personRepository.findById(editor.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(editor);
    }

    @Test
    public void testThatBookDeletionDoesNotDeletePublisher() {
         Book book = TestDataUtil.createTestBook();
        underTest.create(book);

        Publisher publisher = TestDataUtil.createTestPublisher();
        publisherRepository.create(publisher);

        bookPublisherRepository.assignPublisherToBook(book.getId(), publisher.getId());

        underTest.delete(book.getId());

        Optional<Publisher> result = publisherRepository.findById(publisher.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(publisher);
    }

}
