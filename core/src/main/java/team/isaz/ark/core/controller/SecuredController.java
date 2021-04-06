package team.isaz.ark.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.service.AuthService;
import team.isaz.ark.core.service.PublisherService;
import team.isaz.ark.core.service.SearchService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
        return ResponseEntity.ok(authService.getLogin(bearerToken));
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
            summary = "Публикация сниппета",
            description = "Публикация сниппета (для авторизованного пользователя)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/publish")
    public ResponseEntity<Status> publish(
            @RequestHeader HttpHeaders headers,
            @RequestParam(name = "Статус сокрытия") boolean hidden,
            @RequestParam(name = "Название сниппета") @NotBlank String title,
            @RequestParam(name = "Теги") @Valid @NotNull Set<@NotBlank String> tags,
            @Schema(name = "Текст сниппета",
                    description = "Основное содержимое публикуемого сниппета", required = true)
            @RequestBody @NotBlank String text) {
        String bearerToken = authService.getToken(headers);
        return new ResponseEntity<>(publisherService.publish(bearerToken, hidden, title, tags, text), HttpStatus.OK);
    }

}
