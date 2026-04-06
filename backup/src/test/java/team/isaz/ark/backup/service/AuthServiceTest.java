package team.isaz.ark.backup.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import team.isaz.ark.backup.constants.Status;
import team.isaz.ark.backup.dto.TokenCheck;
import team.isaz.ark.backup.repository.rest.UserServiceClient;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Test
    void shouldAllowAdminToken() {
        AuthService authService = new AuthService(userServiceClient);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer admin");
        Mockito.when(userServiceClient.checkToken("Bearer admin"))
                .thenReturn(new TokenCheck(Status.OK, "root", "ROLE_ADMIN"));

        authService.checkAdmin(headers);

        Mockito.verify(userServiceClient).checkToken("Bearer admin");
    }

    @Test
    void shouldRejectMissingAuthorizationHeader() {
        AuthService authService = new AuthService(userServiceClient);

        Assertions.assertThatThrownBy(() -> authService.checkAdmin(new HttpHeaders()))
                .isInstanceOf(AuthenticationSin.class)
                .hasMessageContaining("authorization token");
    }

    @Test
    void shouldRejectUserRole() {
        AuthService authService = new AuthService(userServiceClient);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer user");
        Mockito.when(userServiceClient.checkToken("Bearer user"))
                .thenReturn(new TokenCheck(Status.OK, "captain", "ROLE_USER"));

        Assertions.assertThatThrownBy(() -> authService.checkAdmin(headers))
                .isInstanceOf(AuthenticationSin.class);
    }

    @Test
    void shouldRejectErrorStatus() {
        AuthService authService = new AuthService(userServiceClient);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer invalid");
        Mockito.when(userServiceClient.checkToken("Bearer invalid"))
                .thenReturn(new TokenCheck(Status.ERROR, "root", "ROLE_ADMIN"));

        Assertions.assertThatThrownBy(() -> authService.checkAdmin(headers))
                .isInstanceOf(AuthenticationSin.class);
    }
}
