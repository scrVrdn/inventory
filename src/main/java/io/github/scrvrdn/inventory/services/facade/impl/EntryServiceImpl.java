package io.github.scrvrdn.inventory.services.facade.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.dto.Publisher;
import io.github.scrvrdn.inventory.mappers.EntryDtoExtractor;
import io.github.scrvrdn.inventory.mappers.FlatEntryDtoRowMapper;
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.BookUpdateRequest;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.repositories.BookPersonRepository;
import io.github.scrvrdn.inventory.repositories.BookPublisherRepository;
import io.github.scrvrdn.inventory.repositories.BookRepository;
import io.github.scrvrdn.inventory.repositories.PersonRepository;
import io.github.scrvrdn.inventory.repositories.PublisherRepository;
import io.github.scrvrdn.inventory.services.BookService;
import io.github.scrvrdn.inventory.services.PersonService;
import io.github.scrvrdn.inventory.services.PublisherService;
import io.github.scrvrdn.inventory.services.facade.EntryService;

@Service
public class EntryServiceImpl implements EntryService {

    private final JdbcTemplate jdbcTemplate;
    private final BookRepository bookRepository;
    private final PersonRepository personRepository;
    private final PublisherRepository publisherRepository;
    private final BookPersonRepository bookPersonRepository;
    private final BookPublisherRepository bookPublisherRepository;

    private final BookService bookService;
    private final PersonService personService;
    private final PublisherService publisherService;

    public EntryServiceImpl(
        final JdbcTemplate jdbcTemplate,
        final BookRepository bookRepository,
        final PersonRepository personRepository,
        final PublisherRepository publisherRepository,
        final BookPersonRepository bookPersonRepository,
        final BookPublisherRepository bookPublisherRepository,
        final BookService bookService,
        final PersonService personService,
        final PublisherService publisherService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookRepository = bookRepository;
        this.personRepository = personRepository;
        this.publisherRepository = publisherRepository;
        this.bookPersonRepository = bookPersonRepository;
        this.bookPublisherRepository = bookPublisherRepository;

        this.bookService = bookService;
        this.personService = personService;
        this.publisherService = publisherService;
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
        long bookId = entryDto.getBook().getId();

        List<Long> authorIds = entryDto.getAuthors().stream()
                                                        .map(Person::getId)
                                                        .collect(Collectors.toList());

        List<Long> editorIds = entryDto.getEditors().stream()
                                                        .map(Person::getId)
                                                        .collect(Collectors.toList());

        bookPersonRepository.assignAuthorsToBook(bookId, authorIds);
        bookPersonRepository.assignEditorsToBook(bookId, editorIds);
        bookPublisherRepository.assignPublisherToBook(entryDto.getBook().getId(), entryDto.getPublisher().getId());
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
        Map<Long, List<Person>> authors = bookPersonRepository.findAllAuthorsGroupedByBookId();
        Map<Long, List<Person>> editors = bookPersonRepository.findAllEditorsGroupedByBookId();
        Map<Long, Publisher> publishers = bookPublisherRepository.findPublishersGroupedByBookId();

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
    public FlatEntryDto update(FullEntryDto entry) {

        List<Long> authorIds = personService.updateAuthorsByName(entry.getAuthors());
        List<Long> editorIds = personService.updateEditorsByName(entry.getEditors());
        Long publisherId = publisherService.updatePublisherByNameAndLocation(entry.getPublisher());
        
        BookUpdateRequest request = getBookUpdateRequest(entry.getBook(), authorIds, editorIds, publisherId);
        bookService.update(entry.getBook().getId(), request);

        return mapFullDtoToFlatDto(entry);
    }

    private BookUpdateRequest getBookUpdateRequest(Book book, List<Long> authorIds, List<Long> editorIds, Long publisherId) {
               
        String title = book.getTitle();
        if (title != null && title.isEmpty()) title = null;

        Integer year = book.getYear();

        String isbn10 = book.getIsbn10();
        if (isbn10 != null && isbn10.isEmpty()) isbn10 = null;

        String isbn13 = book.getIsbn13();
        if (isbn13 != null && isbn13.isEmpty()) isbn13 = null;

        String shelfMark = book.getShelfMark();
        if (shelfMark != null && shelfMark.isEmpty()) shelfMark = null;

        return BookUpdateRequest.builder()
                            .title(title)
                            .year(year)
                            .isbn10(isbn10)
                            .isbn13(isbn13)
                            .shelfMark(shelfMark)
                            .authorIds(authorIds)
                            .editorIds(editorIds)
                            .publisherId(publisherId)
                            .build();
    }

    private FlatEntryDto mapFullDtoToFlatDto(FullEntryDto fullDto) {

        return FlatEntryDto.builder()
                    .bookId(fullDto.getBook().getId())
                    .bookTitle(fullDto.getBook().getTitle())
                    .bookYear(fullDto.getBook().getYear())
                    .shelfMark(fullDto.getBook().getShelfMark())
                    .authors(fullDto.getAuthors().stream()
                            .map(a -> a.getLastName() + ", " + a.getFirstNames())
                            .collect(Collectors.joining("; ")))
                    .editors(fullDto.getEditors().stream()
                            .map(e -> e.getLastName() + ", " + e.getFirstNames())
                            .collect(Collectors.joining("; ")))
                    .publisher(fullDto.getPublisher().toString())
                    .build();
    }

    @Override
    public void delete(long id) {
        bookRepository.delete(id);
    }
}
