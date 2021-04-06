package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.constants.RegexPatterns;
import team.isaz.ark.user.service.main.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@Validated
@RestController
@RequestMapping("/secured")
@Tag(name = "Контроллер пользовательских функций",
     description = "Используется для действий над аккаунтом авторизованного пользователя (Смена пароля, удаление аккаунта и т.д.).")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/change_login")
    @Operation(
            summary = "Метод смены логина",
            description = "Метод, используемый для смены логина владельца токена аутентификации",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> changeLogin(
            @Schema(required = true,
                    description = "Строка, содержащая новый логин. Должен содержать от 6 до 25 символов. Разрешено использовать только латинские буквы (в любом регистре) и символ подчеркивания")
            @Valid @RequestParam @Pattern(regexp = RegexPatterns.LOGIN,
                    message = "Login must contain from 6 to 25 characters. Allowed to use only Latin letters (in any case) and the underscore character")
                    String newLogin,
            @RequestHeader HttpHeaders headers) {
        String token = Objects.requireNonNull(headers.get("Authorization")).get(0);
        userService.changeLogin(token, newLogin);
        return new ResponseEntity<>("Login has been successfully changed. All old tokens have been recalled. Re-authorize.", HttpStatus.OK);
    }

    @PutMapping("/change_password")
    @Operation(
            summary = "Метод смены пароля",
            description = "Метод, используемый для смены пароля владельца токена аутентификации",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")

    public ResponseEntity<String> changePassword(
            @Schema(required = true,
                    description = "Строка содержащая новый пароль. Должен содержать от 6 до 50 символов. Можно использовать любые символы, кроме пробелообразных.")
            @Valid @RequestParam @Pattern(regexp = RegexPatterns.PASSWORD,
                    message = "Password should contain 6 to 50 characters. May use any characters except whitespace-like characters.")
                    String newPassword,
            @RequestHeader HttpHeaders headers) {
        String token = Objects.requireNonNull(headers.get("Authorization")).get(0);
        userService.changePassword(token, newPassword);
        return new ResponseEntity<>("Password has been successfully changed. All old tokens have been recalled. Re-authorize.", HttpStatus.OK);
    }

    @DeleteMapping("/delete_account")
    @Operation(
            summary = "Метод удаления аккаунта",
            description = "Метод, используемый для удаления аккаунта владельца токена аутентификации",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> deleteAccount(@RequestHeader HttpHeaders headers) {
        String token = Objects.requireNonNull(headers.get("Authorization")).get(0);
        userService.deleteAccount(token);
        return new ResponseEntity<>("Your account has been successfully deleted! Thank you for being with us!", HttpStatus.OK);
    }

}
