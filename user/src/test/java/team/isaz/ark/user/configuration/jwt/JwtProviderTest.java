package team.isaz.ark.user.configuration.jwt;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import team.isaz.ark.user.constants.Status;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;

import java.util.UUID;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "accessTokenLifetime", 1);
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenLifetime", 30);
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", "jwtsecretwordjwtsecretwordjwtsecretword12");
    }

    @Test
    void shouldGenerateAndParseTokens() {
        UserEntity userEntity = UserEntity.builder()
                .login("captain")
                .role(RoleEntity.builder().name("ROLE_USER").build())
                .tokenVerifyCode(UUID.randomUUID())
                .build();

        String accessToken = jwtProvider.generateTokens(userEntity).getAccessToken();
        String refreshToken = jwtProvider.generateTokens(userEntity).getRefreshToken();

        Assertions.assertThat(jwtProvider.validateToken(accessToken)).isTrue();
        Assertions.assertThat(jwtProvider.isThatAccessToken(accessToken)).isTrue();
        Assertions.assertThat(jwtProvider.isThatAccessToken(refreshToken)).isFalse();
        Assertions.assertThat(jwtProvider.getLoginFromToken(accessToken)).isEqualTo("captain");
        Assertions.assertThat(jwtProvider.getTokenVerifyCode(accessToken)).isEqualTo(userEntity.getTokenVerifyCode());
        Assertions.assertThat(jwtProvider.getInfoFromToken(accessToken).getStatus()).isEqualTo(Status.OK);
    }

    @Test
    void shouldRejectBrokenToken() {
        Assertions.assertThat(jwtProvider.validateToken("broken")).isFalse();
        Assertions.assertThat(jwtProvider.getInfoFromToken("broken").getStatus()).isEqualTo(Status.ERROR);
    }
}
