package com.grigorio.libraryapi.exception;

/**
 * Thrown when a book cannot be found for a given id.
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book not found with id " + id);
    }
}
