package team.isaz.ark.libs.sinsystem.model;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import team.isaz.ark.libs.sinsystem.model.sin.Sin;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Getter
@ToString
public class SinDescription {
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

    private String getPath(WebRequest r) {
        try {
            ServletWebRequest r1 = (ServletWebRequest) r;
            return ((HttpServletRequest) r1.getNativeRequest()).getRequestURI();
        } catch (Throwable t) {
            log.error("Error when trying get http-request uri: {}", t.getMessage());
            return "unknown";
        }
    }
}

