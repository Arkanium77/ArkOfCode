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
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.UserEntityRepository;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private SnippetVaultService snippetVaultService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    void shouldChangeLogin() {
        UserService userService = new UserService(userEntityRepository, snippetVaultService, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().id(1L).login("captain").tokenVerifyCode(UUID.randomUUID()).build();
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(userEntityRepository.findByLogin("captain")).thenReturn(Optional.of(userEntity));

        userService.changeLogin("Bearer token", "captain_new");

        Assertions.assertThat(userEntity.getLogin()).isEqualTo("captain_new");
        Mockito.verify(userEntityRepository).save(userEntity);
        Mockito.verify(snippetVaultService).updateLogin("captain", "captain_new");
    }

    @Test
    void shouldRejectDuplicateNewLogin() {
        UserService userService = new UserService(userEntityRepository, snippetVaultService, passwordEncoder, jwtProvider);
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(userEntityRepository.existsByLogin("captain_new")).thenReturn(true);

        Assertions.assertThatThrownBy(() -> userService.changeLogin("Bearer token", "captain_new"))
                .isInstanceOf(ValidationSin.class);
    }

    @Test
    void shouldRejectMissingUserOnLoginChange() {
        UserService userService = new UserService(userEntityRepository, snippetVaultService, passwordEncoder, jwtProvider);
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(userEntityRepository.findByLogin("captain")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.changeLogin("Bearer token", "captain_new"))
                .isInstanceOf(InternalSin.class);
    }

    @Test
    void shouldChangePassword() {
        UserService userService = new UserService(userEntityRepository, snippetVaultService, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().id(1L).login("captain").build();
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(userEntityRepository.findByLogin("captain")).thenReturn(Optional.of(userEntity));
        Mockito.when(passwordEncoder.encode("new_secret")).thenReturn("encoded");

        userService.changePassword("Bearer token", "new_secret");

        Assertions.assertThat(userEntity.getPassword()).isEqualTo("encoded");
        Mockito.verify(userEntityRepository).save(userEntity);
    }

    @Test
    void shouldRejectMissingUserOnPasswordChange() {
        UserService userService = new UserService(userEntityRepository, snippetVaultService, passwordEncoder, jwtProvider);
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(userEntityRepository.findByLogin("captain")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.changePassword("Bearer token", "new_secret"))
                .isInstanceOf(InternalSin.class);
    }

    @Test
    void shouldDeleteAccount() {
        UserService userService = new UserService(userEntityRepository, snippetVaultService, passwordEncoder, jwtProvider);
        UserEntity userEntity = UserEntity.builder().id(1L).login("captain").build();
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(userEntityRepository.findByLogin("captain")).thenReturn(Optional.of(userEntity));

        userService.deleteAccount("Bearer token");

        Mockito.verify(userEntityRepository).delete(userEntity);
    }

    @Test
    void shouldRejectMissingUserOnDelete() {
        UserService userService = new UserService(userEntityRepository, snippetVaultService, passwordEncoder, jwtProvider);
        Mockito.when(jwtProvider.getLoginFromToken("Bearer token")).thenReturn("captain");
        Mockito.when(userEntityRepository.findByLogin("captain")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.deleteAccount("Bearer token"))
                .isInstanceOf(InternalSin.class);
    }
}
