package team.isaz.ark.backup.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import team.isaz.ark.backup.dto.Response;
import team.isaz.ark.backup.service.AuthService;
import team.isaz.ark.backup.service.BackupService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupControllersTest {

    @Mock
    private BackupService backupService;
    @Mock
    private AuthService authService;

    private SecuredController securedController;
    private ErrorStubController errorStubController;

    @BeforeEach
    void setUp() {
        securedController = new SecuredController(backupService, authService);
        errorStubController = new ErrorStubController();
    }

    @Test
    void securedControllerShouldBackupAndRestore() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer admin");

        when(backupService.backup("/tmp/backup")).thenReturn(Response.ok("bkp"));
        when(backupService.restore("/tmp/backup")).thenReturn(Response.ok("rst"));

        ResponseEntity<Response> backupResponse = securedController.backup(headers, "/tmp/backup");
        ResponseEntity<Response> restoreResponse = securedController.restore(headers, "/tmp/backup");

        assertEquals(HttpStatus.OK, backupResponse.getStatusCode());
        assertEquals("bkp", backupResponse.getBody().getDescription());
        assertEquals(HttpStatus.OK, restoreResponse.getStatusCode());
        assertEquals("rst", restoreResponse.getBody().getDescription());

        verify(authService, times(2)).checkAdmin(headers);
        verify(backupService).backup("/tmp/backup");
        verify(backupService).restore("/tmp/backup");
    }

    @Test
    void errorStubShouldReturnForbidden() {
        ResponseEntity<String> response = errorStubController.error();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Попытка доступа к защищённому методу из внешнего контура!", response.getBody());
    }
}
