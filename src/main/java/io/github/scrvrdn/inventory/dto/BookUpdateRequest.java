package io.github.scrvrdn.inventory.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookUpdateRequest {

    private String title;
    private Integer year;
    private String isbn10;
    private String isbn13;
    private String shelfMark;
    private List<Long> authorIds;
    private List<Long> editorIds;
    private Long publisherId;
}
