package team.isaz.ark.libs.sinsystem.model.sin;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.isaz.ark.libs.sinsystem.model.ArkOfSinCodes;

@Getter
public class AuthenticationSin extends Sin {
    public AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode code) {
        super(code.getStatus(), code.getValue(), "", code.getMessage());
    }

    public AuthenticationSin(String message) {
        super(ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11000.getStatus(), ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11000
                .getValue(), message, ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11000.getMessage());
    }

    public AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode code, String message) {
        super(code.getStatus(), code.getValue(), message, code.getMessage());
    }

    public AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode code, String message, HttpStatus status) {
        super(status, code.getValue(), message, code.getMessage());
    }
}
