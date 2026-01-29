package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.Person;

@Component
public class PersonRowMapper implements RowMapper<Person> {
    @Override
    public Person mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Person.builder()
                    .id(resultSet.getLong("id"))
                    .lastName(resultSet.getString("last_name"))
                    .firstNames(resultSet.getString("first_names"))
                    .build();
    }
}