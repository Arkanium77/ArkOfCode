package team.isaz.ark.backup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import team.isaz.ark.backup.constants.Roles;
import team.isaz.ark.backup.constants.Status;
import team.isaz.ark.backup.dto.TokenCheck;
import team.isaz.ark.backup.repository.rest.UserServiceClient;
import team.isaz.ark.libs.sinsystem.model.ArkOfSinCodes;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserServiceClient userServiceClient;

    private String getToken(HttpHeaders headers) {
        try {
            return Objects.requireNonNull(headers.get("Authorization")).get(0);
        } catch (Throwable t) {
            throw new AuthenticationSin("Can't extract authorization token from header");
        }
    }

    private TokenCheck checkToken(String bearerToken) {
        return userServiceClient.checkToken(bearerToken);
    }

    public void checkAdmin(HttpHeaders headers) {
        String bearerToken = getToken(headers);
        TokenCheck t = checkToken(bearerToken);
        if (Status.ERROR.equals(t.getStatus()) || Roles.USER.equals(t.getRole())) {
            throw new AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11001);
        }
    }
}
