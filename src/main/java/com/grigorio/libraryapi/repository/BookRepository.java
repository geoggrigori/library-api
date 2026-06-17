package com.grigorio.libraryapi.repository;

import com.grigorio.libraryapi.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for {@link Book} entities.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAvailable(boolean available);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * Searches books by optional, case-insensitive title and author substrings
     * combined with an optional availability flag. A {@code null} parameter is
     * ignored so any combination of filters can be supplied.
     */
    @Query("""
            SELECT b FROM Book b
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
              AND (:available IS NULL OR b.available = :available)
            """)
    List<Book> search(@Param("title") String title,
                      @Param("author") String author,
                      @Param("available") Boolean available);
}
