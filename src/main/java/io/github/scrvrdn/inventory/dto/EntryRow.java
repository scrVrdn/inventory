package io.github.scrvrdn.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntryRow {
    private Long bookId;
    private String bookTitle;
    private int bookYear;
    private String shelfMark;
    private String authors;
    private String editors;
    private String publisher;    
}
