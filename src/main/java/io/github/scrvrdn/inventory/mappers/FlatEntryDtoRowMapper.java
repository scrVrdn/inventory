package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;

@Component
public class FlatEntryDtoRowMapper implements RowMapper<FlatEntryDto> {
    @Override
    public FlatEntryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        int yearInt = rs.getInt("year");
        Integer year = rs.wasNull() ? null : yearInt;
        
        return new FlatEntryDto(
                rs.getLong("id"),
                rs.getString("title"),
                year,
                rs.getString("shelf_mark"),
                rs.getString("authors"),
                rs.getString("editors"),
                rs.getString("publisher")
            );
    }
}