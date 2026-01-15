package io.github.scrvrdn.inventory.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.repositories.PersonRepository;


@Repository
public class PersonRepositoryImpl implements PersonRepository {

    private final JdbcTemplate jdbcTemplate;

    public PersonRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Person person) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(createPreparedStatementCreator(person), keyHolder);
        Long id;
        if (rowsAffected > 0) {
            id = keyHolder.getKey().longValue();
        } else {
            id = getExistingId(person);
        }
        
        person.setId(id);
    }

    private PreparedStatementCreator createPreparedStatementCreator(Person person) {
        String query = """
                INSERT OR IGNORE INTO "persons" ("last_name", "first_names")
                VALUES (?, ?);
                """;

        return connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, person.getLastName());
            ps.setString(2, person.getFirstNames());
            return ps;
        };
    }

    private Long getExistingId(Person person) {
        String query = """
                SELECT "id" FROM "persons"
                WHERE "last_name" = ?
                AND "first_names" = ?;
                """;

        return jdbcTemplate.queryForObject(query,
            Long.class,
            person.getLastName(),
            person.getFirstNames()
        );
    }

    @Override
    public void createAll(List<Person> persons) {
        for (Person p : persons) {
            create(p);
        }
    }

    @Override
    public Optional<Person> findById(long id) {
        String query = """
                SELECT * FROM "persons" WHERE "id" = ?;
                """;
        List<Person> result = jdbcTemplate.query(query, new PersonRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public Map<Long, List<Person>> findAuthorsGroupedByBookId() {        
        String query = """
                SELECT * FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'AUTHOR'
                ORDER BY "order_index";
                """;

        List<Entry<Long, Person>> result = jdbcTemplate.query(query, new PersonByBookIdRowMapper());

        return groupEntries(result.stream());
    }

    private Map<Long, List<Person>> groupEntries(Stream<Entry<Long, Person>> entries) {
        return entries.collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
    }

    @Override
    public Map<Long, List<Person>> findEditorsGroupedByBookId() {
        String query = """
                SELECT * FROM "persons"
                JOIN "book_person" ON "persons"."id" = "book_person"."person_id"
                WHERE "role" = 'EDITOR'
                ORDER BY "order_index";
                """;
        List<Entry<Long, Person>> result = jdbcTemplate.query(query, new PersonByBookIdRowMapper());

        return groupEntries(result.stream());
    }

    @Override
    public List<Person> findAll() {
        String query = """
                SELECT * FROM "persons";
                """;
        return jdbcTemplate.query(
                query,
                new PersonRowMapper()
            );
    }

    @Override
    public void update(Person person) {
        String query = """
                UPDATE "persons"
                SET "last_name" = ?, "first_names" = ?
                WHERE "id" = ?;
                """;

        jdbcTemplate.update(
            query,
            person.getLastName(),
            person.getFirstNames(),
            person.getId()
        );
    }

    @Override
    public void delete(long id) {
        String query = """
                DELETE FROM "persons" WHERE "id" = ?;
                """;
        jdbcTemplate.update(query, id);
    }

    public static class PersonRowMapper implements RowMapper<Person> {
        @Override
        public Person mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Person.builder()
                        .id(resultSet.getLong("id"))
                        .lastName(resultSet.getString("last_name"))
                        .firstNames(resultSet.getString("first_names"))
                        .build();
        }
    }

    public static class PersonByBookIdRowMapper implements RowMapper<Entry<Long, Person>> {
        @Override
        public Entry<Long, Person> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Map.entry(resultSet.getLong(
                "book_id"),
                Person.builder()
                    .id(resultSet.getLong("id"))
                    .lastName(resultSet.getString("last_name"))
                    .firstNames(resultSet.getString("first_names"))
                    .build()
            );
        }
    }
}
