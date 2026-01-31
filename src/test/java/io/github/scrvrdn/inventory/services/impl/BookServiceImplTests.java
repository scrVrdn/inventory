package io.github.scrvrdn.inventory.services.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.BookUpdateRequest;
import io.github.scrvrdn.inventory.repositories.BookPersonRepository;
import io.github.scrvrdn.inventory.repositories.BookPublisherRepository;
import io.github.scrvrdn.inventory.repositories.BookRepository;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTests {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookPersonRepository bookPersonRepository;
    
    @Mock
    private BookPublisherRepository bookPublisherRepository;

    @InjectMocks
    private BookServiceImpl underTest;

    @Test
    public void testThatCreateCallsBookRepository() {
        Book mockBook = mock(Book.class);
        underTest.create(mockBook);
        verify(bookRepository).create(mockBook);
    }

    @Test
    public void testThatUpdateCallsRepositories() {
        long bookId = 1L;
        
        Book mockBook = mock(Book.class);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));

        BookUpdateRequest request = mock(BookUpdateRequest.class);
        List<Long> authorsToUpdate = List.of(1L, 2L, 3L);
        when(request.authorIds()).thenReturn(authorsToUpdate);
        List<Long> editorsToUpdate = List.of(4L, 5L, 6L);
        when(request.editorIds()).thenReturn(editorsToUpdate);
        when(request.publisherId()).thenReturn(1L);
        
        List<Long> authorsToRemove = List.of(7L, 8L, 9L);
        when(bookPersonRepository.findAuthorIdsByBookId(bookId)).thenReturn(authorsToRemove);
        List<Long> editorsToRemove = List.of(10L, 11L, 12L);
        when(bookPersonRepository.findEditorIdsByBookId(bookId)).thenReturn(editorsToRemove);
        long publisherToRemove = 2L;
        when(bookPublisherRepository.findPublisherIdByBookId(bookId)).thenReturn(publisherToRemove);

        underTest.update(bookId, request);

        verify(bookRepository).findById(bookId);
        verify(bookRepository).update(mockBook);
        verify(bookPersonRepository).assignAuthorsToBook(bookId, request.authorIds());
        verify(bookPersonRepository).removeAuthorsFromBook(bookId, authorsToRemove);
        verify(bookPersonRepository).assignEditorsToBook(bookId, request.editorIds());
        verify(bookPersonRepository).removeEditorsFromBook(bookId, editorsToRemove);
        verify(bookPublisherRepository).assignPublisherToBook(bookId, request.publisherId());
    }

    @Test
    public void testThatUpdateDeletesPublisherRelationWhenPublisherIdIsNull() {
        long bookId = 1L;
        Book mockBook = mock(Book.class);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));

        BookUpdateRequest request = mock(BookUpdateRequest.class);
        when(request.publisherId()).thenReturn(null);
        long publisherToRemove = 2L;
        when(bookPublisherRepository.findPublisherIdByBookId(bookId)).thenReturn(publisherToRemove);
        

        underTest.update(bookId, request);
        verify(bookPublisherRepository).removePublisherFromBook(bookId, publisherToRemove);
    }

    @Test
    public void testThatDeleteCallsRepository() {
        long bookId = 1L;
        underTest.delete(bookId);
        
        verify(bookRepository).delete(bookId);
    }

    @Test
    public void testThatNumberOfRowsCallsBookRepository() {
        underTest.numberOfRows();
        verify(bookRepository).numberOfRows();
    }
}
