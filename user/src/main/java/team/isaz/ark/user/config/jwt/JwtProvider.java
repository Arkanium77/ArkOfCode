package team.isaz.ark.user.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.isaz.ark.user.aop.annotation.PrepareToken;
import team.isaz.ark.user.constants.Status;
import team.isaz.ark.user.dto.TokenCheck;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.entity.UserEntity;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {
    @Value("${jwt.token.lifetime.access}")
    private Integer accessTokenLifetime;
    @Value("${jwt.token.lifetime.refresh}")
    private Integer refreshTokenLifetime;
    @Value("${jwt.secret}")
    private String jwtSecret;

    public Tokens generateTokens(UserEntity entity) {
        return Tokens.builder()
                .accessToken(getAccessToken(entity.getLogin(), entity.getRole().getName(), entity.getTokenVerifyCode().toString()))
                .refreshToken(getRefreshToken(entity.getLogin(), entity.getRole().getName(), entity.getTokenVerifyCode().toString()))
                .build();
    }

    private String getAccessToken(String login, String role, String tokenVerifyCode) {
        return getToken(login, role, tokenVerifyCode, accessTokenLifetime, true);
    }

    private String getRefreshToken(String login, String role, String tokenVerifyCode) {
        return getToken(login, role, tokenVerifyCode, refreshTokenLifetime, false);
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String getToken(String login, String role, String tokenVerifyCode, int lifetime, boolean isAccessToken) {
        return Jwts.builder()
                .claim("login", login)
                .claim("role", role)
                .claim("token_verify_code", tokenVerifyCode)
                .claim("token_type", isAccessToken)
                .expiration(Date.from(LocalDate.now().plusDays(lifetime).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .signWith(getSecretKey())
                .compact();
    }

    @PrepareToken
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("invalid token");
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
    }

    @PrepareToken
    public Boolean isThatAccessToken(String token) {
        return (Boolean) getClaims(token).get("token_type");
    }

    @PrepareToken
    public String getLoginFromToken(String token) {
        return (String) getClaims(token).get("login");
    }

    @PrepareToken
    public UUID getTokenVerifyCode(String token) {
        return UUID.fromString((String) getClaims(token).get("token_verify_code"));
    }

    @PrepareToken
    public TokenCheck getInfoFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return TokenCheck.builder()
                    .status(Status.OK)
                    .login((String) claims.get("login"))
                    .role((String) claims.get("role"))
                    .build();
        } catch (Exception e) {
            return TokenCheck.builder().status(Status.ERROR).build();
        }
    }
}
