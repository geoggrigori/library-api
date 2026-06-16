package com.grigorio.libraryapi.service;

import com.grigorio.libraryapi.exception.BookConflictException;
import com.grigorio.libraryapi.exception.BookNotFoundException;
import com.grigorio.libraryapi.model.Book;
import com.grigorio.libraryapi.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Business logic for managing library books.
 */
@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(Boolean available) {
        if (available != null) {
            return repository.findByAvailable(available);
        }
        return repository.findAll();
    }

    public Book findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        book.setId(null);
        book.setAvailable(true);
        return repository.save(book);
    }

    public Book update(Long id, Book updated) {
        Book existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setIsbn(updated.getIsbn());
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public Book borrow(Long id) {
        Book book = findById(id);
        if (!book.isAvailable()) {
            throw new BookConflictException("Book with id " + id + " is already borrowed");
        }
        book.setAvailable(false);
        return repository.save(book);
    }

    public Book returnBook(Long id) {
        Book book = findById(id);
        book.setAvailable(true);
        return repository.save(book);
    }
}
