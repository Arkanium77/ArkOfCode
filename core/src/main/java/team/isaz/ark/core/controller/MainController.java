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
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.service.PublisherService;
import team.isaz.ark.core.service.SearchService;

import java.util.List;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/main")
public class MainController {
    private final SearchService searchService;
    private final PublisherService publisherService;

    public MainController(SearchService searchService, PublisherService publisherService) {
        this.searchService = searchService;
        this.publisherService = publisherService;
    }


    @GetMapping("/get")
    public ResponseEntity<List<Snippet>> getAll() {
        return new ResponseEntity<>(searchService.all(), HttpStatus.OK);
    }

    @GetMapping("/get/{author}")
    public ResponseEntity<List<Snippet>> getAllAvailable(@PathVariable @NotBlank String author) {
        return new ResponseEntity<>(searchService.allAvailable(author), HttpStatus.OK);
    }

    @PostMapping("/find/{author}")
    public ResponseEntity<List<Snippet>> find(@PathVariable @NotBlank String author, @RequestParam String query) {
        return new ResponseEntity<>(searchService.find(author, query), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<String> response(
            @Schema(name = "NAME",
                    description = "DESCRIPTION")
            @RequestBody Snippet snippet) {
        return new ResponseEntity<>(publisherService.publish(snippet), HttpStatus.OK);
    }
}
