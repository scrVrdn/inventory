package io.github.scrvrdn.inventory.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.mappers.BookRowMapper;
import io.github.scrvrdn.inventory.repositories.BookRepository;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final JdbcTemplate jdbcTemplate;
    private final BookRowMapper bookRowMapper;

    public BookRepositoryImpl(final JdbcTemplate jdbcTemplate, final BookRowMapper bookRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookRowMapper = bookRowMapper;
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
            ps.setObject(2, book.getYear(), Types.INTEGER);
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
        List<Book> result = jdbcTemplate.query(query, bookRowMapper, id);        
        return result.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        String query = """
                SELECT * FROM "books";
                """;
        return jdbcTemplate.query(query, bookRowMapper);
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
}
