package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.Book;

public interface BookRepository {

    void create(Book book);

    Optional<Book> findById(long id);

    List<Book> findAll();

    void update(Book book);
    
    void delete(long id);
    
}