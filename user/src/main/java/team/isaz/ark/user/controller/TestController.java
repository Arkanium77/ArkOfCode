package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.sinsystem.sin.InternalSin;

import java.util.Random;
import javax.management.ReflectionException;

@RestController
@RequestMapping("/public")
@Tag(name = "Контроллер для тестирования",
        description = "Используется для публикации тестовых методов")
public class TestController {

    @SneakyThrows
    @GetMapping("/throw")
    @Operation(
            summary = "Получение выбрасывание рандомных исключений",
            description = "Метод, используемый для получения рандомных исключений"
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public ResponseEntity<String> throwIt() {
        Random r = new Random();
        int a = r.nextInt(5);
        switch (a) {
            case 0:
                throw new ArithmeticException("Арифметик ексепшон");
            case 1:
                throw new ArrayIndexOutOfBoundsException("-1 индекс не к добру");
            case 2:
                throw new ReflectionException(new RuntimeException("Evil"), "Рефлексия зло");
            case 3:
                throw new NullPointerException("вот  что я люблю");
            case 4:
                throw new RuntimeException("Хз чего");
        }
        return ResponseEntity.ok("Ok");
    }

    @GetMapping("/rand")
    @Operation(
            summary = "111111111111",
            description = "11111111111111111"
    )
    @ApiResponse(responseCode = "200", description = "Сообщение об успешном завершении операции")
    @ApiResponse(responseCode = "500", description = "Сообщение о типе и описании произошедшей ошибки")
    public InternalSin rand() {
        return new InternalSin("Sin!");
    }
}
