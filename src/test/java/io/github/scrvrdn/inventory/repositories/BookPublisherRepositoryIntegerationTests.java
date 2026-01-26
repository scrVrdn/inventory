package io.github.scrvrdn.inventory.repositories;

import java.util.Map;
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
import io.github.scrvrdn.inventory.dto.Publisher;

@SpringBootTest
public class BookPublisherRepositoryIntegerationTests {

    private final JdbcTemplate jdbcTemplate;
    private final BookPublisherRepository underTest;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;

    @Autowired
    public BookPublisherRepositoryIntegerationTests(
            final JdbcTemplate jdbcTemplate,
            final BookPublisherRepository underTest,
            final BookRepository bookRepository,
            final PublisherRepository publisherRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "book_person", "published");
    }

    @Test
    public void testThatPublisherCanBeAssignedAndRetrievedByBook() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Publisher publisher = TestDataUtil.createTestPublisher();
        publisherRepository.create(publisher);

        underTest.assignPublisherToBook(book.getId(), publisher.getId());

        Optional<Publisher> result = underTest.findPublisherByBookId(book.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(publisher);
    }

    @Test
    public void testThatPublishersGroupedByBookIdCanBeRetrieved() {
        Publisher publisher1 = TestDataUtil.createTestPublisher();
        publisherRepository.create(publisher1);
        Publisher publisher2 = TestDataUtil.createTestPublisher2();
        publisherRepository.create(publisher2);
        Publisher publisher3 = TestDataUtil.createTestPublisher3();
        publisherRepository.create(publisher3);

        Book book1 = TestDataUtil.createTestBook();
        bookRepository.create(book1);
        Book book2 = TestDataUtil.createTestBook2();
        bookRepository.create(book2);
        Book book3 = TestDataUtil.createTestBook3();
        bookRepository.create(book3);

        underTest.assignPublisherToBook(book1.getId(), publisher1.getId());
        underTest.assignPublisherToBook(book2.getId(), publisher2.getId());
        underTest.assignPublisherToBook(book3.getId(), publisher3.getId());

        Map<Long, Publisher> result = underTest.findPublishersGroupedByBookId();
        
        assertThat(result)
            .hasSize(3)
            .containsExactly(Map.entry(book1.getId(), publisher1), Map.entry(book2.getId(), publisher2), Map.entry(book3.getId(), publisher3));
    }

    @Test
    public void testThatPublisherCanBeRemovedFromBook() {
        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);

        Publisher publisher = TestDataUtil.createTestPublisher();
        publisherRepository.create(publisher);
        underTest.assignPublisherToBook(book.getId(), publisher.getId());

        underTest.removePublisherFromBook(book.getId(), publisher.getId());

        Optional<Publisher> result = underTest.findPublisherByBookId(book.getId());
        assertThat(result).isEmpty();
    }
}
