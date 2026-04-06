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
import team.isaz.ark.user.constants.Roles;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.RoleEntityRepository;
import team.isaz.ark.user.repository.UserEntityRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleEntityRepository roleEntityRepository;

    @Test
    void shouldRejectEmptyUpdateRequest() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);

        Assertions.assertThatThrownBy(() -> adminService.changeUserData(1L, null))
                .isInstanceOf(ValidationSin.class);
    }

    @Test
    void shouldRejectMissingUserOnUpdate() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("new_login");
        Mockito.when(userEntityRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> adminService.changeUserData(1L, userInfo))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void shouldRejectAlreadyUpdatedUser() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserEntity userEntity = UserEntity.builder().id(1L).login("captain").password("encoded").build();
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("captain");
        userInfo.setPassword("secret");
        Mockito.when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        Mockito.when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        Assertions.assertThatThrownBy(() -> adminService.changeUserData(1L, userInfo))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("already updated");
    }

    @Test
    void shouldRejectDuplicateLoginOnUpdate() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserEntity userEntity = UserEntity.builder().id(1L).login("captain").password("encoded").build();
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("new_login");
        userInfo.setPassword("secret");
        Mockito.when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        Mockito.when(userEntityRepository.existsByLogin("new_login")).thenReturn(true);

        Assertions.assertThatThrownBy(() -> adminService.changeUserData(1L, userInfo))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldChangeUserData() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserEntity userEntity = UserEntity.builder().id(1L).login("captain").password("encoded").build();
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin("new_login");
        userInfo.setPassword("secret");
        Mockito.when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        Mockito.when(passwordEncoder.encode("secret")).thenReturn("secret-encoded");

        adminService.changeUserData(1L, userInfo);

        Assertions.assertThat(userEntity.getLogin()).isEqualTo("new_login");
        Assertions.assertThat(userEntity.getPassword()).isEqualTo("secret-encoded");
        Mockito.verify(userEntityRepository).save(userEntity);
    }

    @Test
    void shouldGetIdByLogin() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserEntity userEntity = UserEntity.builder().id(42L).login("captain").build();
        Mockito.when(userEntityRepository.findByLogin("captain")).thenReturn(Optional.of(userEntity));

        Assertions.assertThat(adminService.getId("captain")).isEqualTo(42L);
    }

    @Test
    void shouldDeleteAccount() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserEntity userEntity = UserEntity.builder().id(42L).login("captain").build();
        Mockito.when(userEntityRepository.findById(42L)).thenReturn(Optional.of(userEntity));

        adminService.deleteAccount(42L);

        Mockito.verify(userEntityRepository).delete(userEntity);
    }

    @Test
    void shouldBanAndUnbanAccount() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserEntity userEntity = UserEntity.builder().id(42L).login("captain").build();
        Mockito.when(userEntityRepository.findById(42L)).thenReturn(Optional.of(userEntity));

        adminService.banAccount(42L);
        adminService.unBanAccount(42L);

        Mockito.verify(userEntityRepository).save(userEntity.withUserBanned(true));
        Mockito.verify(userEntityRepository).save(userEntity.withUserBanned(false));
    }

    @Test
    void shouldPromoteAndDemoteAccount() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        RoleEntity adminRole = RoleEntity.builder().name(Roles.ADMIN).build();
        RoleEntity userRole = RoleEntity.builder().name(Roles.USER).build();
        UserEntity userEntity = UserEntity.builder().id(42L).login("captain").role(userRole).build();
        Mockito.when(userEntityRepository.findById(42L)).thenReturn(Optional.of(userEntity), Optional.of(userEntity.withRole(adminRole)));
        Mockito.when(roleEntityRepository.findById(Roles.ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(roleEntityRepository.findById(Roles.USER)).thenReturn(Optional.of(userRole));

        adminService.promoteAccount(42L);
        adminService.demoteAccount(42L);

        Mockito.verify(userEntityRepository).save(Mockito.argThat(savedUser -> Roles.ADMIN.equals(savedUser.getRole().getName())));
        Mockito.verify(userEntityRepository).save(Mockito.argThat(savedUser -> Roles.USER.equals(savedUser.getRole().getName())));
    }

    @Test
    void shouldSkipPromoteAndDemoteWhenRoleAlreadySet() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        RoleEntity adminRole = RoleEntity.builder().name(Roles.ADMIN).build();
        RoleEntity userRole = RoleEntity.builder().name(Roles.USER).build();
        UserEntity adminEntity = UserEntity.builder().id(42L).login("captain").role(adminRole).build();
        UserEntity userEntity = UserEntity.builder().id(43L).login("captain").role(userRole).build();
        Mockito.when(userEntityRepository.findById(42L)).thenReturn(Optional.of(adminEntity));
        Mockito.when(userEntityRepository.findById(43L)).thenReturn(Optional.of(userEntity));
        Mockito.when(roleEntityRepository.findById(Roles.ADMIN)).thenReturn(Optional.of(adminRole));
        Mockito.when(roleEntityRepository.findById(Roles.USER)).thenReturn(Optional.of(userRole));

        adminService.promoteAccount(42L);
        adminService.demoteAccount(43L);

        Mockito.verify(userEntityRepository, Mockito.never()).save(adminEntity.withRole(adminRole));
        Mockito.verify(userEntityRepository, Mockito.never()).save(userEntity.withRole(userRole));
    }

    @Test
    void shouldRejectMissingRolesForPromoteAndDemote() {
        AdminService adminService = new AdminService(userEntityRepository, passwordEncoder, roleEntityRepository);
        UserEntity userEntity = UserEntity.builder().id(42L).login("captain").role(RoleEntity.builder().name(Roles.USER).build()).build();
        Mockito.when(userEntityRepository.findById(42L)).thenReturn(Optional.of(userEntity));
        Mockito.when(roleEntityRepository.findById(Roles.ADMIN)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> adminService.promoteAccount(42L)).isInstanceOf(InternalSin.class);

        Mockito.when(roleEntityRepository.findById(Roles.USER)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> adminService.demoteAccount(42L)).isInstanceOf(InternalSin.class);
    }
}
