package team.isaz.ark.sinsystem.sin;


import lombok.AllArgsConstructor;
import lombok.Getter;

public class ArkOfSinCodes {
    @Getter
    @AllArgsConstructor
    public enum InternalErrorCode {
        ERR_CODE_10000("10000");
        private final String value;
    }

    @Getter
    @AllArgsConstructor
    public enum AuthenticationErrorCode {
        ERR_CODE_11000("11000");
        private final String value;
    }


    @Getter
    @AllArgsConstructor
    public enum ValidationErrorCode {
        ERR_CODE_12000("12000");
        private final String value;
    }

}
