package team.isaz.ark.user.service.main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.constants.Roles;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.RoleEntityRepository;
import team.isaz.ark.user.repository.UserEntityRepository;

@Slf4j
@Service
public class AdminService {

    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RoleEntityRepository roleEntityRepository;

    public AdminService(final UserEntityRepository userEntityRepository,
                        final PasswordEncoder passwordEncoder,
                        final RoleEntityRepository roleEntityRepository,
                        final JwtProvider jwtProvider) {
        this.userEntityRepository = userEntityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.roleEntityRepository = roleEntityRepository;
    }

    /*
    public void changeUserData(Long id, UserInfo userInfo) {
        String login = jwtProvider.getLoginFromToken(token);
        if (userEntityRepository.existsByLogin(newLogin))
            throw new RuntimeException("User with your new login (" + newLogin + ") already exists!");
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Token valid, but user not found!"));
        userEntity.setLogin(newLogin);
        userEntityRepository.save(userEntity);
        log.info("Login for id={} changed from {} to {}", userEntity.getId(), login, userEntity.getLogin());
    }
*/
    public Long getId(String login) {
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("User not found!"));
        log.info("Account {} have id = {}", userEntity.getLogin(), userEntity.getId());
        return userEntity.getId();
    }

    public void deleteAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
        userEntityRepository.delete(userEntity);
        log.info("Account of {} (id = {}) successful deleted", userEntity.getLogin(), userEntity.getId());
    }

    public void banAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
        userEntityRepository.save(userEntity.withUserBanned(true));
        log.info("Account of {} (id = {}) successful banned", userEntity.getLogin(), userEntity.getId());
    }

    public void unBanAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
        userEntityRepository.save(userEntity.withUserBanned(false));
        log.info("Account of {} (id = {}) successful unbanned", userEntity.getLogin(), userEntity.getId());
    }

    public void promoteAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
        RoleEntity admin = getAdminRole();
        if (userEntity.getRole().equals(admin)) {
            log.info("Account of {} (id = {}) already has role ADMIN", userEntity.getLogin(), userEntity.getId());
            return;
        }
        userEntityRepository.save(userEntity.withRole(admin));
        log.info("Account of {} (id = {}) promoted to role ADMIN", userEntity.getLogin(), userEntity.getId());
    }

    public void demoteAccount(Long id) {
        UserEntity userEntity = userEntityRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
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
                .orElseThrow(() -> new RuntimeException("Can't find basic \"USER\" role"));
    }

    private RoleEntity getAdminRole() {
        return roleEntityRepository.findById(Roles.ADMIN)
                .orElseThrow(() -> new RuntimeException("Can't find basic \"ADMIN\" role"));
    }
}
