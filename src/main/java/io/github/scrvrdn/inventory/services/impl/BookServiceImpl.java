package io.github.scrvrdn.inventory.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.BookUpdateRequest;
import io.github.scrvrdn.inventory.repositories.BookPersonRepository;
import io.github.scrvrdn.inventory.repositories.BookPublisherRepository;
import io.github.scrvrdn.inventory.repositories.BookRepository;
import io.github.scrvrdn.inventory.services.BookService;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookPersonRepository bookPersonRepository;
    private final BookPublisherRepository bookPublisherRepository;

    public BookServiceImpl(final BookRepository bookRepository, final BookPersonRepository bookPersonRepository, final BookPublisherRepository bookPublisherRepository) {
        this.bookRepository = bookRepository;
        this.bookPersonRepository = bookPersonRepository;
        this.bookPublisherRepository = bookPublisherRepository;
    }

    @Override
    public void create(Book book) {
        bookRepository.create(book);
    }

    @Override
    public void update(long bookId, BookUpdateRequest request) {
        updateBookFields(bookId, request);
        updateAuthors(bookId, request);
        updateEditors(bookId, request);
        updatePublisher(bookId, request);
    }

    private void updateBookFields(long bookId, BookUpdateRequest request) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        book.setTitle(request.title());
        book.setYear(request.year());
        book.setIsbn10(request.isbn10());
        book.setIsbn13(request.isbn13());
        book.setShelfMark(request.shelfMark());
        bookRepository.update(book);
    }

    private void updateAuthors(long bookId, BookUpdateRequest request) {
        List<Long> authorsToRemove = detectRemovedAuthors(bookId, request.authorIds());
        if (!authorsToRemove.isEmpty()) bookPersonRepository.removeAuthorsFromBook(bookId, authorsToRemove);
        if (!request.authorIds().isEmpty()) bookPersonRepository.assignAuthorsToBook(bookId, request.authorIds());
    }

    private List<Long> detectRemovedAuthors(long bookId, List<Long> requestedAuthors) {
        List<Long> currentAuthorIds = bookPersonRepository.findAuthorIdsByBookId(bookId);
        return currentAuthorIds.stream().filter(id -> !requestedAuthors.contains(id)).toList();
    }

    private void updateEditors(long bookId, BookUpdateRequest request) {
        List<Long> editorsToRemove = detectRemovedEditors(bookId, request.editorIds());
        if (!editorsToRemove.isEmpty()) bookPersonRepository.removeEditorsFromBook(bookId, editorsToRemove);
        if (!request.editorIds().isEmpty()) bookPersonRepository.assignEditorsToBook(bookId, request.editorIds());
    }

    private List<Long> detectRemovedEditors(long bookId, List<Long> requestedEditors) {
        List<Long> currentEditorIds = bookPersonRepository.findEditorIdsByBookId(bookId);
        return currentEditorIds.stream().filter(id -> !requestedEditors.contains(id)).toList();
    }

    private void updatePublisher(long bookId, BookUpdateRequest request) {
        Long currentPublisherId = bookPublisherRepository.findPublisherIdByBookId(bookId);
        if (request.publisherId() == null && currentPublisherId != null) bookPublisherRepository.removePublisherFromBook(bookId, currentPublisherId);
        if (request.publisherId() != null && request.publisherId() != currentPublisherId) bookPublisherRepository.assignPublisherToBook(bookId, request.publisherId());
    }

    @Override
    public void delete(long bookId) {
        bookRepository.delete(bookId);
    }
}
