package io.github.scrvrdn.inventory.dto;

public record FlatEntryDto(
            Long bookId,
            String bookTitle,
            Integer bookYear,
            String shelfMark,
            String authors,
            String editors,
            String publisher
        ) {}
