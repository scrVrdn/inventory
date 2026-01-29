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

import io.github.scrvrdn.inventory.dto.Publisher;
import io.github.scrvrdn.inventory.mappers.PublisherRowMapper;
import io.github.scrvrdn.inventory.repositories.PublisherRepository;

@Repository
public class PublisherRepositoryImpl implements PublisherRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PublisherRowMapper publisherRowMapper;
    
    public PublisherRepositoryImpl(final JdbcTemplate jdbcTemplate, final PublisherRowMapper publisherRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.publisherRowMapper = publisherRowMapper;
    }

    @Override
    public void create(Publisher publisher) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(createPreparedStatementCreator(publisher), keyHolder);

        Long id;
        if (affectedRows > 0) {
            id = keyHolder.getKey().longValue();
        } else {
            id = getExistingId(publisher);
        }
        
        publisher.setId(id);
    }

    private PreparedStatementCreator createPreparedStatementCreator(Publisher publisher) {
        String query = """
                INSERT OR IGNORE INTO "publishers" ("name", "location")
                VALUES (?, ?);
                """;

        return connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, publisher.getName());
            ps.setString(2, publisher.getLocation());
            return ps;
        };
    }

    private Long getExistingId(Publisher publisher) {
        String query = """
                SELECT "id" FROM "publishers"
                WHERE "name" = ?
                AND "location" = ?;
                """;

        return jdbcTemplate.queryForObject(
            query,
            Long.class,
            publisher.getName(),
            publisher.getLocation()
        );
    }

    @Override
    public Optional<Publisher> findById(long id) {
        String query = """
                SELECT * FROM "publishers" WHERE "id" = ?;
                """;
        List<Publisher> result = jdbcTemplate.query(query, publisherRowMapper,id);
        return result.stream().findFirst();
    }

    @Override
    public List<Publisher> findAll() {
        String query ="""
                SELECT * FROM "publishers";
                """;
        return jdbcTemplate.query(query, publisherRowMapper);
    }


    @Override
    public void update(Publisher publisher) {
        String query = """
                UPDATE "publishers"
                SET "name" = ?, "location" = ?
                WHERE "id" = ?;
                """;

        jdbcTemplate.update(
            query,
            publisher.getName(),
            publisher.getLocation(),
            publisher.getId()
        );
    }

    @Override
    public void delete(long id) {
        String query = """
                DELETE FROM "publishers" WHERE "id" = ?;
                """;
        
        jdbcTemplate.update(query, id);
    }

}
