package team.isaz.ark.sinsystem.model.sin;

import lombok.Getter;
import team.isaz.ark.sinsystem.model.ArkOfSinCodes;

@Getter
public class InternalSin extends Sin {

    public InternalSin(Throwable ex) {
        super(ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getStatus(), ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getValue(),
                throwableToString(ex),
                ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getMessage());
    }

    public InternalSin(String message) {
        super(ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getStatus(), ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getValue(),
                message,
                ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000.getMessage());
    }

    public InternalSin(ArkOfSinCodes.InternalErrorCode code, String message) {
        super(code.getStatus(), code.getValue(), message, code.getMessage());
    }

}
