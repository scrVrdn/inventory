package io.github.scrvrdn.inventory.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Default
    private List<Person> authors = new ArrayList<>();

    @Default
    private List<Person> editors = new ArrayList<>();
    
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
