package com.grigorio.libraryapi.repository;

import com.grigorio.libraryapi.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Book} entities.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAvailable(boolean available);
}
