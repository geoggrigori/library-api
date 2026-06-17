package com.grigorio.libraryapi.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.grigorio.libraryapi.model.Book;
import com.grigorio.libraryapi.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private Book persist(String title, String author, String isbn, boolean available) {
        Book book = new Book(title, author, isbn);
        book.setAvailable(available);
        return repository.save(book);
    }

    private String json(String title, String author, String isbn) {
        return """
                {"title":"%s","author":"%s","isbn":"%s"}
                """.formatted(title, author, isbn);
    }

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Clean Code", "Robert C. Martin", "9780132350884")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void createWithInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "", "abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.author").exists())
                .andExpect(jsonPath("$.fieldErrors.isbn").exists());
    }

    @Test
    void getByIdReturnsBook() throws Exception {
        Book saved = persist("Refactoring", "Martin Fowler", "9780201485677", true);

        mockMvc.perform(get("/books/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Refactoring")));
    }

    @Test
    void getByMissingIdReturns404() throws Exception {
        mockMvc.perform(get("/books/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void listReturnsAllBooks() throws Exception {
        persist("Book A", "Author A", "1111111111", true);
        persist("Book B", "Author B", "2222222222", false);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listFilterByAvailableTrue() throws Exception {
        persist("Available Book", "Author A", "1111111111", true);
        persist("Borrowed Book", "Author B", "2222222222", false);

        mockMvc.perform(get("/books").param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Available Book")));
    }

    @Test
    void listFilterByAvailableFalse() throws Exception {
        persist("Available Book", "Author A", "1111111111", true);
        persist("Borrowed Book", "Author B", "2222222222", false);

        mockMvc.perform(get("/books").param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Borrowed Book")));
    }

    @Test
    void listFilterByTitleSubstringCaseInsensitive() throws Exception {
        persist("Clean Code", "Robert C. Martin", "1111111111", true);
        persist("The Clean Coder", "Robert C. Martin", "2222222222", false);
        persist("Refactoring", "Martin Fowler", "3333333333", true);

        mockMvc.perform(get("/books").param("title", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listFilterByAuthorSubstringCaseInsensitive() throws Exception {
        persist("Clean Code", "Robert C. Martin", "1111111111", true);
        persist("Refactoring", "Martin Fowler", "2222222222", true);

        mockMvc.perform(get("/books").param("author", "fowler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Refactoring")));
    }

    @Test
    void listFilterByTitleAndAvailableCombined() throws Exception {
        persist("Clean Code", "Robert C. Martin", "1111111111", true);
        persist("The Clean Coder", "Robert C. Martin", "2222222222", false);
        persist("Refactoring", "Martin Fowler", "3333333333", true);

        mockMvc.perform(get("/books")
                        .param("title", "clean")
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));
    }

    @Test
    void listFilterByAuthorAndAvailableCombined() throws Exception {
        persist("Clean Code", "Robert C. Martin", "1111111111", true);
        persist("The Clean Coder", "Robert C. Martin", "2222222222", false);

        mockMvc.perform(get("/books")
                        .param("author", "martin")
                        .param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("The Clean Coder")));
    }

    @Test
    void updateReplacesBookFields() throws Exception {
        Book saved = persist("Old Title", "Old Author", "1111111111", true);

        mockMvc.perform(put("/books/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("New Title", "New Author", "9780201485677")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Title")))
                .andExpect(jsonPath("$.author", is("New Author")));
    }

    @Test
    void updateMissingReturns404() throws Exception {
        mockMvc.perform(put("/books/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("New Title", "New Author", "9780201485677")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        Book saved = persist("To Delete", "Author", "1111111111", true);

        mockMvc.perform(delete("/books/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/books/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMissingReturns404() throws Exception {
        mockMvc.perform(delete("/books/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void borrowAndReturnFlow() throws Exception {
        Book saved = persist("Borrowable", "Author", "1111111111", true);

        mockMvc.perform(post("/books/{id}/borrow", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));

        // Borrowing again conflicts.
        mockMvc.perform(post("/books/{id}/borrow", saved.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Return makes it available again.
        mockMvc.perform(post("/books/{id}/return", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(true)));

        // Now it can be borrowed once more.
        mockMvc.perform(post("/books/{id}/borrow", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void borrowMissingReturns404() throws Exception {
        mockMvc.perform(post("/books/{id}/borrow", 999999))
                .andExpect(status().isNotFound());
    }
}
