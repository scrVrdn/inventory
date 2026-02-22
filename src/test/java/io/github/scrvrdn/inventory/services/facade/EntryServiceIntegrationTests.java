package io.github.scrvrdn.inventory.services.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.dto.PageRequest;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.dto.Publisher;
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class EntryServiceIntegrationTests {

    private final JdbcTemplate jdbcTemplate;
    private final EntryService underTest;

    @Autowired
    public EntryServiceIntegrationTests(JdbcTemplate jdbcTemplate, EntryService underTest) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "book_person", "published");
    }

    @Test
    public void testThatFullEntryDtoCanBeCreatedAndRetrieved() {
        FlatEntryDto emptyEntry = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry = TestDataUtil.createTestEntry();
        entry.getBook().setId(emptyEntry.bookId());

        entry.getAuthors().add(TestDataUtil.createTestPerson3());
        entry.getAuthors().add(TestDataUtil.createTestPerson4());
        entry.getEditors().add(TestDataUtil.createTestPerson4());
        underTest.update(entry);
        
        Optional<FullEntryDto> result = underTest.findById(entry.getBook().getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entry);
    }

    @Test
    public void testThatEmptyFullEntryDtoCanBeRetrieved() {
        FlatEntryDto dto = underTest.createEmptyEntry().orElseThrow();
        long bookId = dto.bookId();
        FullEntryDto expected = FullEntryDto.builder()
                                            .book(Book.builder().id(bookId).build())
                                            .build();

        Optional<FullEntryDto> result = underTest.findById(bookId);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    public void testThatFindsAllFullEntryDtos() {
        FlatEntryDto emptyEntry1 = underTest.createEmptyEntry().orElseThrow();        
        FullEntryDto entry1 = TestDataUtil.createTestEntry();
        entry1.getBook().setId(emptyEntry1.bookId());

        FlatEntryDto emptyEntry2 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry2 = TestDataUtil.createTestEntry2();
        entry2.getBook().setId(emptyEntry2.bookId());

        entry1.getAuthors().add(TestDataUtil.createTestPerson3());
        entry2.getAuthors().add(TestDataUtil.createTestPerson());
        entry1.getAuthors().add(TestDataUtil.createTestPerson2());
        entry1.getAuthors().add(TestDataUtil.createTestPerson4());

        underTest.update(entry1);
        underTest.update(entry2);

        List<FullEntryDto> result = underTest.findAll();
        assertThat(result)
            .hasSize(2)
            .containsExactly(entry1, entry2);
    }

    @Test
    public void testThatGetsFlatEntryDtoByBookId() {
        FlatEntryDto emptyEntry = underTest.createEmptyEntry().orElseThrow();        
        FullEntryDto entry = TestDataUtil.createTestEntry();
        entry.getBook().setId(emptyEntry.bookId());

        entry.getAuthors().add(TestDataUtil.createTestPerson3());
        entry.getEditors().add(TestDataUtil.createTestPerson4());

        underTest.update(entry);
        FlatEntryDto entryRow = TestDataUtil.createEntryRowFromEntry(entry);

        Optional<FlatEntryDto> result = underTest.getFlatEntryDtoByBookId(entry.getBook().getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entryRow);
    }

    @Test
    public void testThatGetsEmptyFlatEntryDto() {
        FlatEntryDto dto = underTest.createEmptyEntry().orElseThrow();

        Optional<FlatEntryDto> result = underTest.getFlatEntryDtoByBookId(dto.bookId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(dto);
    }

    @Test
    public void testThatGetsNextFlatEntryDtoAfterBookId() {
        FlatEntryDto dto1 = underTest.createEmptyEntry().orElseThrow();
        FlatEntryDto dto2 = underTest.createEmptyEntry().orElseThrow();
        FlatEntryDto dto3 = underTest.createEmptyEntry().orElseThrow();

        Optional<FlatEntryDto> result = underTest.getNextFlatEntryDtoAfterBookId(dto1.bookId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(dto2);

        underTest.delete(dto2.bookId());

        Optional<FlatEntryDto> resultAfterDeletion = underTest.getNextFlatEntryDtoAfterBookId(dto1.bookId());
        assertThat(resultAfterDeletion).isPresent();
        assertThat(resultAfterDeletion.get()).isEqualTo(dto3);
    }

    @Test
    public void testThatGetsCorrectFlatEntryDtosForAPage() {
        FlatEntryDto emptyEntry1 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry1 = TestDataUtil.createTestEntry();
        entry1.getBook().setId(emptyEntry1.bookId());

        FlatEntryDto emptyEntry2 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry2 = TestDataUtil.createTestEntry2();
        entry2.getBook().setId(emptyEntry2.bookId());

        List<FlatEntryDto> result = underTest.getFlatEntryDtos(1, 1);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().bookId()).isEqualTo(entry2.getBook().getId());
    }

    @Test
    public void testThatGetsSortedEntriesForPage() {
        List<FullEntryDto> entries = prepareEntries();

        List<FlatEntryDto> rowEntries = entries.stream().map(e -> TestDataUtil.createEntryRowFromEntry(e)).toList();
        testSortByDefault(rowEntries);
        testSortByBookId(rowEntries);
        testSortByTitle(rowEntries);
        testSortByYear(rowEntries);
        testSortByShelfMark(rowEntries);
        testSortByAuthor(rowEntries);
        testSortByEditor(rowEntries);
    }

    private List<FullEntryDto> prepareEntries() {
        Book book1 = Book.builder()
                    .title("Z")
                    .year(2000)
                    .isbn10("1")
                    .isbn13("2")
                    .shelfMark("a")
                    .build();

        Book book2 = Book.builder()
                    .title("Y")
                    .year(1999)
                    .isbn10("2")
                    .isbn13("4")
                    .shelfMark("b")
                    .build();

        Book book3 = Book.builder()
                    .title("X")
                    .year(2001)
                    .isbn10("3")
                    .isbn13("6")
                    .shelfMark("c")
                    .build();

        Book book4 = Book.builder()
                    .title("W")
                    .year(1997)
                    .isbn10("4")
                    .isbn13("8")
                    .shelfMark("b")
                    .build();
        Book book5 = Book.builder()
                    .title("Yz")
                    .year(1980)
                    .isbn10("6")
                    .isbn13("11")
                    .shelfMark("d")
                    .build();

        Person person1 = Person.builder()
                        .lastName("Ac")
                        .firstNames("Cd")
                        .build();
        Person person2 = Person.builder()
                        .lastName("Ba")
                        .firstNames("A")
                        .build();
        Person person3 = Person.builder()
                        .lastName("Ba")
                        .firstNames("B")
                        .build();
        Person person4 = Person.builder()
                        .lastName("Cc")
                        .firstNames("B")
                        .build();

        Person person5 = Person.builder()
                        .lastName("C")
                        .firstNames("A")
                        .build();

        Publisher publisher1 = TestDataUtil.createTestPublisher();
        Publisher publisher2 = TestDataUtil.createTestPublisher2();
        Publisher publisher3 = TestDataUtil.createTestPublisher3();

        List<FullEntryDto> entries = new ArrayList<>();

        FlatEntryDto entry1 = underTest.createEmptyEntry().orElseThrow();
        book1.setId(entry1.bookId());
        FullEntryDto fullEntry1 = createFullEntry(book1, List.of(person1, person3, person5), List.of(person2, person3, person1), publisher3);
        entries.add(fullEntry1);

        FlatEntryDto entry2 = underTest.createEmptyEntry().orElseThrow();
        book2.setId(entry2.bookId());
        FullEntryDto fullEntry2 = createFullEntry(book2, List.of(person3, person2, person4), List.of(person1, person4, person3), publisher1);
        entries.add(fullEntry2);

        FlatEntryDto entry3 = underTest.createEmptyEntry().orElseThrow();
        book3.setId(entry3.bookId());
        FullEntryDto fullEntry3 = createFullEntry(book3, List.of(person4, person3, person1, person2), List.of(person5, person3, person1), publisher2);
        entries.add(fullEntry3);

        FlatEntryDto entry4 = underTest.createEmptyEntry().orElseThrow();
        book4.setId(entry4.bookId());
        FullEntryDto fullEntry4 = createFullEntry(book4, List.of(person1, person3, person5, person2), List.of(person4, person1, person2), publisher2);
        entries.add(fullEntry4);

        FlatEntryDto entry5 = underTest.createEmptyEntry().orElseThrow();
        book5.setId(entry5.bookId());
        FullEntryDto fullEntry5 = createFullEntry(book5, List.of(person3, person2), List.of(person4, person1), publisher1);
        entries.add(fullEntry5);

        return entries;
    }

    private FullEntryDto createFullEntry(Book book, List<Person> authors, List<Person> editors, Publisher publisher) {
         FullEntryDto fullEntry = FullEntryDto.builder()
                        .book(book)
                        .authors(authors)
                        .editors(editors)
                        .publisher(publisher)
                        .build();
        underTest.update(fullEntry);
        return fullEntry;
    }

    private void testSortByDefault(List<FlatEntryDto> entries) {
        PageRequest sortByDefault = new PageRequest(0, 10, null, null, null, true);
        PageRequest sortByDefaultDesc = new PageRequest(0, 10, null, null, "DESC", true);
        
        List<FlatEntryDto> expectedAsc = entries.stream().sorted((a, b) -> {
                if (a.bookTitle().equals(b.bookTitle())) return a.bookId().compareTo(b.bookId());
                return a.bookTitle().compareTo(b.bookTitle());
                }).toList();
        List<FlatEntryDto> expectedDesc = entries.stream().sorted((a, b) -> {
                if (a.bookTitle().equals(b.bookTitle())) return b.bookId().compareTo(a.bookId());
                return b.bookTitle().compareTo(a.bookTitle());
            }).toList();

        Page resultAsc = underTest.getPage(sortByDefault);
        Page resultDesc = underTest.getPage(sortByDefaultDesc);
        
        assertThat(resultAsc.entries()).isEqualTo(expectedAsc);
        assertThat(resultDesc.entries()).isEqualTo(expectedDesc);
    }

    private void testSortByBookId(List<FlatEntryDto> entries) {
        PageRequest sortByBookIdAsc = new PageRequest(0, 10, null, "b.\"id\"", "ASC", true);
        PageRequest sortByBookIdDesc = new PageRequest(0, 10, null, "b.\"id\"", "DESC", true);

        List<FlatEntryDto> expectedAsc = entries.stream().sorted((a, b) -> a.bookId().compareTo(b.bookId())).toList();
        List<FlatEntryDto> expectedDesc = entries.stream().sorted((a, b) -> b.bookId().compareTo(a.bookId())).toList();

        Page resultAsc = underTest.getPage(sortByBookIdAsc);
        Page resultDesc = underTest.getPage(sortByBookIdDesc);

        assertThat(resultAsc.entries()).isEqualTo(expectedAsc);
        assertThat(resultDesc.entries()).isEqualTo(expectedDesc);
    }

    private void testSortByTitle(List<FlatEntryDto> entries) {
        PageRequest sortByTitleAsc = new PageRequest(0, 10, null, "\"title\"", "ASC", true);
        PageRequest sortByTitleDesc = new PageRequest(0, 10, null, "\"title\"", "DESC", true);

        List<FlatEntryDto> expectedAsc = entries.stream().sorted((a, b) -> {
                if (a.bookTitle().equals(b.bookTitle())) return a.bookId().compareTo(b.bookId());
                return a.bookTitle().compareTo(b.bookTitle());
            }).toList();
        List<FlatEntryDto> expectedDesc = entries.stream().sorted((a, b) -> {
                if (a.bookTitle().equals(b.bookTitle())) return b.bookId().compareTo(a.bookId());
                return b.bookTitle().compareTo(a.bookTitle());
            }).toList();

        Page resultAsc = underTest.getPage(sortByTitleAsc);
        Page resultDesc = underTest.getPage(sortByTitleDesc);

        assertThat(resultAsc.entries()).isEqualTo(expectedAsc);
        assertThat(resultDesc.entries()).isEqualTo(expectedDesc);
    }

    private void testSortByYear(List<FlatEntryDto> entries) {
        PageRequest sortByYearAsc = new PageRequest(0, 10, null, "\"year\"", "ASC", true);
        PageRequest sortByYearDesc = new PageRequest(0, 10, null, "\"year\"", "DESC", true);

        List<FlatEntryDto> expectedAsc = entries.stream().sorted((a, b) -> {
            if (a.bookYear() == b.bookYear()) return a.bookId().compareTo(b.bookId());
            return a.bookYear().compareTo(b.bookYear());
            }).toList();

        List<FlatEntryDto> expectedDesc = entries.stream().sorted((a, b) -> {
            if (a.bookYear() == b.bookYear()) return b.bookId().compareTo(a.bookId());
            return b.bookYear().compareTo(a.bookYear());
            }).toList();

        Page resultAsc = underTest.getPage(sortByYearAsc);
        Page resultDesc = underTest.getPage(sortByYearDesc);

        assertThat(resultAsc.entries()).isEqualTo(expectedAsc);
        assertThat(resultDesc.entries()).isEqualTo(expectedDesc);
    }

    private void testSortByShelfMark(List<FlatEntryDto> entries) {
        PageRequest sortByShelfMarkAsc = new PageRequest(0, 10, null, "\"shelf_mark\"", "ASC", true);
        PageRequest sortByShelfMarkDesc = new PageRequest(0, 10, null, "\"shelf_mark\"", "DESC", true);

        List<FlatEntryDto> expectedAsc = entries.stream().sorted((a, b) -> {
            if (a.shelfMark().equals(b.shelfMark())) return a.bookId().compareTo(b.bookId());
            return a.shelfMark().compareTo(b.shelfMark());
            }).toList();

        List<FlatEntryDto> expectedDesc = entries.stream().sorted((a, b) -> {
            if (a.shelfMark().equals(b.shelfMark())) return b.bookId().compareTo(a.bookId());
            return b.shelfMark().compareTo(a.shelfMark());
            }).toList();

        Page resultAsc = underTest.getPage(sortByShelfMarkAsc);
        Page resultDesc = underTest.getPage(sortByShelfMarkDesc);

        assertThat(resultAsc.entries()).isEqualTo(expectedAsc);
        assertThat(resultDesc.entries()).isEqualTo(expectedDesc);
    }

    private void testSortByAuthor(List<FlatEntryDto> entries) {
        PageRequest sortByAuthorsAsc = new PageRequest(0, 10, null, "\"authors\"", "ASC", true);
        PageRequest sortByAuthorsDesc = new PageRequest(0, 10, null, "\"authors\"", "DESC", true);

        List<FlatEntryDto> expectedAsc = entries.stream().sorted((a, b) -> {
            if (a.authors().equals(b.authors())) return a.bookId().compareTo(b.bookId());
            return a.authors().compareTo(b.authors());
            }).toList();

        List<FlatEntryDto> expectedDesc = entries.stream().sorted((a, b) -> {
            if (a.authors().equals(b.authors())) return b.bookId().compareTo(a.bookId());
            return b.authors().compareTo(a.authors());
            }).toList();

        Page resultAsc = underTest.getPage(sortByAuthorsAsc);
        Page resultDesc = underTest.getPage(sortByAuthorsDesc);

        assertThat(resultAsc.entries()).isEqualTo(expectedAsc);
        assertThat(resultDesc.entries()).isEqualTo(expectedDesc);
    }

    private void testSortByEditor(List<FlatEntryDto> entries) {
        PageRequest sortByEditorsAsc = new PageRequest(0, 10, null, "\"editors\"", "ASC", true);
        PageRequest sortByEditorsDesc = new PageRequest(0, 10, null, "\"editors\"", "DESC", true);

        List<FlatEntryDto> expectedAsc = entries.stream().sorted((a, b) -> {
            if (a.editors().equals(b.editors())) return a.bookId().compareTo(b.bookId());
            return a.editors().compareTo(b.editors());
            }).toList();

        List<FlatEntryDto> expectedDesc = entries.stream().sorted((a, b) -> {
            if (a.editors().equals(b.editors())) return b.bookId().compareTo(a.bookId());
            return b.editors().compareTo(a.editors());
            }).toList();

        Page resultAsc = underTest.getPage(sortByEditorsAsc);
        Page resultDesc = underTest.getPage(sortByEditorsDesc);

        assertThat(resultAsc.entries()).isEqualTo(expectedAsc);
        assertThat(resultDesc.entries()).isEqualTo(expectedDesc);
    }

    @Test
    public void testThatGetsCorrectSortedAndFilteredDtosForPage() {
        FlatEntryDto emptyEntry1 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry1 = TestDataUtil.createTestEntry();
        entry1.getBook().setId(emptyEntry1.bookId());
        FlatEntryDto expectedEntry = underTest.update(entry1);

        FlatEntryDto emptyEntry2 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry2 = TestDataUtil.createTestEntry2();
        entry2.getBook().setId(emptyEntry2.bookId());
        underTest.update(entry2);
        
        PageRequest request = new PageRequest(0, 10, "ed", "\"authors\"", "ASC", true);

        Page result = underTest.getPage(request);
        assertThat(result.entries()).containsExactly(expectedEntry);
    }

    @Test
    public void testThatGetsPageWithBook() {
        int n = 15;
        List<FlatEntryDto> entries = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            FlatEntryDto entry = underTest.createEmptyEntry().orElseThrow();
            entries.add(entry);
        }

        long bookId = 9L;
        int pageSize = 4;
        List<FlatEntryDto> expected = entries.subList(7, 11);
        
        PageRequest request = new PageRequest(0, pageSize, null, "b.\"id\"", "DESC", true);

        Page result = underTest.getPageWithBook(bookId, request);
        
        assertThat(result.entries()).containsExactly(expected.get(3), expected.get(2), expected.get(1), expected.get(0));
    }

    @Test
    public void testThatGetsAllFLatEntryDtos() {
        FlatEntryDto emptyEntry1 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry1 = TestDataUtil.createTestEntry();
        entry1.getBook().setId(emptyEntry1.bookId());

        FlatEntryDto emptyEntry2 = underTest.createEmptyEntry().orElseThrow();        
        FullEntryDto entry2 = TestDataUtil.createTestEntry2();
        entry2.getBook().setId(emptyEntry2.bookId());

        entry1.getAuthors().add(TestDataUtil.createTestPerson3());
        underTest.update(entry1);
        underTest.update(entry2);

        FlatEntryDto expectedEntryRow1 = TestDataUtil.createEntryRowFromEntry(entry1);
        FlatEntryDto expectedEntryRow2 = TestDataUtil.createEntryRowFromEntry(entry2);
        
        List<FlatEntryDto> result = underTest.getAllFlatEntryDtos();
        assertThat(result)
            .hasSize(2)
            .containsExactly(expectedEntryRow1, expectedEntryRow2);
    }

    @Test
    public void testThatCanUpdateEntry() {
        FlatEntryDto emptyEntry = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry = TestDataUtil.createTestEntry();
        entry.getBook().setId(emptyEntry.bookId());
        underTest.update(entry);

        entry.getAuthors().add(TestDataUtil.createTestPerson2());
        
        underTest.update(entry);
        Optional<FullEntryDto> result = underTest.findById(entry.getBook().getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entry);
        List<FullEntryDto> resultList = underTest.findAll();
        assertThat(resultList).containsExactly(entry);
    }


    @Test
    public void testThatEntryCanBeDeleted() {
        FlatEntryDto emptyEntry = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry = TestDataUtil.createTestEntry();
        entry.getBook().setId(emptyEntry.bookId());
        underTest.update(entry);

        underTest.delete(entry.getBook().getId());
        Optional<FullEntryDto> result = underTest.findById(entry.getBook().getId());
        assertThat(result).isEmpty();
    }
    
}
