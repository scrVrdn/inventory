package io.github.scrvrdn.inventory.services.facade.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.BookUpdateRequest;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.repositories.EntryViewRepository;
import io.github.scrvrdn.inventory.services.domain.BookService;
import io.github.scrvrdn.inventory.services.domain.PersonService;
import io.github.scrvrdn.inventory.services.domain.PublisherService;
import io.github.scrvrdn.inventory.services.facade.EntryService;

@Service
public class EntryServiceImpl implements EntryService {

    private final EntryViewRepository entryViewRepository;
    private final BookService bookService;
    private final PersonService personService;
    private final PublisherService publisherService;

    public EntryServiceImpl(
        final EntryViewRepository entryViewRepository,
        final BookService bookService,
        final PersonService personService,
        final PublisherService publisherService
    ) {
        this.entryViewRepository = entryViewRepository;
        this.bookService = bookService;
        this.personService = personService;
        this.publisherService = publisherService;
    }

    @Transactional
    @Override
    public Optional<FlatEntryDto> createEmptyEntry() {
        Book emptyBook = createEmptyBook();
        bookService.create(emptyBook);
        return Optional.of(new FlatEntryDto(emptyBook.getId(), null, null, null, null, null, null));
    }

    protected Book createEmptyBook() {
        return Book.builder().build();
    }

    @Override
    public Optional<FullEntryDto> findById(long id) {
        return entryViewRepository.findById(id);
    }

    @Override
    public List<FullEntryDto> findAll() {
        return entryViewRepository.findAll();
    }

    @Override
    public Optional<FlatEntryDto> getFlatEntryDtoByBookId(long bookId) {
        return entryViewRepository.getFlatEntryDtoByBookId(bookId);
    }

    @Override
    public Optional<FlatEntryDto> getNextFlatEntryDtoAfterBookId(long bookId) {
        return entryViewRepository.getNextFlatEntryDtoAfterBookId(bookId);
    }

    @Override
    public List<FlatEntryDto> getFlatEntryDtos(int numberOfEntries, int fromRow) {
        return entryViewRepository.getFlatEntryDtos(numberOfEntries, fromRow);
    }

    @Override
    public List<FlatEntryDto> getAllFlatEntryDtos() {
        return entryViewRepository.getAllFlatEntryDtos();
    }

    @Transactional
    @Override
    public FlatEntryDto update(FullEntryDto entry) {
        convertEmptyStringsToNull(entry);

        List<Long> authorIds = personService.updateAuthorsByName(entry.getAuthors());
        List<Long> editorIds = personService.updateEditorsByName(entry.getEditors());
        Long publisherId = publisherService.updatePublisherByNameAndLocation(entry.getPublisher());
        Book book = entry.getBook();

        BookUpdateRequest request = new BookUpdateRequest(
                                            book.getTitle(),
                                            book.getYear(),
                                            book.getIsbn10(),
                                            book.getIsbn13(),
                                            book.getShelfMark(),
                                            authorIds,
                                            editorIds,
                                            publisherId
                                        );

        bookService.update(entry.getBook().getId(), request);

        return mapFullDtoToFlatDto(entry);
    }

    private void convertEmptyStringsToNull(FullEntryDto entry) {
        convertEmptyStringsToNull(entry.getBook());

        for (Person a : entry.getAuthors()) {
            convertEmptyStringsToNull(a);
        }
        
        for (Person e : entry.getEditors()) {
            convertEmptyStringsToNull(e);
        }

        convertEmptyStringsToNull(entry.getPublisher());
    }

    private void convertEmptyStringsToNull(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(f -> f.getType() == String.class)
                .forEach(f -> {
                    f.setAccessible(true);
                    try {
                        String value = (String) f.get(obj);
                        if (value != null && value.trim().isEmpty()) {
                            f.set(obj, null);
                        }
                    } catch (IllegalAccessException e) {
                        System.err.println(e.getMessage());
                    }
                });
    }

    private FlatEntryDto mapFullDtoToFlatDto(FullEntryDto fullDto) {
        return new FlatEntryDto(
            fullDto.getBook().getId(),
            fullDto.getBook().getTitle(),
            fullDto.getBook().getYear(),
            fullDto.getBook().getShelfMark(),
            fullDto.getAuthors().stream()
                .map(a -> a.toString())
                .collect(Collectors.joining("; ")),
            fullDto.getEditors().stream()
                .map(e -> e.toString())
                .collect(Collectors.joining("; ")),
            fullDto.getPublisher().toString()
        );
    }

    @Transactional
    @Override
    public void delete(long id) {
        bookService.delete(id);
    }

    public int numberOfRows() {
        return bookService.numberOfRows();
    }
}
