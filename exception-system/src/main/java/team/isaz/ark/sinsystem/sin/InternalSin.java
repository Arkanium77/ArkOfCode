package team.isaz.ark.sinsystem.sin;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InternalSin extends Sin {

    public InternalSin(Throwable ex) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getValue(),
                throwableToString(ex),
                "Произошла непредвиденная ошибка");
    }

    public InternalSin(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getValue(),
                message,
                "Произошла непредвиденная ошибка");
    }

    public InternalSin(String message, String localizedMessage, ArkOfSinCodes.InternalErrorCode code) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, code.getValue(), message, localizedMessage);
    }

}
