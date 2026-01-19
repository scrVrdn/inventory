package io.github.scrvrdn.inventory.services;

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
import io.github.scrvrdn.inventory.dto.FlatEntryDto;

import static org.assertj.core.api.Assertions.assertThat;

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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books", "persons", "publishers", "authored", "edited", "published");
    }

    @Test
    public void testThatFullEntryDtoCanBeCreatedAndRetrieved() {
        FullEntryDto entry = TestDataUtil.createTestEntry();
        entry.getAuthors().add(TestDataUtil.createTestPerson3());
        entry.getAuthors().add(TestDataUtil.createTestPerson4());
        entry.getEditors().add(TestDataUtil.createTestPerson4());
        underTest.create(entry);
        
        Optional<FullEntryDto> result = underTest.findById(entry.getBook().getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entry);
    }

    @Test
    public void testThatFindsAllFullEntryDtos() {

        FullEntryDto entry1 = TestDataUtil.createTestEntry();
        FullEntryDto entry2 = TestDataUtil.createTestEntry2();
        entry1.getAuthors().add(TestDataUtil.createTestPerson3());
        entry2.getAuthors().add(TestDataUtil.createTestPerson());
        entry1.getAuthors().add(TestDataUtil.createTestPerson2());
        entry1.getAuthors().add(TestDataUtil.createTestPerson4());
        underTest.create(entry1);
        underTest.create(entry2);

        List<FullEntryDto> result = underTest.findAll();
        assertThat(result)
            .hasSize(2)
            .containsExactly(entry1, entry2);
    }

    @Test
    public void testThatGetsFlatEntryDtos() {
        FullEntryDto entry = TestDataUtil.createTestEntry();
        entry.getAuthors().add(TestDataUtil.createTestPerson3());
        entry.getEditors().add(TestDataUtil.createTestPerson4());
        underTest.create(entry);
        FlatEntryDto entryRow = TestDataUtil.createEntryRowFromEntry(entry);

        Optional<FlatEntryDto> result = underTest.getFlatEntryDto(entry.getBook().getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entryRow);
    }

    @Test
    public void testThatGetsAllFLatEntryDtos() {
        FullEntryDto entry1 = TestDataUtil.createTestEntry();
        FullEntryDto entry2 = TestDataUtil.createTestEntry2();
        entry1.getAuthors().add(TestDataUtil.createTestPerson3());
        underTest.create(entry1);
        underTest.create(entry2);
        FlatEntryDto entryRow1 = TestDataUtil.createEntryRowFromEntry(entry1);
        FlatEntryDto entryRow2 = TestDataUtil.createEntryRowFromEntry(entry2);
        
        List<FlatEntryDto> result = underTest.getAllFlatEntryDto();
        assertThat(result)
            .hasSize(2)
            .containsExactly(entryRow1, entryRow2);
    }

    @Test
    public void testThatCanUpdateEntry() {
        FullEntryDto entry = TestDataUtil.createTestEntry();
        underTest.create(entry);

        entry.getBook().setTitle("UPDATED");
        entry.getUpdatedFields().add("book");
       // entry.getAuthors().add(TestDataUtil.createTestPerson2());
        
        underTest.update(entry);

        List<FullEntryDto> result = underTest.findAll();
        assertThat(result).containsExactly(entry);
    }

    
}
