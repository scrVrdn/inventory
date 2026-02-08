package io.github.scrvrdn.inventory.services.facade;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;

@SpringBootTest
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
    public void testThatGetsCorrectSortedAndFilteredDtosForPage() {
        FlatEntryDto emptyEntry1 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry1 = TestDataUtil.createTestEntry();
        entry1.getBook().setId(emptyEntry1.bookId());
        FlatEntryDto expectedEntry = underTest.update(entry1);

        FlatEntryDto emptyEntry2 = underTest.createEmptyEntry().orElseThrow();
        FullEntryDto entry2 = TestDataUtil.createTestEntry2();
        entry2.getBook().setId(emptyEntry2.bookId());
        underTest.update(entry2);
        

        Page result = underTest.getSortedAndFilteredEntries(10, 0, null, "ed");
        assertThat(result.entries()).containsExactly(expectedEntry);
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
