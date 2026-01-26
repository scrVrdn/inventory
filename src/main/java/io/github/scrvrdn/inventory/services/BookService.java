package io.github.scrvrdn.inventory.services;

import io.github.scrvrdn.inventory.dto.BookUpdateRequest;

public interface BookService {

    void update(long bookId, BookUpdateRequest request);
}
