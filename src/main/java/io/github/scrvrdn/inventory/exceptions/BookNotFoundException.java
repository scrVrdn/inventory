package io.github.scrvrdn.inventory.exceptions;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(long id) {
        super("Book with Id " + id + " not found.");
    }
}
