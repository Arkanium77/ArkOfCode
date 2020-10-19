package team.isaz.ark.core.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.core.entity.Book;
import team.isaz.ark.core.repository.BookRepository;

import java.util.List;

@RestController
@RequestMapping("/main")
public class MainController {
    BookRepository repository;

    public MainController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Book>> getAll() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/get/title/{title}")
    public ResponseEntity<List<Book>> getAllByTitle(@PathVariable String title) {
        return new ResponseEntity<>(repository.findAllByTitleAndIsHiddenFalse(title), HttpStatus.OK);
    }

    @GetMapping("/get/title/{title}/author")
    public ResponseEntity<List<Book>> getAllByTitleAndAuthor(@PathVariable String title,
                                                             @RequestParam(defaultValue = "Автор") String author) {
        return new ResponseEntity<>(repository.findAllByTitleAndAuthor(title, author), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<String> response(
            @RequestParam(defaultValue = "Автор") String author,
            @RequestParam(defaultValue = "Название") String title,
            @RequestParam(defaultValue = "false") Boolean isHidden,
            @Schema(name = "NAME",
                    description = "DESCRIPTION",
                    defaultValue = "Текст этой замечательной книги")
            @RequestBody String text) {
        Book book = Book.builder().author(author).isHidden(isHidden).title(title).text(text).build();
        repository.save(book);
        return new ResponseEntity<>(book.toString(), HttpStatus.OK);
    }
}
