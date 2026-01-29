package io.github.scrvrdn.inventory.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.mappers.PersonRowMapper;
import io.github.scrvrdn.inventory.repositories.PersonRepository;


@Repository
public class PersonRepositoryImpl implements PersonRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PersonRowMapper personRowMapper;

    public PersonRepositoryImpl(final JdbcTemplate jdbcTemplate, final PersonRowMapper personRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.personRowMapper = personRowMapper;
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
        List<Person> result = jdbcTemplate.query(query, personRowMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public List<Person> findAll() {
        String query = """
                SELECT * FROM "persons";
                """;
        return jdbcTemplate.query(
                query,
                personRowMapper
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
}
