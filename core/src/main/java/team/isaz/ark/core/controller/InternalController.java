package team.isaz.ark.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.service.AuthService;
import team.isaz.ark.core.service.InternalOperationService;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {
    private final InternalOperationService internalOperationService;
    private final AuthService authService;

    @Operation(
            summary = "Обновление имени автора",
            description = "При смене логина автора этот метод обновляет все сниппеты новым значением имени. Только для сервисов",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/snippets/login")
    public ResponseEntity<Status> updateLogin(@RequestHeader HttpHeaders headers, @RequestParam String login,
                                              @RequestParam String newLogin) {
        String bearerToken = authService.getToken(headers);
        internalOperationService.updateLogin(login, newLogin, bearerToken);
        return new ResponseEntity<>(Status.OK, HttpStatus.OK);
    }

}
