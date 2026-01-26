package io.github.scrvrdn.inventory.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.mappers.PersonByBookIdRowMapper;
import io.github.scrvrdn.inventory.mappers.PersonRowMapper;
import io.github.scrvrdn.inventory.repositories.BookPersonRepository;

@Repository
public class BookPersonRepositoryImpl implements BookPersonRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookPersonRepositoryImpl (final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void assignAuthorsToBook(long bookId, List<Long> authorIds) {
        String query = """
                INSERT INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, 'AUTHOR', ?)
                ON CONFLICT ("book_id", "person_id", "role") DO UPDATE
                SET "order_index" = "excluded"."order_index";
                """;
        jdbcTemplate.batchUpdate(
                    query,
                    new PersonBatchSetter(bookId, authorIds)
        );
    }

    @Override
    public List<Person> findAuthorsByBookId(long bookId) {
        String query = """
                SELECT * FROM "book_person"
                JOIN "persons" ON "book_person"."person_id" = "persons"."id"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;

        return jdbcTemplate.query(query, new PersonRowMapper(), bookId);
    }

    @Override
    public List<Long> findAuthorIdsByBookId(long bookId) {
        String query = """
                SELECT "person_id" FROM "book_person"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;
        return jdbcTemplate.queryForList(query, Long.class, bookId);
    }

    @Override
    public Map<Long, List<Person>> findAllAuthorsGroupedByBookId() {        
        String query = """
                SELECT * FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'AUTHOR'
                ORDER BY "order_index";
                """;

        List<Entry<Long, Person>> result = jdbcTemplate.query(query, new PersonByBookIdRowMapper());

        return groupEntries(result.stream());
    }

    

    @Override
    public void removeAuthorsFromBook(long bookId, List<Long> authorIds) {
        String query = """
                DELETE FROM "book_person"
                WHERE "role" = 'AUTHOR'
                AND "book_id" = ?
                AND "person_id" = ?;
                """;

        jdbcTemplate.batchUpdate(query,
                                authorIds,
                                authorIds.size(),
                                (ps, id) -> {
                                    ps.setLong(1, bookId);
                                    ps.setLong(2, id);
                                });
    }

    @Override
    public void assignEditorsToBook(long bookId, List<Long> editorIds) {
        String query = """
                INSERT INTO "book_person" ("book_id", "person_id", "role", "order_index")
                VALUES (?, ?, 'EDITOR', ?)
                ON CONFLICT ("book_id", "person_id", "role") DO UPDATE
                SET "order_index" = "excluded"."order_index";
                """;
        
        jdbcTemplate.batchUpdate(query, new PersonBatchSetter(bookId, editorIds));
    }

    @Override
    public List<Person> findEditorsByBookId(long bookId) {
        String query = """
                SELECT * FROM "book_person"
                JOIN "persons" ON "book_person"."person_id" = "persons"."id"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;
        
        return jdbcTemplate.query(query, new PersonRowMapper(), bookId);
    }

    @Override
    public List<Long> findEditorIdsByBookId(long bookId) {
        String query = """
                SELECT "person_id" FROM "book_person"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                ORDER BY "order_index";
                """;

        return jdbcTemplate.queryForList(query, Long.class, bookId);
    }

    @Override
    public Map<Long, List<Person>> findAllEditorsGroupedByBookId() {
        String query = """
                SELECT * FROM "book_person"
                JOIN "persons" ON "book_person"."person_id" = "persons"."id"
                WHERE "role" = 'EDITOR'
                ORDER BY "order_index";
                """;

        List<Entry<Long, Person>> result = jdbcTemplate.query(query, new PersonByBookIdRowMapper());
        return groupEntries(result.stream());
    }

    @Override
    public void removeEditorsFromBook(long bookId, List<Long> editorIds) {
        String query = """
                DELETE FROM "book_person"
                WHERE "role" = 'EDITOR'
                AND "book_id" = ?
                AND "person_id" = ?;
                """;

        jdbcTemplate.batchUpdate(
                            query,
                            editorIds,
                            editorIds.size(),
                            (ps, id) -> {
                                ps.setLong(1, bookId);
                                ps.setLong(2, id);
        });
    }


    private Map<Long, List<Person>> groupEntries(Stream<Entry<Long, Person>> entries) {
        return entries.collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
    }

    private class PersonBatchSetter implements BatchPreparedStatementSetter {

        private final long bookId;
        private final List<Long> ids;

        PersonBatchSetter(long bookId, List<Long> ids) {
            this.bookId = bookId;
            this.ids = ids;
        }
        
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setLong(1, bookId);
            long id = ids.get(i);
            ps.setLong(2, id);
            ps.setInt(3, i);
        }

        @Override
        public int getBatchSize() {
            return ids.size();
        }
    }




}
