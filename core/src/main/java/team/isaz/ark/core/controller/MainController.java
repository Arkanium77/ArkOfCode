package team.isaz.ark.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.service.AuthService;
import team.isaz.ark.core.service.PublisherService;
import team.isaz.ark.core.service.SearchService;

import java.util.List;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/main")
@AllArgsConstructor
public class MainController {
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
