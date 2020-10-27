package team.isaz.ark.user.configuration.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.isaz.ark.user.aop.annotation.PrepareToken;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.entity.UserEntity;

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

    private String getToken(String login, String role, String tokenVerifyCode, int lifetime, boolean isAccessToken) {
        return Jwts.builder()
                .claim("login", login)
                .claim("role", role)
                .claim("token_verify_code", tokenVerifyCode)
                .claim("token_type", isAccessToken)
                .setExpiration(Date.from(LocalDate.now().plusDays(lifetime).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    @PrepareToken
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("invalid token");
        }
        return false;
    }

    @PrepareToken
    public Boolean isThatAccessToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return (Boolean) claims.get("token_type");
    }

    @PrepareToken
    public String getLoginFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return (String) claims.get("login");
    }

    @PrepareToken
    public UUID getTokenVerifyCode(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return UUID.fromString((String) claims.get("token_verify_code"));
    }


}
