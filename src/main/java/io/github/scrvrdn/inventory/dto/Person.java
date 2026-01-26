package io.github.scrvrdn.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Person {

    private Long id;
    private String lastName;
    private String firstNames;

    @Override
    public String toString() {
        if (lastName == null && firstNames == null) return "";
        if (lastName == null) return firstNames;
        if (firstNames == null) return lastName;

        return lastName + ", " + firstNames;
    }
}
