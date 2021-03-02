package team.isaz.ark.libs.sinsystem.model.sin;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class Sin extends RuntimeException {
    private final HttpStatus status;
    private final String arkErrorCode;
    private final String localizedMessage;

    public Sin(HttpStatus status, String arkErrorCode, String message, String localizedMessage) {
        super(message);
        this.status = status;
        this.arkErrorCode = arkErrorCode;
        this.localizedMessage = localizedMessage;
    }

    protected static String throwableToString(Throwable t) {
        return t.getClass().getSimpleName() + ": " + t.getMessage() +
                (t.getCause() == null ? "" : ", cause: " + t.getCause());
    }
}
