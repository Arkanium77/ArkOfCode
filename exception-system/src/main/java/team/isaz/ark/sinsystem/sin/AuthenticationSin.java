package team.isaz.ark.sinsystem.sin;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationSin extends Sin {
    public AuthenticationSin(String message, String localizedMessage, ArkOfSinCodes.AuthenticationErrorCode code) {
        super(HttpStatus.NOT_FOUND, code.getValue(), message, localizedMessage);
    }

    public AuthenticationSin(HttpStatus status, String message, String localizedMessage, ArkOfSinCodes.AuthenticationErrorCode code) {
        super(status, code.getValue(), message, localizedMessage);
    }

    public static AuthenticationSin forbiddenSin(String message) {
        return new AuthenticationSin(HttpStatus.FORBIDDEN, message, "У пользователя недостаточно прав", ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11000);
    }
}
