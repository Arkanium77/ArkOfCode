package team.isaz.ark.user.service.main;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team.isaz.ark.libs.sinsystem.model.sin.InternalSin;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.constants.Roles;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.RoleEntityRepository;
import team.isaz.ark.user.repository.UserEntityRepository;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private RoleEntityRepository roleEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    void shouldRegisterNewUser() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain_1");
        userInfo.setPassword("secret_1");
        RoleEntity role = RoleEntity.builder().name(Roles.USER).build();
        UserEntity saved = UserEntity.builder().id(1L).login("captain_1").role(role).build();
        Mockito.when(roleEntityRepository.findById(Roles.USER)).thenReturn(Optional.of(role));
        Mockito.when(passwordEncoder.encode("secret_1")).thenReturn("encoded");
        Mockito.when(userEntityRepository.save(Mockito.any(UserEntity.class))).thenReturn(saved);

        Assertions.assertThat(accountService.registerUser(userInfo)).isSameAs(saved);
    }

    @Test
    void shouldRejectDuplicateLoginOnRegister() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain_1");
        userInfo.setPassword("secret_1");
        Mockito.when(roleEntityRepository.findById(Roles.USER)).thenReturn(Optional.of(RoleEntity.builder().name(Roles.USER).build()));
        Mockito.when(userEntityRepository.existsByLogin("captain_1")).thenReturn(true);

        Assertions.assertThatThrownBy(() -> accountService.registerUser(userInfo))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectRegisterWithoutUserRole() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain_1");
        userInfo.setPassword("secret_1");
        Mockito.when(roleEntityRepository.findById(Roles.USER)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> accountService.registerUser(userInfo))
                .isInstanceOf(InternalSin.class);
    }

    @Test
    void shouldLoginUserWhenPasswordMatches() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder()
                .login("captain_1")
                .password("encoded")
                .role(RoleEntity.builder().name(Roles.USER).build())
                .tokenVerifyCode(UUID.randomUUID())
                .build();
        Tokens tokens = Tokens.builder().accessToken("access").refreshToken("refresh").build();
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain_1");
        userInfo.setPassword("secret_1");
        Mockito.when(userEntityRepository.findByLogin("captain_1")).thenReturn(Optional.of(userEntity));
        Mockito.when(passwordEncoder.matches("secret_1", "encoded")).thenReturn(true);
        Mockito.when(jwtProvider.generateTokens(userEntity)).thenReturn(tokens);

        Assertions.assertThat(accountService.login(userInfo)).isSameAs(tokens);
    }

    @Test
    void shouldReturnNullWhenUserIsBanned() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().login("captain_1").password("encoded").role(RoleEntity.builder().name(Roles.USER).build()).build().withUserBanned(true);
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain_1");
        userInfo.setPassword("secret_1");
        Mockito.when(userEntityRepository.findByLogin("captain_1")).thenReturn(Optional.of(userEntity));
        Mockito.when(passwordEncoder.matches("secret_1", "encoded")).thenReturn(true);

        Assertions.assertThat(accountService.login(userInfo)).isNull();
    }

    @Test
    void shouldReturnNullWhenPasswordDoesNotMatch() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().login("captain_1").password("encoded").build();
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain_1");
        userInfo.setPassword("secret_1");
        Mockito.when(userEntityRepository.findByLogin("captain_1")).thenReturn(Optional.of(userEntity));
        Mockito.when(passwordEncoder.matches("secret_1", "encoded")).thenReturn(false);

        Assertions.assertThat(accountService.login(userInfo)).isNull();
    }

    @Test
    void shouldReturnNullWhenUserMissingOnLogin() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain_1");
        userInfo.setPassword("secret_1");
        Mockito.when(userEntityRepository.findByLogin("captain_1")).thenReturn(Optional.empty());

        Assertions.assertThat(accountService.login(userInfo)).isNull();
    }

    @Test
    void shouldPerformInternalLogin() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().login("_tech").role(RoleEntity.builder().name("ROLE_SERVICE").build()).tokenVerifyCode(UUID.randomUUID()).build();
        Tokens tokens = Tokens.builder().accessToken("access").refreshToken("refresh").build();
        Mockito.when(userEntityRepository.findByLogin("_tech")).thenReturn(Optional.of(userEntity));
        Mockito.when(jwtProvider.generateTokens(userEntity)).thenReturn(tokens);

        Assertions.assertThat(accountService.internalLogin("_tech")).isSameAs(tokens);
    }

    @Test
    void shouldRejectInternalLoginForUnknownUser() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        Mockito.when(userEntityRepository.findByLogin("_tech")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> accountService.internalLogin("_tech"))
                .isInstanceOf(InternalSin.class);
    }

    @Test
    void shouldFindByLogin() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().login("captain_1").build();
        Mockito.when(userEntityRepository.findByLogin("captain_1")).thenReturn(Optional.of(userEntity));

        Assertions.assertThat(accountService.findByLogin("captain_1")).contains(userEntity);
    }

    @Test
    void shouldRejectInvalidRefreshToken() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        Mockito.when(jwtProvider.validateToken("refresh")).thenReturn(false);

        Assertions.assertThatThrownBy(() -> accountService.refreshToken("refresh"))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("not valid token");
    }

    @Test
    void shouldRejectAccessTokenOnRefresh() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        Mockito.when(jwtProvider.validateToken("refresh")).thenReturn(true);
        Mockito.when(jwtProvider.isThatAccessToken("refresh")).thenReturn(true);

        Assertions.assertThatThrownBy(() -> accountService.refreshToken("refresh"))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("not refresh token");
    }

    @Test
    void shouldRefreshTokenPair() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().login("captain_1").role(RoleEntity.builder().name(Roles.USER).build()).tokenVerifyCode(UUID.randomUUID()).build();
        Tokens tokens = Tokens.builder().accessToken("access").refreshToken("refresh-new").build();
        Mockito.when(jwtProvider.validateToken("refresh")).thenReturn(true);
        Mockito.when(jwtProvider.isThatAccessToken("refresh")).thenReturn(false);
        Mockito.when(jwtProvider.getLoginFromToken("refresh")).thenReturn("captain_1");
        Mockito.when(userEntityRepository.findByLogin("captain_1")).thenReturn(Optional.of(userEntity));
        Mockito.when(jwtProvider.generateTokens(userEntity)).thenReturn(tokens);

        Assertions.assertThat(accountService.refreshToken("refresh")).isSameAs(tokens);
    }

    @Test
    void shouldRejectRefreshForUnknownUser() {
        AccountService accountService = new AccountService(userEntityRepository, roleEntityRepository, passwordEncoder, jwtProvider);
        Mockito.when(jwtProvider.validateToken("refresh")).thenReturn(true);
        Mockito.when(jwtProvider.isThatAccessToken("refresh")).thenReturn(false);
        Mockito.when(jwtProvider.getLoginFromToken("refresh")).thenReturn("captain_1");
        Mockito.when(userEntityRepository.findByLogin("captain_1")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> accountService.refreshToken("refresh"))
                .isInstanceOf(InternalSin.class);
    }
}
