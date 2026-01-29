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
        Book book = bookRepository.findById(bookId).orElseThrow();
        
        book.setTitle(request.title());
        book.setYear(request.year());
        book.setIsbn10(request.isbn10());
        book.setIsbn13(request.isbn13());
        book.setShelfMark(request.shelfMark());

        bookRepository.update(book);

        updateAuthors(bookId, request.authorIds());
        updateEditors(bookId, request.editorIds());

        if (request.publisherId() != null) bookPublisherRepository.assignPublisherToBook(bookId, request.publisherId());   
    }

    private void updateAuthors(long bookId, List<Long> requestedAuthors) {
        List<Long> authorsToRemove = detectRemovedAuthors(bookId, requestedAuthors);
        if (!authorsToRemove.isEmpty()) bookPersonRepository.removeAuthorsFromBook(bookId, authorsToRemove);

        if (!requestedAuthors.isEmpty()) bookPersonRepository.assignAuthorsToBook(bookId, requestedAuthors);
    }

    private List<Long> detectRemovedAuthors(long bookId, List<Long> requestedAuthors) {
        List<Long> currentAuthorIds = bookPersonRepository.findAuthorIdsByBookId(bookId);
        return currentAuthorIds.stream().filter(id -> !requestedAuthors.contains(id)).toList();
    }

    private void updateEditors(long bookId, List<Long> requestedEditors) {
        List<Long> editorsToRemove = detectRemovedEditors(bookId, requestedEditors);
        if (!editorsToRemove.isEmpty()) bookPersonRepository.removeEditorsFromBook(bookId, editorsToRemove);

        if (!requestedEditors.isEmpty()) bookPersonRepository.assignEditorsToBook(bookId, requestedEditors);
    }

    private List<Long> detectRemovedEditors(long bookid, List<Long> requestedEditors) {
        List<Long> currentEditorIds = bookPersonRepository.findEditorIdsByBookId(bookid);
        return currentEditorIds.stream().filter(id -> !requestedEditors.contains(id)).toList();
    }

    @Override
    public void delete(long bookId) {
        bookRepository.delete(bookId);
    }
}
