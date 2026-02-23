package io.github.scrvrdn.inventory.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.exceptions.BookNotFoundException;
import io.github.scrvrdn.inventory.exceptions.UniqueConstraintViolationException;
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
            try {
                id = getExistingId(book);

            } catch (DataAccessException e) {
                throw new RuntimeException("Failed to create new Book.");
            }            
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
                OR "isbn13" = ?;
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

        try {
            jdbcTemplate.update(
                query,
                book.getTitle(),
                book.getYear(),
                book.getIsbn10(),
                book.getIsbn13(),
                book.getShelfMark(),
                book.getId()
            );

        } catch (DataAccessException e) {
            handleSQLException(e);
        }
        
    }

    private void handleSQLException(DataAccessException e) {
        if (e instanceof UncategorizedSQLException uncatEx) {
            SQLException sqlEx = (SQLException) uncatEx.getCause();
            UniqueColumn col = resolveUniqueColumn(sqlEx);

            String userMessage;
            if (col == UniqueColumn.BOOK_ISBN10) {
                userMessage = "A book with this ISBN-10 already exists.";

            } else if (col == UniqueColumn.BOOK_ISBN13) {
                userMessage = "A book with this ISBN-13 already exists.";

            } else {
                userMessage = "The data you entered violates a unique constraint.";
            }

            throw new UniqueConstraintViolationException(userMessage, e);
        }

        throw e;
    }

    private UniqueColumn resolveUniqueColumn(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return null;

        if (msg.contains("UNIQUE constraint failed: books.isbn10")) {
            return UniqueColumn.BOOK_ISBN10;
        }

        if (msg.contains("UNIQUE constraint failed: books.isbn13")) {
            return UniqueColumn.BOOK_ISBN13;
        }

        return null;
    }

    @Override
    public void delete(long id) {
        String query = """
                DELETE FROM "books" WHERE "id" = ?;
                """;

        int rowsAffected = jdbcTemplate.update(query, id);
        if (rowsAffected == 0) {
            throw new BookNotFoundException(id);
        }
    }

    @Override
    public int numberOfRows() {
        String query = """
                SELECT "total_rows" FROM "row_counters" WHERE "table_name" = 'books';
                """;
        return jdbcTemplate.queryForObject(query, int.class);
    }

    public enum UniqueColumn {
        BOOK_ISBN10,
        BOOK_ISBN13
    }

}
