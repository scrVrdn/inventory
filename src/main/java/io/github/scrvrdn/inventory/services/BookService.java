package io.github.scrvrdn.inventory.services;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.BookUpdateRequest;

public interface BookService {

    void create(Book book);

    void update(long bookId, BookUpdateRequest request);

    void delete(long bookId);
}
