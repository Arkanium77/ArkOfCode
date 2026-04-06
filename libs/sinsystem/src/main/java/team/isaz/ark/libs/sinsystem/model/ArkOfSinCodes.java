package team.isaz.ark.libs.sinsystem.model;

import org.springframework.http.HttpStatus;

public class ArkOfSinCodes {

    public enum InternalErrorCode {
        ERR_CODE_10000("10000", "Произошла непредвиденная ошибка", HttpStatus.INTERNAL_SERVER_ERROR);

        private final String value;
        private final String message;
        private final HttpStatus status;

        InternalErrorCode(String value, String message, HttpStatus status) {
            this.value = value;
            this.message = message;
            this.status = status;
        }

        public String getValue() { return value; }
        public String getMessage() { return message; }
        public HttpStatus getStatus() { return status; }
    }

    public enum AuthenticationErrorCode {
        ERR_CODE_11000("11000", "Ошибка аутентификации.", HttpStatus.UNAUTHORIZED),
        ERR_CODE_11001("11001", "Не удалось получить логин по токену авторизации. Перелогиньтесь и попробуйте снова.",
                HttpStatus.UNAUTHORIZED);

        private final String value;
        private final String message;
        private final HttpStatus status;

        AuthenticationErrorCode(String value, String message, HttpStatus status) {
            this.value = value;
            this.message = message;
            this.status = status;
        }

        public String getValue() { return value; }
        public String getMessage() { return message; }
        public HttpStatus getStatus() { return status; }
    }

    public enum ValidationErrorCode {
        ERR_CODE_12000("12000", "Ошибка валидации.", HttpStatus.BAD_REQUEST);

        private final String value;
        private final String message;
        private final HttpStatus status;

        ValidationErrorCode(String value, String message, HttpStatus status) {
            this.value = value;
            this.message = message;
            this.status = status;
        }

        public String getValue() { return value; }
        public String getMessage() { return message; }
        public HttpStatus getStatus() { return status; }
    }
}
