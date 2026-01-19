package io.github.scrvrdn.inventory.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.domain.Publisher;
import io.github.scrvrdn.inventory.repositories.BookRepository;
import io.github.scrvrdn.inventory.repositories.impl.PersonRepositoryImpl.PersonRowMapper;
import io.github.scrvrdn.inventory.repositories.impl.PublisherRepositoryImpl.PublisherRowMapper;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Book book) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(createPreparedStatementCreator(book), keyHolder);

        Long id;
        if (affectedRows > 0) {
            id = keyHolder.getKey().longValue();
        } else {
            id = getExistingId(book);
        }
        
        book.setId(id);
    }

    private PreparedStatementCreator createPreparedStatementCreator(Book book) {
        String query = """
                INSERT OR IGNORE INTO "books" ("title", "year", "isbn10", "isbn13", "shelf_mark")
                VALUES (?, ?, ?, ?, ?);
                """;

        return connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getYear());
            ps.setString(3, book.getIsbn10());
            ps.setString(4, book.getIsbn13());
            ps.setString(5, book.getShelfMark());
            return ps;
        };
    }

    private Long getExistingId(Book book) {
        String query = """
                SELECT "id" FROM "books"
                WHERE "isbn10" = ?
                AND "isbn13" = ?;
                """;
        
        return jdbcTemplate.queryForObject(
            query,
            Long.class,
            book.getIsbn10(),
            book.getIsbn13()
        );
    }

    @Override
    public Optional<Book> findById(long id) {
        String query = """
                SELECT * FROM "books" WHERE "id" = ?;
                """;
        List<Book> result = jdbcTemplate.query(query, new BookRowMapper(), id);        
        return result.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        String query = """
                SELECT * FROM "books";
                """;
        return jdbcTemplate.query(query, new BookRowMapper());
    }

    @Override
    public void update(Book book) {
        String query = """
                UPDATE "books"
                SET "title" = ?, "year" = ?, "isbn10" = ?, "isbn13" = ?, "shelf_mark" = ?
                WHERE "id" = ?;
                """;

        jdbcTemplate.update(
            query,
            book.getTitle(),
            book.getYear(),
            book.getIsbn10(),
            book.getIsbn13(),
            book.getShelfMark(),
            book.getId()
        );
    }

    @Override
    public void delete(long id) {
        String query = """
                DELETE FROM "books" WHERE "id" = ?;
                """;
        jdbcTemplate.update(query, id);
    }

    @Override
    public void assignToAuthor(Book book, Person author) {
        assignToAuthor(book, List.of(author));
    }

    @Override
    public void assignToAuthor(Book book, List<Person> authors) {
        String query = """
                INSERT OR IGNORE INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, ?, ?);
                """;
        int i = 0;
        for (Person a : authors) {
            jdbcTemplate.update(
                query,
                book.getId(),
                a.getId(),
                "AUTHOR",
                i++
            );
        }
    }

    @Override
    public List<Person> findAuthors(Book book) {
        String query = """
                SELECT "persons"."id", "last_name", "first_names" FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;
        return jdbcTemplate.query(
            query,
            new PersonRowMapper(),
            book.getId()
        );
    }

    @Override
    public void assignToEditor(Book book, Person editor) {
        assignToEditor(book, List.of(editor));
    }

    @Override
    public void assignToEditor(Book book, List<Person> editors) {
        String query = """
                INSERT OR IGNORE INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, ?, ?);
                """;
        int i = 0;
        for (Person e : editors) {
            jdbcTemplate.update(
                query,
                book.getId(),
                e.getId(),
                "EDITOR",
                i++        
            );
        }
    }

    @Override
    public List<Person> findEditors(Book book) {
        String query = """
                SELECT "persons"."id", "last_name", "first_names" FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;
        return jdbcTemplate.query(
            query,
            new PersonRowMapper(),
            book.getId()
        );
    }

    @Override
    public void assignToPublisher(Book book, Publisher publisher) {
        String query = """
                INSERT INTO "published" ("book_id", "publisher_id")
                VALUES (?, ?);
                """;
                
        jdbcTemplate.update(
            query,
            book.getId(),
            publisher.getId()
        );
    }

    @Override
    public Optional<Publisher> findPublisher(Book book) {
        String query = """
                SELECT "publishers"."id", "name", "location" FROM "publishers"
                JOIN "published" ON "publishers"."id" = "published"."publisher_id"
                WHERE "published"."book_id" = ?;
                """;
        List<Publisher> result = jdbcTemplate.query(query, new PublisherRowMapper(), book.getId());
        return result.stream().findFirst();
    }


    public static class BookRowMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Book.builder()
                .id(resultSet.getLong("id"))
                .title(resultSet.getString("title"))
                .year(resultSet.getObject("year", Integer.class))
                .isbn10(resultSet.getString("isbn10"))
                .isbn13(resultSet.getString("isbn13"))
                .shelfMark(resultSet.getString("shelf_mark"))
                .build();
        }
    }
}
