package io.github.scrvrdn.inventory.services.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.domain.Publisher;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
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
    public Optional<FlatEntryDto> createEmptyEntry() {
        Book emptyBook = createEmptyBook();
        bookRepository.create(emptyBook);
        return Optional.ofNullable(FlatEntryDto.builder()
                                                .bookId(emptyBook.getId())
                                                .build());
    }

    protected Book createEmptyBook() {
        return Book.builder().build();
    }
    
    @Override
    public void create(FullEntryDto entryDto) {
        createEntities(entryDto);
        createRelations(entryDto);        
    }

    private void createEntities(FullEntryDto entryDto) {
        bookRepository.create(entryDto.getBook());
        personRepository.createAll(entryDto.getAuthors());
        personRepository.createAll(entryDto.getEditors());
        publisherRepository.create(entryDto.getPublisher());
    }

    private void createRelations(FullEntryDto entryDto) {
        bookRepository.assignToAuthor(entryDto.getBook(), entryDto.getAuthors());
        bookRepository.assignToEditor(entryDto.getBook(), entryDto.getEditors());
        bookRepository.assignToPublisher(entryDto.getBook(), entryDto.getPublisher());
    }

    @Override
    public Optional<FullEntryDto> findById(long id) {
        String query = """
                SELECT
                    b."id" AS "id", b."title", b."year", b."isbn10", b."isbn13", b."shelf_mark",
                    a."id" AS "author_id", a."last_name" AS "author_last_name", a."first_names" AS "author_first_names",
                    e."id" AS "editor_id", e."last_name" AS "editor_last_name", e."first_names" AS "editor_first_names",
                    "publishers"."id" AS "publisher_id", "publishers"."location" AS "publisher_location", "publishers"."name" AS "publisher_name"
                    FROM "books" b
                    LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                    LEFT JOIN "persons" a ON "book_person"."person_id" = a."id" AND "book_person"."role" = 'AUTHOR'
                    LEFT JOIN "persons" e ON "book_person"."person_id" = e."id" AND "book_person"."role" = 'EDITOR'
                    LEFT JOIN "published" ON b."id" = "published"."book_id"
                    LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                    WHERE b."id" = ?
                    ORDER BY "book_person"."role", "book_person"."order_index";
                """;

        FullEntryDto entry = jdbcTemplate.query(query, new EntryDtoExtractor(), id);
        return Optional.ofNullable(entry);
    }

    @Override
    public List<FullEntryDto> findAll() {
        List<Book> books = bookRepository.findAll();
        Map<Long, List<Person>> authors = personRepository.findAuthorsGroupedByBookId();
        Map<Long, List<Person>> editors = personRepository.findEditorsGroupedByBookId();
        Map<Long, Publisher> publishers = publisherRepository.findPublishersGroupedByBookId();

        return books.stream()
                    .map(b -> FullEntryDto.builder()
                        .book(b)
                        .authors(authors.get(b.getId()))
                        .editors(editors.get(b.getId()))
                        .publisher(publishers.get(b.getId()))
                        .build()
                    ).toList();
    }

    @Override
    public Optional<FlatEntryDto> getFlatEntryDto(long bookId) {
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
                
        List<FlatEntryDto> result = jdbcTemplate.query(query, new FlatEntryDtoRowMapper(), bookId);
        return result.stream().findFirst();
    }

    @Override
    public List<FlatEntryDto> getAllFlatEntryDtos() {
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

        return jdbcTemplate.query(query, new FlatEntryDtoRowMapper());
    }

    @Override
    public void update(FullEntryDto entry) {
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

    public static class FlatEntryDtoRowMapper implements RowMapper<FlatEntryDto> {
        @Override
        public FlatEntryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            int yearInt = rs.getInt("year");
            Integer year = rs.wasNull() ? null : yearInt;

            return FlatEntryDto.builder()
                .bookId(rs.getLong("id"))
                .bookTitle(rs.getString("title"))
                .bookYear(year)
                .shelfMark(rs.getString("shelf_mark"))
                .authors(rs.getString("authors"))
                .editors(rs.getString("editors"))
                .publisher(rs.getString("publisher"))
                .build();
        }
    }

    public static class EntryDtoExtractor implements ResultSetExtractor<FullEntryDto> {
        @Override
        public FullEntryDto extractData(ResultSet rs) throws SQLException {
            if (!rs.next()) return null;

            int yearInt = rs.getInt("year");
            Integer year = rs.wasNull() ? null : yearInt;

            Book book = Book.builder()
                    .id(rs.getLong("id"))
                    .title(rs.getString("title"))
                    .year(year)
                    .isbn10(rs.getString("isbn10"))
                    .isbn13(rs.getString("isbn13"))
                    .shelfMark(rs.getString("shelf_mark"))
                    .build();
            
            long publisherId = rs.getLong("publisher_id");
            Publisher publisher = null;
            if (!rs.wasNull()) {
                publisher = Publisher.builder()
                    .id(publisherId)
                    .name(rs.getString("publisher_name"))
                    .location(rs.getString("publisher_location"))
                    .build();
            }

            

            List<Person> authors = new ArrayList<>();
            List<Person> editors = new ArrayList<>();

            do {
                Long authorId = rs.getLong("author_id");
                if (!rs.wasNull()) {
                    authors.add(Person.builder()
                            .id(authorId)    
                            .lastName(rs.getString("author_last_name"))
                            .firstNames(rs.getString("author_first_names"))
                            .build()
                    );
                }

                Long editorId = rs.getLong("editor_id");
                if (!rs.wasNull()) {
                    editors.add(Person.builder()
                            .id(editorId)
                            .lastName(rs.getString("editor_last_name"))
                            .firstNames(rs.getString("editor_first_names"))
                            .build()
                    );
                }

            } while (rs.next());

            return FullEntryDto.builder()
                        .book(book)
                        .authors(authors)
                        .editors(editors)
                        .publisher(publisher)
                        .build();
        }
    }
}
