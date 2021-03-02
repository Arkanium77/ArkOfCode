package team.isaz.ark.libs.sinsystem.model.sin;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import team.isaz.ark.libs.sinsystem.model.ArkOfSinCodes;

@Getter
public class ValidationSin extends Sin {
    public ValidationSin(ArkOfSinCodes.ValidationErrorCode code) {
        super(code.getStatus(), code.getValue(), "", code.getMessage());
    }

    public ValidationSin(ArkOfSinCodes.ValidationErrorCode code, String message) {
        super(code.getStatus(), code.getValue(), message, code.getMessage());
    }

    public ValidationSin(String message) {
        super(ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000.getStatus(), ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000
                .getValue(), message, ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000.getMessage());
    }

    public ValidationSin(ArkOfSinCodes.ValidationErrorCode code, HttpStatus status, String message) {
        super(status, code.getValue(), message, code.getMessage());
    }

    public ValidationSin(MethodArgumentNotValidException exception) {
        super(ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000.getStatus(),
                ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000.getValue(),
                exception.getMessage(),
                ArkOfSinCodes.ValidationErrorCode.ERR_CODE_12000.getMessage());
    }

}
