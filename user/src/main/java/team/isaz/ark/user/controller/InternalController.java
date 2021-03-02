package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.constants.Status;
import team.isaz.ark.user.dto.TokenCheck;

@RestController
@RequestMapping("/internal")
@Tag(name = "Внутренний контроллер",
        description = "Внутренний контроллер для обращений микросервисов")
@AllArgsConstructor
public class InternalController {
    private final JwtProvider jwt;

    @GetMapping("/bearer/login")
    @Operation(
            summary = "Проверка токена и получение логина",
            description = "Проверка токена и получение логина"
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public TokenCheck getLogin(String bearerToken) {
        if (bearerToken != null && jwt.validateToken(bearerToken) && jwt.isThatAccessToken(bearerToken)) {
            return TokenCheck.builder()
                    .status(Status.OK)
                    .login(jwt.getLoginFromToken(bearerToken))
                    .build();
        }
        return TokenCheck.builder()
                .status(Status.ERROR)
                .build();
    }
}
