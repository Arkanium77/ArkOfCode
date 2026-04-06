package team.isaz.ark.user.service.auxiliary;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import team.isaz.ark.user.configuration.CustomUserDetails;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.service.main.AccountService;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AccountService accountService;

    @Test
    void shouldLoadKnownUser() {
        CustomUserDetailsService service = new CustomUserDetailsService(accountService);
        UserEntity userEntity = UserEntity.builder()
                .login("captain")
                .password("secret")
                .tokenVerifyCode(UUID.randomUUID())
                .role(RoleEntity.builder().name("ROLE_USER").build())
                .build();
        Mockito.when(accountService.findByLogin("captain")).thenReturn(Optional.of(userEntity));

        CustomUserDetails details = service.loadUserByUsername("captain");

        Assertions.assertThat(details.getUsername()).isEqualTo("captain");
        Assertions.assertThat(details.getAuthorities()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyUserForUnknownLogin() {
        CustomUserDetailsService service = new CustomUserDetailsService(accountService);
        Mockito.when(accountService.findByLogin("captain")).thenReturn(Optional.empty());

        CustomUserDetails details = service.loadUserByUsername("captain");

        Assertions.assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("Unknown");
        Assertions.assertThat(details.getUsername()).isNull();
    }
}
