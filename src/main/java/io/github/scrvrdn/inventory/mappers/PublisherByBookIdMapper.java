package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.Publisher;

@Component
public class PublisherByBookIdMapper implements RowMapper<Entry<Long, Publisher>> {
    @Override
    public Entry<Long, Publisher> mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Map.entry(
            rs.getLong("book_id"),
            Publisher.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .location(rs.getString("location"))
                .build()
        );
    }
}