package io.github.scrvrdn.inventory.services.facade.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.repositories.EntryViewRepository;
import io.github.scrvrdn.inventory.services.BookService;

@ExtendWith(MockitoExtension.class)
public class EntryServiceImplTests {
    
    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private BookService bookService;

    @Mock
    private EntryViewRepository entryViewRepository;

    @Spy
    @InjectMocks
    private EntryServiceImpl underTest;

    @Test
    public void testThatCreateEmptyEntryCallsBookRepository() {
        Book mockBook = mock(Book.class);
        when(mockBook.getId()).thenReturn(1L);        
        when(underTest.createEmptyBook()).thenReturn(mockBook);

        FlatEntryDto expected = new FlatEntryDto(1L, null, null, null, null, null, null);

        Optional<FlatEntryDto> result = underTest.createEmptyEntry();        

        assertThat(result.get()).isEqualTo(expected);
        verify(bookService).create(mockBook);
    }

    @Test
    public void testThatFindByIdCallsEntryViewRepository() {
        long bookId = 1L;
        underTest.findById(bookId);
        verify(entryViewRepository).findById(bookId);
    }

    @Test
    public void testThatFindAllCallsEntryViewRepository() {
        underTest.findAll();
        verify(entryViewRepository).findAll();
    }

    @Test
    public void testThatGetFlatEntryDtoByBookIdCallsEntryViewRepository() {
        long bookId = 1L;
        underTest.getFlatEntryDtoByBookId(bookId);
        verify(entryViewRepository).getFlatEntryDtoByBookId(bookId);
    }

    @Test
    public void testThatGetNextFlatEntryDtoAfterBookIdCallsEntryViewRepository() {
        long bookId = 1L;
        underTest.getNextFlatEntryDtoAfterBookId(bookId);
        verify(entryViewRepository).getNextFlatEntryDtoAfterBookId(bookId);
    }

    @Test
    public void testThatGetFlatEntryDtosGeneratesCallsEntryViewRepository() {
        int numberOfEntries = 3;
        int fromRow = 4;
        underTest.getFlatEntryDtos(numberOfEntries, fromRow);
        verify(entryViewRepository).getFlatEntryDtos(numberOfEntries, fromRow);
    }

    @Test
    public void testThatGetAllFlatEntryDtoCallsEntryViewRepository() {
        underTest.getAllFlatEntryDtos();
        verify(entryViewRepository).getAllFlatEntryDtos();
    }

    @Test
    public void testThatDeleteCallsBookService() {
        long bookId = 1L;
        underTest.delete(bookId);
        verify(bookService).delete(bookId);
    }

    @Test
    public void testThatNumberOfRowsCallsBookService() {
        underTest.numberOfRows();
        verify(bookService).numberOfRows();
    }

}
