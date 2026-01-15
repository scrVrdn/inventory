package io.github.scrvrdn.inventory.services.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.domain.Publisher;
import io.github.scrvrdn.inventory.dto.EntryDto;
import io.github.scrvrdn.inventory.dto.EntryRow;
import io.github.scrvrdn.inventory.repositories.BookRepository;
import io.github.scrvrdn.inventory.repositories.PersonRepository;
import io.github.scrvrdn.inventory.repositories.PublisherRepository;
import io.github.scrvrdn.inventory.services.EntryService;

@Service
public class EntryServiceImpl implements EntryService {

    private final JdbcTemplate jdbcTemplate;
    private final BookRepository bookRepository;
    private final PersonRepository personRepository;
    private final PublisherRepository publisherRepository;

    public EntryServiceImpl(final JdbcTemplate jdbcTemplate, final BookRepository bookRepository, final PersonRepository personRepository, final PublisherRepository publisherRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookRepository = bookRepository;
        this.personRepository = personRepository;
        this.publisherRepository = publisherRepository;
    }

    @Override
    public void create(EntryDto entryDto) {
        createEntities(entryDto);
        createRelations(entryDto);        
    }

    private void createEntities(EntryDto entryDto) {
        bookRepository.create(entryDto.getBook());
        personRepository.createAll(entryDto.getAuthors());
        personRepository.createAll(entryDto.getEditors());
        publisherRepository.create(entryDto.getPublisher());
    }

    private void createRelations(EntryDto entryDto) {
        bookRepository.assignToAuthor(entryDto.getBook(), entryDto.getAuthors());
        bookRepository.assignToEditor(entryDto.getBook(), entryDto.getEditors());
        bookRepository.assignToPublisher(entryDto.getBook(), entryDto.getPublisher());
    }

    @Override
    public List<EntryDto> findAll() {
        List<Book> books = bookRepository.findAll();
        Map<Long, List<Person>> authors = personRepository.findAuthorsGroupedByBookId();
        Map<Long, List<Person>> editors = personRepository.findEditorsGroupedByBookId();
        Map<Long, Publisher> publishers = publisherRepository.findPublishersGroupedByBookId();

        return books.stream()
                    .map(b -> EntryDto.builder()
                        .book(b)
                        .authors(authors.get(b.getId()))
                        .editors(editors.get(b.getId()))
                        .publisher(publishers.get(b.getId()))
                        .build()
                    ).toList();
    }

    @Override
    public Optional<EntryRow> getEntryRow(long bookId) {
        String query = """
                SELECT 
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    "publishers"."location" || ': ' || "publishers"."name" AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE b."id" = ?
                GROUP BY b."id";
                """;
                
        List<EntryRow> result = jdbcTemplate.query(query, new EntryRowRowMapper(), bookId);
        return result.stream().findFirst();
    }

    @Override
    public List<EntryRow> getAllEntryRows() {
        String query = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN p."last_name" || ', ' || p."first_names" END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    "publishers"."location" || ': ' || "publishers"."name" AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP BY b."id";
                """;

        return jdbcTemplate.query(query, new EntryRowRowMapper());
    }

    @Override
    public void update(EntryDto entry) {
        if (entry.isUpdated("book")) {
            bookRepository.update(entry.getBook());
        }

        if (entry.isUpdated("authors")) {
            
        }

        if (entry.isUpdated("editors")) {

        }

        if (entry.isUpdated("publisher")) {
            publisherRepository.update(entry.getPublisher());
        }

        entry.getUpdatedFields().clear();
    }

    public static class EntryRowRowMapper implements RowMapper<EntryRow> {
        @Override
        public EntryRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return EntryRow.builder()
                .bookId(rs.getLong("id"))
                .bookTitle(rs.getString("title"))
                .bookYear(rs.getObject("year", Integer.class))
                .shelfMark(rs.getString("shelf_mark"))
                .authors(rs.getString("authors"))
                .editors(rs.getString("editors"))
                .publisher(rs.getString("publisher"))
                .build();
        }
    }
}
