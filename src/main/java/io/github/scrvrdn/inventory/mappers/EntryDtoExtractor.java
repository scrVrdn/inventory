package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.dto.Publisher;


@Component
public class EntryDtoExtractor implements ResultSetExtractor<FullEntryDto> {
    @Override
    public FullEntryDto extractData(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;

        int yearInt = rs.getInt("year");
        Integer year = rs.wasNull() ? null : yearInt;

        Book book = Book.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .year(year)
                .isbn10(rs.getString("isbn10"))
                .isbn13(rs.getString("isbn13"))
                .shelfMark(rs.getString("shelf_mark"))
                .build();
        
        long publisherId = rs.getLong("publisher_id");
        Publisher publisher = null;
        if (!rs.wasNull()) {
            publisher = Publisher.builder()
                .id(publisherId)
                .name(rs.getString("publisher_name"))
                .location(rs.getString("publisher_location"))
                .build();
        }

        

        List<Person> authors = new ArrayList<>();
        List<Person> editors = new ArrayList<>();

        do {
            Long authorId = rs.getLong("author_id");
            if (!rs.wasNull()) {
                authors.add(Person.builder()
                        .id(authorId)    
                        .lastName(rs.getString("author_last_name"))
                        .firstNames(rs.getString("author_first_names"))
                        .build()
                );
            }

            Long editorId = rs.getLong("editor_id");
            if (!rs.wasNull()) {
                editors.add(Person.builder()
                        .id(editorId)
                        .lastName(rs.getString("editor_last_name"))
                        .firstNames(rs.getString("editor_first_names"))
                        .build()
                );
            }

        } while (rs.next());

        return FullEntryDto.builder()
                    .book(book)
                    .authors(authors)
                    .editors(editors)
                    .publisher(publisher)
                    .build();
    }
}