package team.isaz.ark.sinsystem.sin;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Getter
public class ValidationSin extends Sin {

    public ValidationSin(String message, String localizedMessage, ArkOfSinCodes.ValidationErrorCode code) {
        super(HttpStatus.BAD_REQUEST, code.getValue(), message, localizedMessage);
    }

    public ValidationSin(HttpStatus status, String message, String localizedMessage, ArkOfSinCodes.ValidationErrorCode code) {
        super(status, code.getValue(), message, localizedMessage);
    }

    public static ValidationSin defaultValidationException(Exception e) {
        return new ValidationSin("Request validation failed! " + throwableToString(e),
                "Обнаружена ошибка при проведении форматно-логического контроля",
                ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000);
    }

    public static ValidationSin defaultValidationException(String messagePrefix, Exception e) {
        return new ValidationSin(messagePrefix + " " + throwableToString(e),
                "Обнаружена ошибка при проведении форматно-логического контроля",
                ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000);
    }

    public static ValidationSin autoValidationException(MethodArgumentNotValidException exception) {
        return new ValidationSin(exception.getMessage(),
                "Запрос не соответствует формату",
                ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000);
    }

}
