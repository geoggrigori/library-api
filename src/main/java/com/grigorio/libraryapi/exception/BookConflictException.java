package com.grigorio.libraryapi.exception;

/**
 * Thrown when a borrow/return operation conflicts with the current state of a book.
 */
public class BookConflictException extends RuntimeException {

    public BookConflictException(String message) {
        super(message);
    }
}
