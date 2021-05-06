package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/error")
@Tag(name = "Контроллер-заглушка",
        description = "Используется для переадресации внешних запросов ко внетренним методам")
@RequiredArgsConstructor
public class ErrorStubController {


    @GetMapping("")
    @Operation(
            summary = "Ответ о несанкционированном доступе",
            description = "В этот метод будут перенаправлены любые попытки обратиться к внутренним методам через гейтвей"
    )
    @ApiResponse(responseCode = "403", description = "Сообщение о попытке несанкционированного доступа")
    public ResponseEntity<String> error() {

        return new ResponseEntity<>("Попытка доступа к защищённому методу из внешнего контура!",
                HttpStatus.FORBIDDEN);
    }
}
