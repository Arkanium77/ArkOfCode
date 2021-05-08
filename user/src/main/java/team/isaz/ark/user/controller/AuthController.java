package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.service.main.AccountService;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/public")
@Tag(name = "Контроллер доступа",
        description = "Используется для регистрации, авторизации, продления токенов")
@RequiredArgsConstructor
public class AuthController {
    private final AccountService accountService;

    @PostMapping("/register")
    @Operation(
            summary = "Метод регистрации",
            description = "Метод, используемый для регистрации нового аккаунта"
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> registerUser(@Valid
                                               @RequestBody
                                               @Schema(required = true,
                                                       description = "Информация о пользователе (логин, пароль)")
                                                       UserInfo userInfo) {
        UserEntity userEntity = accountService.registerUser(userInfo);
        return ResponseEntity.ok(userEntity.getLogin() + ", registration success! Now auth with your password.");
    }

    @PostMapping("/auth")
    @Operation(
            summary = "Метод аутентификации",
            description = "Метод, используемый для \"входа\" в существующий аккаунт и получения пары токенов аутентификации."
    )
    @ApiResponse(responseCode = "200", description = "Json с парой токенов (access и refresh)")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<?> auth(@Valid
                                  @RequestBody
                                  @Schema(required = true,
                                          description = "Информация о пользователе (логин, пароль)")
                                          UserInfo request) {
        Tokens tokens = accountService.login(request);
        return tokens == null ?
                new ResponseEntity<>("Login failed! Try again!", HttpStatus.UNAUTHORIZED) :
                ResponseEntity.ok(tokens);
    }

    @GetMapping("/refresh")
    @Operation(
            summary = "Метод восстановления токена",
            description = "Метод, используемый для получения новой пары токенов по актуальному refresh-токену"
    )
    @ApiResponse(responseCode = "200", description = "Json с парой токенов (access и refresh)")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<Tokens> registerUser(@RequestParam
                                               @Schema(required = true,
                                                       description = "Актуальный refresh-токен")
                                                       String refreshToken) {
        Tokens tokens = accountService.refreshToken(refreshToken);
        return ResponseEntity.ok(tokens);
    }
}
