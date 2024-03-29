package team.isaz.ark.libs.sinsystem.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import team.isaz.ark.libs.sinsystem.model.SinDescription;
import team.isaz.ark.libs.sinsystem.model.sin.InternalSin;
import team.isaz.ark.libs.sinsystem.model.sin.Sin;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Value("${spring.application.name}")
    private String serviceCode;

    @ExceptionHandler(value = {Throwable.class})
    public ResponseEntity<SinDescription> handleThrowable(Throwable ex, WebRequest req) {
        log.trace("Caught {} with message {}", ex.getClass().getSimpleName(), ex.getMessage());
        log.error("Sin System handle and resolve error: ", ex);
        Sin e;
        if (ex instanceof Sin) {
            e = (Sin) ex;
        } else if (ex instanceof MethodArgumentNotValidException) {
            e = new ValidationSin((MethodArgumentNotValidException) ex);
        } else {
            e = new InternalSin(ex);
        }
        SinDescription response = new SinDescription(e, serviceCode, req);
        log.error("Sin discovered: {}", response);
        return new ResponseEntity<>(response, e.getStatus());
    }
}