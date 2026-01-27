package io.github.scrvrdn.inventory.dto;

import java.util.List;

public record BookUpdateRequest(
                String title,
                Integer year,
                String isbn10,
                String isbn13,
                String shelfMark,
                List<Long> authorIds,
                List<Long> editorIds,
                Long publisherId
            ) {}
