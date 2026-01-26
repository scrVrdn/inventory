package io.github.scrvrdn.inventory.services.cleanup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.Publisher;
import io.github.scrvrdn.inventory.repositories.BookPublisherRepository;
import io.github.scrvrdn.inventory.repositories.BookRepository;
import io.github.scrvrdn.inventory.repositories.PublisherRepository;
import io.github.scrvrdn.inventory.services.cleanup.PublisherCleanupService;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class PublisherCleanupServiceIntegrationTests {
    private final JdbcTemplate jdbcTemplate;

    private final PublisherCleanupService underTest;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final BookPublisherRepository bookPublisherRepository;

    @Autowired
    public PublisherCleanupServiceIntegrationTests(final JdbcTemplate jdbcTemplate,
        final PublisherCleanupService underTest,
        final BookRepository bookRepository,
        final PublisherRepository publisherRepository,
        final BookPublisherRepository bookPublisherRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.bookPublisherRepository = bookPublisherRepository;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "book_person", "published");
    }

     @Test
    public void testThatOnlyUnassignedPublishersAreDeleted() {
        Publisher publisher1 = TestDataUtil.createTestPublisher();
        publisherRepository.create(publisher1);

        Publisher publisher2 = TestDataUtil.createTestPublisher2();
        publisherRepository.create(publisher2);

        Publisher publisher3 = TestDataUtil.createTestPublisher3();
        publisherRepository.create(publisher3);

        Book book = TestDataUtil.createTestBook();
        bookRepository.create(book);
        bookPublisherRepository.assignPublisherToBook(book.getId(), publisher3.getId());


        underTest.cleanupUnusedPublishers();

        List<Publisher> result = publisherRepository.findAll();
        assertThat(result)
            .hasSize(1)
            .containsExactly(publisher3);
    }
}
