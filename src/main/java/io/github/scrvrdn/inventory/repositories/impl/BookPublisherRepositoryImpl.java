package io.github.scrvrdn.inventory.repositories.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.dto.Publisher;
import io.github.scrvrdn.inventory.mappers.PublisherByBookIdMapper;
import io.github.scrvrdn.inventory.mappers.PublisherRowMapper;
import io.github.scrvrdn.inventory.repositories.BookPublisherRepository;

@Repository
public class BookPublisherRepositoryImpl implements BookPublisherRepository {
    private final JdbcTemplate jdbcTemplate;

    public BookPublisherRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

     @Override
    public void assignPublisherToBook(long bookId, long publisherId) {
        String query = """
                INSERT INTO "published" ("book_id", "publisher_id")
                VALUES (?, ?)
                ON CONFLICT ("book_id") DO UPDATE
                SET "publisher_id" = "excluded"."publisher_id";
                """;
                
        jdbcTemplate.update(
            query,
            bookId,
            publisherId
        );
    }

    @Override
    public void removePublisherFromBook(long bookId, long publisherId) {
        String query = """
                DELETE FROM "published"
                WHERE "book_id" = ? AND "publisher_id" = ?;
                """;

        jdbcTemplate.update(
            query,
            bookId,
            publisherId);
    }

    @Override
    public Optional<Publisher> findPublisherByBookId(long bookId) {
        String query = """
                SELECT "publishers"."id", "name", "location" FROM "publishers"
                JOIN "published" ON "publishers"."id" = "published"."publisher_id"
                WHERE "published"."book_id" = ?;
                """;
        List<Publisher> result = jdbcTemplate.query(query, new PublisherRowMapper(), bookId);
        return result.stream().findFirst();
    }


    @Override
    public Map<Long, Publisher> findPublishersGroupedByBookId() {
        String query = """
                SELECT * FROM "publishers"
                JOIN "published" ON "publishers"."id" = "published"."publisher_id";
                """;

        List<Entry<Long, Publisher>> result = jdbcTemplate.query(query, new PublisherByBookIdMapper());
        return result.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
