package team.isaz.ark.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.isaz.ark.core.constants.RegexPatterns;

import javax.validation.constraints.Pattern;

@Data
public class UserInfo {

    @Schema(title = "Логин", description = "Логин пользователя. Должен содержать от 6 до 25 символов. Разрешено использовать только латинские буквы (в любом регистре) и символ подчеркивания")
    @Pattern(regexp = RegexPatterns.LOGIN,
            message = "Login must contain from 6 to 25 characters. Allowed to use only Latin letters (in any case) and the underscore character")
    private String login;

    @Schema(title = "Пароль", description = "Пароль должен содержать от 6 до 50 символов. Можно использовать любые символы, кроме пробелообразных.")
    @Pattern(regexp = RegexPatterns.PASSWORD,
            message = "Password should contain 6 to 50 characters. May use any characters except whitespace-like characters.")
    private String password;
}
