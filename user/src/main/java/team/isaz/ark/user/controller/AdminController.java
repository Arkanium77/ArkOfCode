package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.constants.RegexPatterns;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.service.main.AdminService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/hidden")
@Tag(name = "Контроллер административных функций",
     description = "Используется для действий над аккаунтами других пользователей/администраторов.")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/id/{login}")
    @Operation(
            summary = "Получение идентификатора пользователя",
            description = "Метод, используемый для получения идентификатора пользователя по логину.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> delete(
            @Schema(required = true,
                    description = "Идентификатор пользователя")
            @Valid @PathVariable @Pattern(regexp = RegexPatterns.LOGIN,
                    message = "Login must contain from 6 to 25 characters. Allowed to use only Latin letters (in any case) and the underscore character")
                    String login) {
        return new ResponseEntity<>(adminService.getId(login).toString(), HttpStatus.OK);
    }

    @PutMapping("/{id}/ban")
    @Operation(
            summary = "Забанить пользователя",
            description = "Метод, используемый для бана пользователя по id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> ban(
            @Schema(required = true,
                    description = "Идентификатор пользователя")
            @Valid @PathVariable @NotNull @Positive Long id) {
        adminService.banAccount(id);
        return new ResponseEntity<>("User successful banned!", HttpStatus.OK);
    }

    @PutMapping("/{id}/unban")
    @Operation(
            summary = "Разбанить пользователя",
            description = "Метод, используемый для разбана пользователя по id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> unBan(
            @Schema(required = true,
                    description = "Идентификатор пользователя")
            @Valid @PathVariable @NotNull @Positive Long id) {
        adminService.unBanAccount(id);
        return new ResponseEntity<>("User successful unbanned!", HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete")
    @Operation(
            summary = "Удалить пользователя",
            description = "Метод, используемый для удаления пользователя по id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> delete(
            @Schema(required = true,
                    description = "Идентификатор пользователя")
            @Valid @PathVariable @NotNull @Positive Long id) {
        adminService.deleteAccount(id);
        return new ResponseEntity<>("User successful deleted!", HttpStatus.OK);
    }

    @PutMapping("/{id}/promote")
    @Operation(
            summary = "Дать полномочия администратора",
            description = "Метод, используемый для продвижения пользователя до роли администратора по id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> promote(
            @Schema(required = true,
                    description = "Идентификатор пользователя")
            @Valid @PathVariable @NotNull @Positive Long id) {
        adminService.promoteAccount(id);
        return new ResponseEntity<>("User successful promoted!", HttpStatus.OK);
    }

    @PutMapping("/{id}/demote")
    @Operation(
            summary = "Отнять полномочия администратора",
            description = "Метод, используемый для понижения пользователя до роли стандартных полномочий по id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> demote(
            @Schema(required = true,
                    description = "Идентификатор пользователя")
            @Valid @PathVariable @NotNull @Positive Long id) {
        adminService.demoteAccount(id);
        return new ResponseEntity<>("User successful demoted!", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить данные пользователя",
            description = "Метод, используемый для принудительной смены логина/пароля пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> update(
            @Schema(required = true,
                    description = "Идентификатор пользователя")
            @Valid @PathVariable @NotNull @Positive Long id,
            @Valid
            @RequestBody
            @Schema(required = true,
                    description = "Информация о пользователе (логин, пароль)") UserInfo userInfo) {
        adminService.changeUserData(id, userInfo);
        return new ResponseEntity<>("User data successful updated!", HttpStatus.OK);
    }

}
