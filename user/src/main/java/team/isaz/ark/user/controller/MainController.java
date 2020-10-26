package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main")
public class MainController {


    @GetMapping("/get")
    @Operation(
            summary = "Тест",
            description = "Просто метод возвращающий ваш текст"
    )
    @ApiResponse(responseCode = "200", description = "Текст")
    public ResponseEntity<String> get(@RequestParam String text) {
        return new ResponseEntity<>("That is your text: " + text, HttpStatus.OK);
    }

}
