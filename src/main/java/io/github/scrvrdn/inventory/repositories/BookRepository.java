package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.domain.Book;
import io.github.scrvrdn.inventory.domain.Person;
import io.github.scrvrdn.inventory.domain.Publisher;

public interface BookRepository {

    void create(Book book);

    Optional<Book> findById(long id);

    List<Book> findAll();

    void update(Book book);
    
    void delete(long id);


    void assignToAuthor(Book book, Person author);

    void assignToAuthor(Book book, List<Person> authors);

    List<Person> findAuthors(Book book);


    void assignToEditor(Book book, Person editor);

    void assignToEditor(Book book, List<Person> editors);

    List<Person> findEditors(Book book);


    void assignToPublisher(Book book, Publisher publisher);

    Optional<Publisher> findPublisher(Book book);
}