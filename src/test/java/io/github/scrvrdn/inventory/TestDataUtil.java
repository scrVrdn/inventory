package io.github.scrvrdn.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.domain.Publisher;
import io.github.scrvrdn.inventory.dto.EntryDto;
import io.github.scrvrdn.inventory.dto.EntryRow;

public final class TestDataUtil {

    private TestDataUtil() {}

    public static Book createTestBook() {
        return Book.builder()
            .title("Poetry and Tales")    
            .year(1984)
            .isbn10("0940450186")
            .isbn13("9780940450189")
            .shelfMark("A:a:3:1")
            .build();
    }

    public static Book createTestBook2() {
        return Book.builder()
            .title("The Anatomy of Melancholy")
            .year(2023)
            .isbn10("0141192283")
            .isbn13("9780141192284")
            .shelfMark("F:a:2:3")
            .build();
    }

    public static Book createTestBook3() {
        return Book.builder()
            .title("Essays and Reviews")
            .year(1984)
            .isbn10("0940450194")
            .isbn13("9780940450196")
            .shelfMark("A:b:1:1")
            .build();
    }

    public static Person createTestPerson() {
        return Person.builder()
                .lastName("Poe")
                .firstNames("Edgar Allan")
                .build();
    }

    public static Person createTestPerson2() {
        return Person.builder()
                .lastName("Burton")
                .firstNames("Robert")
                .build();
    }
    
    public static Person createTestPerson3() {
        return Person.builder()
                .lastName("Quinn")
                .firstNames("Patrick F.")
                .build();
    }

    public static Person createTestPerson4() {
        return Person.builder()
                .lastName("Gowland")
                .firstNames("Angus")
                .build();
    }

    public static Publisher createTestPublisher() {
        return Publisher.builder()
            .name("Library of America")
            .location("New York")
            .build();
    }

    public static Publisher createTestPublisher2() {
        return Publisher.builder()
            .name("Penguin Classics")
            .location("London")
            .build();
    }

    public static Publisher createTestPublisher3() {
        return Publisher.builder()
            .name("Clarendon Press")
            .location("Oxford")
            .build();
    }

    public static EntryDto createTestEntry() {
        Book book = createTestBook();
        Person author = createTestPerson();
        Person editor = createTestPerson3();
        Publisher publisher = createTestPublisher();
        
        return EntryDto.builder()
                .book(book)
                .authors(new ArrayList<>(List.of(author)))
                .editors(new ArrayList<>(List.of(editor)))
                .publisher(publisher)
                .build();
    }

    public static EntryDto createTestEntry2() {
        Book book = createTestBook2();
        Person author = createTestPerson2();
        Person editor = createTestPerson4();
        Publisher publisher = createTestPublisher2();

        return EntryDto.builder()
                .book(book)
                .authors(new ArrayList<>(List.of(author)))
                .editors(new ArrayList<>(List.of(editor)))
                .publisher(publisher)
                .build();
    }

    public static EntryRow createEntryRowFromEntry(EntryDto entry) {
        return EntryRow.builder()
                    .bookId(entry.getBook().getId())
                    .bookTitle(entry.getBook().getTitle())
                    .bookYear(entry.getBook().getYear())
                    .shelfMark(entry.getBook().getShelfMark())
                    .authors(entry.getAuthors().stream()
                            .map(a -> a.getLastName() + ", " + a.getFirstNames())
                            .collect(Collectors.joining("; ")))
                    .editors(entry.getEditors().stream()
                            .map(e -> e.getLastName() + ", " + e.getFirstNames())
                            .collect(Collectors.joining("; ")))
                    .publisher(entry.getPublisher().getLocation() + ": " + entry.getPublisher().getName())
                    .build();
    }
}
