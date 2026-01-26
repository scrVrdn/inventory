package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;

public class FlatEntryDtoRowMapper implements RowMapper<FlatEntryDto> {
    @Override
    public FlatEntryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        int yearInt = rs.getInt("year");
        Integer year = rs.wasNull() ? null : yearInt;

        return FlatEntryDto.builder()
            .bookId(rs.getLong("id"))
            .bookTitle(rs.getString("title"))
            .bookYear(year)
            .shelfMark(rs.getString("shelf_mark"))
            .authors(rs.getString("authors"))
            .editors(rs.getString("editors"))
            .publisher(rs.getString("publisher"))
            .build();
    }
}