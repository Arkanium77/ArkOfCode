package team.isaz.ark.libs.sinsystem.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ArkOfSinCodes {
    @Getter
    @AllArgsConstructor
    public enum InternalErrorCode {
        ERR_CODE_10000("10000", "Произошла непредвиденная ошибка", HttpStatus.INTERNAL_SERVER_ERROR);
        private final String value;
        private final String message;
        private final HttpStatus status;
    }

    @Getter
    @AllArgsConstructor
    public enum AuthenticationErrorCode {
        ERR_CODE_11000("11000", "Ошибка аутентификации", HttpStatus.UNAUTHORIZED);
        private final String value;
        private final String message;
        private final HttpStatus status;
    }


    @Getter
    @AllArgsConstructor
    public enum ValidationErrorCode {
        ERR_CODE_12000("12000", "Ошибка валидации", HttpStatus.BAD_REQUEST);
        private final String value;
        private final String message;
        private final HttpStatus status;
    }

}
