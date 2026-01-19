package io.github.scrvrdn.inventory.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.domain.Publisher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FullEntryDto {
    private Book book;
    private List<Person> authors;
    private List<Person> editors;
    private Publisher publisher;

    @Default
    private Set<String> updatedFields = new HashSet<>();

    public void markUpdated(String field) {
        updatedFields.add(field);
    }

    public boolean isUpdated(String field) {
        return updatedFields.contains(field);
    }
}
