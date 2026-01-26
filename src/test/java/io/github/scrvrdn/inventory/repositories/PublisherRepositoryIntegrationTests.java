package io.github.scrvrdn.inventory.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.Publisher;

@SpringBootTest
public class PublisherRepositoryIntegrationTests {

    private final JdbcTemplate jdbcTemplate;
    private final PublisherRepository underTest;
    private final BookRepository bookRepository;

    @Autowired
    public PublisherRepositoryIntegrationTests(final JdbcTemplate jdbcTemplate, final PublisherRepository underTest, final BookRepository bookRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
        this.bookRepository = bookRepository;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "publishers");
    }

    @Test
    public void testThatPublisherCanBeCreatedAndRetrieved() {
        Publisher publisher = TestDataUtil.createTestPublisher();
        underTest.create(publisher);

        Optional<Publisher> result = underTest.findById(publisher.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(publisher);
    }

    @Test
    public void testThatNoDuplicateIsCreated() {
        Publisher publisher1 = TestDataUtil.createTestPublisher();
        underTest.create(publisher1);

        Publisher publisher2 = TestDataUtil.createTestPublisher();
        underTest.create(publisher2);

        Optional<Publisher> result = underTest.findById(publisher2.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(publisher1);
    }

    @Test
    public void testThatAllPublishersCanBeRetrieved() {
        Publisher publisher1 = TestDataUtil.createTestPublisher();
        underTest.create(publisher1);

        Publisher publisher2 = TestDataUtil.createTestPublisher2();
        underTest.create(publisher2);

        Publisher publisher3 = TestDataUtil.createTestPublisher3();
        underTest.create(publisher3);

        List<Publisher> result = underTest.findAll();
        assertThat(result)
            .hasSize(3)
            .containsExactly(publisher1, publisher2, publisher3);
    }

    

    @Test
    public void testThatPublisherCanBeUpdated() {
        Publisher publisher = TestDataUtil.createTestPublisher();
        underTest.create(publisher);

        publisher.setName("UPDATED");
        publisher.setLocation("UPDATED");

        underTest.update(publisher);
        Optional<Publisher> result = underTest.findById(publisher.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(publisher);
    }

    @Test
    public void testThatPublisherCanBeDeleted() {
        Publisher publisher = TestDataUtil.createTestPublisher();
        underTest.create(publisher);

        underTest.delete(publisher.getId());

        Optional<Publisher> result = underTest.findById(publisher.getId());
        assertThat(result).isEmpty();
    }
}
