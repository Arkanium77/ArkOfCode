package team.isaz.ark.libs.sinsystem.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import team.isaz.ark.libs.sinsystem.model.sin.Sin;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SinDescription {
    private static final Logger log = LoggerFactory.getLogger(SinDescription.class);

    private final String timestamp;
    private final String path;
    private final String serviceCode;
    private final String httpErrorCode;
    private final String arkErrorCode;
    private final String localizedMessage;
    private final String message;

    public SinDescription(Sin ex, String serviceCode, WebRequest request) {
        this.serviceCode = serviceCode;
        this.arkErrorCode = ex.getArkErrorCode();
        this.httpErrorCode = String.valueOf(ex.getStatus().value());
        this.localizedMessage = ex.getLocalizedMessage();
        this.message = ex.getMessage();
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.path = getPath(request);
    }

    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public String getServiceCode() { return serviceCode; }
    public String getHttpErrorCode() { return httpErrorCode; }
    public String getArkErrorCode() { return arkErrorCode; }
    public String getLocalizedMessage() { return localizedMessage; }
    public String getMessage() { return message; }

    private String getPath(WebRequest r) {
        try {
            ServletWebRequest r1 = (ServletWebRequest) r;
            return ((HttpServletRequest) r1.getNativeRequest()).getRequestURI();
        } catch (Throwable t) {
            log.error("Error when trying get http-request uri: {}", t.getMessage());
            return "unknown";
        }
    }

    @Override
    public String toString() {
        return "SinDescription{" +
                "timestamp='" + timestamp + '\'' +
                ", path='" + path + '\'' +
                ", serviceCode='" + serviceCode + '\'' +
                ", httpErrorCode='" + httpErrorCode + '\'' +
                ", arkErrorCode='" + arkErrorCode + '\'' +
                ", localizedMessage='" + localizedMessage + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
