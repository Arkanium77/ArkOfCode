package team.isaz.ark.core.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import team.isaz.ark.core.dto.ErrorInfo;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorInfo> unknownException(Exception ex, WebRequest req) {
        ErrorInfo info = ErrorInfo.builder()
                .name(ex.getClass().getSimpleName())
                .description(ex.getMessage()).build();
        return new ResponseEntity<>(info, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}