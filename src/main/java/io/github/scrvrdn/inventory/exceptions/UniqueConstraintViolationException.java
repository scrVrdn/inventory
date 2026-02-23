package io.github.scrvrdn.inventory.exceptions;

public class UniqueConstraintViolationException extends RuntimeException {

    public UniqueConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
