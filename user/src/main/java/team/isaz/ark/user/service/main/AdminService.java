package team.isaz.ark.user.service.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.isaz.ark.libs.sinsystem.model.sin.InternalSin;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;
import team.isaz.ark.user.constants.Roles;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.RoleEntityRepository;
import team.isaz.ark.user.repository.UserEntityRepository;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleEntityRepository roleEntityRepository;

    public void changeUserData(Long id, UserInfo userInfo) {
        if (Objects.isNull(userInfo) || Objects.isNull(userInfo.getLogin()) && Objects.isNull(userInfo.getPassword())) {
            throw new ValidationSin("No data to change!");
        }
        UserEntity userEntity = userEntityRepository.findById(id)
                .orElseThrow(() -> new ValidationSin("User not found!"));
        if ((userInfo.getLogin() == null || userEntity.getLogin().equals(userInfo.getLogin())) && (passwordEncoder.matches(userInfo.getPassword(), userEntity.getPassword()))) {
            throw new ValidationSin("That user already updated!");
        }
        if (userInfo.getLogin() != null) {
            if (userEntityRepository.existsByLogin(userInfo.getLogin())) {
                throw new ValidationSin("User with new login (" + userInfo.getLogin() + ") already exists!");
            }
            userEntity.setLogin(userInfo.getLogin());
        }
        if (userInfo.getPassword() != null) {
            userEntity.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        }
        userEntityRepository.save(userEntity);
        log.info("Data of user with id={} updated", userEntity.getId());
    }

    public Long getId(String login) {
        UserEntity userEntity = userEntityRepository.findByLogin(login)
                .orElseThrow(() -> new ValidationSin("User not found!"));
        log.info("Account {} have id = {}", userEntity.getLogin(), userEntity.getId());
        return userEntity.getId();
    }

    public void deleteAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id)
                .orElseThrow(() -> new ValidationSin("User not found!"));
        userEntityRepository.delete(userEntity);
        log.info("Account of {} (id = {}) successful deleted", userEntity.getLogin(), userEntity.getId());
    }

    public void banAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id)
                .orElseThrow(() -> new ValidationSin("User not found!"));
        userEntityRepository.save(userEntity.withUserBanned(true));
        log.info("Account of {} (id = {}) successful banned", userEntity.getLogin(), userEntity.getId());
    }

    public void unBanAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id)
                .orElseThrow(() -> new ValidationSin("User not found!"));
        userEntityRepository.save(userEntity.withUserBanned(false));
        log.info("Account of {} (id = {}) successful unbanned", userEntity.getLogin(), userEntity.getId());
    }

    public void promoteAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id)
                .orElseThrow(() -> new ValidationSin("User not found!"));
        RoleEntity admin = getAdminRole();
        if (userEntity.getRole().equals(admin)) {
            log.info("Account of {} (id = {}) already has role ADMIN", userEntity.getLogin(), userEntity.getId());
            return;
        }
        userEntityRepository.save(userEntity.withRole(admin));
        log.info("Account of {} (id = {}) promoted to role ADMIN", userEntity.getLogin(), userEntity.getId());
    }

    public void demoteAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id)
                .orElseThrow(() -> new ValidationSin("User not found!"));
        RoleEntity user = getUserRole();
        if (userEntity.getRole().equals(user)) {
            log.info("Account of {} (id = {}) already has role USER", userEntity.getLogin(), userEntity.getId());
            return;
        }
        userEntityRepository.save(userEntity.withRole(user));
        log.info("Account of {} (id = {}) demoted to role USER", userEntity.getLogin(), userEntity.getId());
    }

    private RoleEntity getUserRole() {
        return roleEntityRepository.findById(Roles.USER)
                .orElseThrow(() -> new InternalSin("Can't find basic \"USER\" role"));
    }

    private RoleEntity getAdminRole() {
        return roleEntityRepository.findById(Roles.ADMIN)
                .orElseThrow(() -> new InternalSin("Can't find basic \"ADMIN\" role"));
    }
}
