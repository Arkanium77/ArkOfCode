package team.isaz.ark.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.service.SearchService;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final SearchService searchService;

    @Operation(
            summary = "Поиск сниппетов",
            description = "Поиск сниппетов для неавторизованного пользователя (только публичные)"
    )
    @GetMapping("/search")
    public ResponseEntity<List<Snippet>> search(@RequestParam @NotBlank String query) {
        return new ResponseEntity<>(searchService.search(query), HttpStatus.OK);
    }


    @Operation(
            summary = "Получение сниппета",
            description = "Получение сниппета по айди для неавторизованного пользователя (только публичные)"
    )
    @GetMapping("/get/{snippetId}")
    public ResponseEntity<Snippet> get(@PathVariable String snippetId) {
        return new ResponseEntity<>(searchService.get(snippetId), HttpStatus.OK);
    }

}
