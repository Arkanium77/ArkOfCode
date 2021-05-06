package team.isaz.ark.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.core.dto.Response;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.service.AuthService;
import team.isaz.ark.core.service.PublisherService;
import team.isaz.ark.core.service.SearchService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/secured")
@RequiredArgsConstructor
public class SecuredController {
    private final SearchService searchService;
    private final PublisherService publisherService;
    private final AuthService authService;

    @Operation(
            summary = "Узнать свой логин",
            description = "Узнать свой логин",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<TokenCheck> getMyLogin(@RequestHeader HttpHeaders headers) {
        String bearerToken = authService.getToken(headers);
        return ResponseEntity.ok(authService.checkToken(bearerToken));
    }

    @Operation(
            summary = "Поиск сниппетов",
            description = "Поиск сниппетов среди доступных для авторизованного пользователя (свои+все публичные)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/search")
    public ResponseEntity<List<Snippet>> search(@RequestHeader HttpHeaders headers,
                                                @RequestParam @NotBlank String query) {
        String bearerToken = authService.getToken(headers);
        return new ResponseEntity<>(searchService.search(bearerToken, query), HttpStatus.OK);
    }

    @Operation(
            summary = "Получение сниппета",
            description = "Получение сниппета по айди среди доступных для авторизованного пользователя (свои+все публичные)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/{snippetId}")
    public ResponseEntity<Snippet> get(@RequestHeader HttpHeaders headers,
                                       @PathVariable String snippetId) {
        String bearerToken = authService.getToken(headers);
        return new ResponseEntity<>(searchService.get(bearerToken, snippetId), HttpStatus.OK);
    }

    @Operation(
            summary = "Публикация сниппета",
            description = "Публикация сниппета (для авторизованного пользователя)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/publish")
    public ResponseEntity<Response> publish(
            @RequestHeader HttpHeaders headers,
            @RequestParam(name = "Статус сокрытия", defaultValue = "true") boolean hidden,
            @RequestParam(name = "Название сниппета") @NotBlank String title,
            @RequestParam(name = "Теги", required = false) @Valid Set<@NotBlank String> tags,
            @Schema(name = "Текст сниппета",
                    description = "Основное содержимое публикуемого сниппета", required = true)
            @RequestBody @NotBlank String text) {
        String bearerToken = authService.getToken(headers);
        return new ResponseEntity<>(publisherService.publish(bearerToken, hidden, title, tags, text), HttpStatus.OK);
    }

    @Operation(
            summary = "Обновление сниппета",
            description = "Обновление сниппета (для авторизованного пользователя)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/update/{snippetId}")
    public ResponseEntity<Response> update(
            @PathVariable String snippetId,
            @RequestHeader HttpHeaders headers,
            @RequestParam(name = "Статус сокрытия", required = false) Boolean hidden,
            @RequestParam(name = "Название сниппета", required = false) String title,
            @RequestParam(name = "Теги", required = false) @Valid Set<@NotBlank String> tags,
            @Schema(name = "Текст сниппета",
                    description = "Основное содержимое публикуемого сниппета")
            @RequestBody String text) {
        String bearerToken = authService.getToken(headers);
        if (text != null && (text.isEmpty() || text.replaceAll("\\s", "").isEmpty())) {
            text = null;
        }
        return new ResponseEntity<>(publisherService.update(snippetId, bearerToken, hidden, title, tags, text), HttpStatus.OK);
    }

}
