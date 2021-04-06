package team.isaz.ark.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.repository.rest.UserServiceClient;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    UserServiceClient userServiceClient;

    public String getToken(HttpHeaders headers) {
        try {
            return Objects.requireNonNull(headers.get("Authorization")).get(0);
        } catch (Throwable t) {
            throw new AuthenticationSin("Can't extract authorization token from header");
        }
    }

    public TokenCheck getLogin(String bearerToken) {
        return userServiceClient.getLogin(bearerToken);
    }
}
