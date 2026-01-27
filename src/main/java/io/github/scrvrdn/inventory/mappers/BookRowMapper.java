package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.Book;

@Component
public class BookRowMapper implements RowMapper<Book> {
    @Override
    public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
        int yearInt = rs.getInt("year");
        Integer year = rs.wasNull() ? null : yearInt;

        return Book.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .year(year)
            .isbn10(rs.getString("isbn10"))
            .isbn13(rs.getString("isbn13"))
            .shelfMark(rs.getString("shelf_mark"))
            .build();
    }
}