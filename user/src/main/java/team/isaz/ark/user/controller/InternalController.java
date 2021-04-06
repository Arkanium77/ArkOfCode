package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.configuration.jwt.JwtFilter;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.constants.Status;
import team.isaz.ark.user.dto.TokenCheck;

@RestController
@RequestMapping("/internal")
@Tag(name = "Внутренний контроллер",
        description = "Внутренний контроллер для обращений микросервисов")
@RequiredArgsConstructor
public class InternalController {
    private final JwtProvider jwt;
    private final JwtFilter jwtFilter;

    @GetMapping("/bearer/check")
    @Operation(
            summary = "Проверка токена и получение информации",
            description = "Проверка токена и получение информации"
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public TokenCheck check(String bearerToken) {
        if (jwtFilter.isTokenValid(bearerToken)) {
            return jwt.getInfoFromToken(bearerToken);
        }
        return TokenCheck.builder()
                .status(Status.ERROR)
                .build();
    }
}
