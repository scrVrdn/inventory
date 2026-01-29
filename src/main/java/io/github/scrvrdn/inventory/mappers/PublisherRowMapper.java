package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.Publisher;

@Component
public class PublisherRowMapper implements RowMapper<Publisher> {
    @Override
    public Publisher mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Publisher.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .location(rs.getString("location"))
            .build();
    }
}