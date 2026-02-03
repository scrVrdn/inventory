package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.dto.Publisher;

@Component
public class EntryDtoListExtractor implements ResultSetExtractor<List<FullEntryDto>> {

    @Override
    public List<FullEntryDto> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, FullEntryDto> entriesByBookId = new LinkedHashMap<>();

        while (rs.next()) {
            long bookId = rs.getLong("id");

            FullEntryDto entryDto = entriesByBookId.computeIfAbsent(bookId, id -> {
                try {
                    return extractDto(rs, id);
                } catch (SQLException e) {
                    
                    e.printStackTrace();
                }

                return null;
            });
           

            Long authorId = rs.getLong("author_id");
            if (!rs.wasNull()) {
                entryDto.getAuthors().add(Person.builder()
                                                .id(authorId)
                                                .lastName(rs.getString("author_last_name"))
                                                .firstNames(rs.getString("author_first_names"))
                                                .build()
                );
            }

            Long editorId = rs.getLong("editor_id");
            if (!rs.wasNull()) {
                entryDto.getEditors().add(Person.builder()
                                                .id(editorId)
                                                .lastName(rs.getString("editor_last_name"))
                                                .firstNames(rs.getString("editor_first_names"))
                                                .build()    
                );
            }
        }

        return new ArrayList<>(entriesByBookId.values());
    }

    private FullEntryDto extractDto(ResultSet rs, Long id) throws SQLException {
        
        return FullEntryDto.builder()
                .book(getBook(rs, id))
                .authors(new ArrayList<>())
                .editors(new ArrayList<>())
                .publisher(getPublisher(rs))
                .build();
    }

    private Book getBook(ResultSet rs, Long id) throws SQLException {
        int yearInt = rs.getInt("year");
        Integer year = rs.wasNull() ? null : yearInt;
        
        return Book.builder()
                        .id(id)
                        .title(rs.getString("title"))
                        .year(year)
                        .isbn10(rs.getString("isbn10"))
                        .isbn13(rs.getString("isbn13"))
                        .shelfMark(rs.getString("shelf_mark"))
                        .build();
                
    }

    private Publisher getPublisher(ResultSet rs) throws SQLException {
        long publisherId = rs.getLong("publisher_id");

        if (!rs.wasNull()) {
            return Publisher.builder()
                .id(publisherId)
                .name(rs.getString("publisher_name"))
                .location(rs.getString("publisher_location"))
                .build();
        }

        return null;
    }

}
