package io.github.scrvrdn.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {

    private Long id;
    private String title;
    private Integer year;
    private String isbn10;
    private String isbn13;
    private String shelfMark;
}
