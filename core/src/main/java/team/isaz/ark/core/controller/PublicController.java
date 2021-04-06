package team.isaz.ark.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.service.PublisherService;
import team.isaz.ark.core.service.SearchService;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final SearchService searchService;
    private final PublisherService publisherService;

    @Operation(
            summary = "Поиск сниппетов",
            description = "Поиск сниппетов для неавторизованного пользователя (только публичные)"
    )
    @GetMapping("/search")
    public ResponseEntity<List<Snippet>> search(@RequestParam @NotBlank String query) {
        return new ResponseEntity<>(searchService.search(query), HttpStatus.OK);
    }

    //-- На удаление

    @GetMapping("/get")
    public ResponseEntity<List<Snippet>> getAll() {
        return new ResponseEntity<>(searchService.all(), HttpStatus.OK);
    }

    @GetMapping("/get/{author}")
    public ResponseEntity<List<Snippet>> getAllAvailable(@PathVariable @NotBlank String author) {
        return new ResponseEntity<>(searchService.allAvailable(author), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<String> response(
            @Schema(name = "NAME",
                    description = "DESCRIPTION")
            @RequestBody Snippet snippet) {
        return new ResponseEntity<>(publisherService.publish(snippet).name(), HttpStatus.OK);
    }
}
