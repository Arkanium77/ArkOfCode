package team.isaz.ark.backup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.backup.dto.Response;
import team.isaz.ark.backup.service.AuthService;
import team.isaz.ark.backup.service.BackupService;

@RestController
@RequestMapping("/secured")
@RequiredArgsConstructor
public class SecuredController {
    private final BackupService backupService;
    private final AuthService authService;

    @Operation(
            summary = "Создание резервной копии",
            description = "Создание резервной копии хранилища сниппетов (для пользователя с рангом администратора)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/backup")
    public ResponseEntity<Response> backup(@RequestHeader HttpHeaders headers,
                                           @RequestParam(name = "Путь к папке резервного копирования", required = false)
                                                   String path) {
        authService.checkAdmin(headers);
        return new ResponseEntity<>(backupService.backup(path), HttpStatus.OK);
    }

    @Operation(
            summary = "Восстановление резервной копии",
            description = "Восстановление резервной копии хранилища сниппетов (для пользователя с рангом администратора)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/restore")
    public ResponseEntity<Response> restore(@RequestHeader HttpHeaders headers,
                                            @RequestParam(name = "Путь к папке резервного копирования", required = false)
                                                    String path) {
        authService.checkAdmin(headers);
        return new ResponseEntity<>(backupService.restore(path), HttpStatus.OK);
    }

}
