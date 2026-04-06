package team.isaz.ark.core.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.repository.rest.UserServiceClient;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Test
    void shouldExtractTokenFromHeader() {
        AuthService authService = new AuthService(userServiceClient);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer token");

        Assertions.assertThat(authService.getToken(headers)).isEqualTo("Bearer token");
    }

    @Test
    void shouldRejectMissingAuthorizationHeader() {
        AuthService authService = new AuthService(userServiceClient);

        Assertions.assertThatThrownBy(() -> authService.getToken(new HttpHeaders()))
                .isInstanceOf(AuthenticationSin.class)
                .hasMessageContaining("authorization token");
    }

    @Test
    void shouldDelegateTokenCheck() {
        AuthService authService = new AuthService(userServiceClient);
        TokenCheck tokenCheck = new TokenCheck(Status.OK, "captain", "ROLE_USER");
        Mockito.when(userServiceClient.checkToken("Bearer token")).thenReturn(tokenCheck);

        Assertions.assertThat(authService.checkToken("Bearer token")).isSameAs(tokenCheck);
    }

    @Test
    void shouldReturnLoginForValidToken() {
        AuthService authService = new AuthService(userServiceClient);
        Mockito.when(userServiceClient.checkToken("Bearer token"))
                .thenReturn(new TokenCheck(Status.OK, "captain", "ROLE_USER"));

        Assertions.assertThat(authService.getLogin("Bearer token")).isEqualTo("captain");
    }

    @Test
    void shouldRejectTokenWithErrorStatus() {
        AuthService authService = new AuthService(userServiceClient);
        Mockito.when(userServiceClient.checkToken("Bearer token"))
                .thenReturn(new TokenCheck(Status.ERROR, null, null));

        Assertions.assertThatThrownBy(() -> authService.getLogin("Bearer token"))
                .isInstanceOf(AuthenticationSin.class);
    }
}
