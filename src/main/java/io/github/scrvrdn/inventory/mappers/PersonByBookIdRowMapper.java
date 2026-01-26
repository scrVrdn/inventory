package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.core.RowMapper;

import io.github.scrvrdn.inventory.dto.Person;

public class PersonByBookIdRowMapper implements RowMapper<Entry<Long, Person>> {
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