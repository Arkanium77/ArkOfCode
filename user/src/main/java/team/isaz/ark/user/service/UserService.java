package team.isaz.ark.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.constants.Roles;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.RoleEntityRepository;
import team.isaz.ark.user.repository.UserEntityRepository;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserEntityRepository userEntityRepository;
    private final RoleEntityRepository roleEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(final UserEntityRepository userEntityRepository,
                       final RoleEntityRepository roleEntityRepository,
                       final PasswordEncoder passwordEncoder,
                       final JwtProvider jwtProvider) {
        this.userEntityRepository = userEntityRepository;
        this.roleEntityRepository = roleEntityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public UserEntity registerUser(UserInfo userInfo) {
        return register(userInfo.getLogin(), userInfo.getPassword(), getUserRole());
    }

    private UserEntity register(String login, String password, RoleEntity role) {
        log.info("Trying register \"{}\" with role {}", login, role.getName());
        log.info("Password {} => {}", password, passwordEncoder.encode(password));
        UserEntity userEntity = userEntityRepository.save(UserEntity.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build());
        log.info("Successful registration! \"{}\" id is {}", login, userEntity.getId());
        return userEntity;
    }

    private RoleEntity getUserRole() {
        return roleEntityRepository.findById(Roles.USER)
                .orElseThrow(() -> new RuntimeException("Can't find basic \"USER\" role"));
    }

    private Optional<UserEntity> login(String login, String password) {
        Optional<UserEntity> userEntity = userEntityRepository.findByLogin(login);
        if (userEntity.isPresent()) {
            if (passwordEncoder.matches(password, userEntity.get().getPassword())) {
                if (userEntity.get().isUserBanned()) {
                    log.info("Login success, but user {} banned!", login);
                    return Optional.empty();
                }
                log.info("Login success!");
                return userEntity;
            }
        }
        log.info("Login failed!");
        return Optional.empty();
    }

    public Tokens login(UserInfo userInfo) {
        return login(userInfo.getLogin(), userInfo.getPassword())
                .map(jwtProvider::generateTokens)
                .orElse(null);
    }

    public Optional<UserEntity> findByLogin(String username) {
        log.debug("Searching user \"{}\"", username);
        return userEntityRepository.findByLogin(username);
    }

    public Tokens refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) throw new RuntimeException("It's not valid token!");
        if (jwtProvider.isThatAccessToken(refreshToken)) throw new RuntimeException("It's not refresh token!");
        String login = jwtProvider.getLoginFromToken(refreshToken);
        log.info("Refreshing token for {}", login);
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Unexpected error: token valid, but user not found!"));
        Tokens tokens = jwtProvider.generateTokens(userEntity);
        log.info("Token successful refreshed");
        return tokens;

    }

    public void changeLogin(String token, String newLogin) {
        String login = jwtProvider.getLoginFromToken(token);
        if (userEntityRepository.existsByLogin(newLogin))
            throw new RuntimeException("User with your new login (" + newLogin + ") already exists!");
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Token valid, but user not found!"));
        userEntity.setLogin(newLogin);
        userEntityRepository.save(userEntity);
        log.info("Login for id={} changed from {} to {}", userEntity.getId(), login, userEntity.getLogin());
    }

    public void changePassword(String token, String newPassword) {
        String login = jwtProvider.getLoginFromToken(token);
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Token valid, but user not found!"));
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userEntityRepository.save(userEntity);
        log.info("Password for {} (id = {}) successful changed", login, userEntity.getId());
    }

    public void deleteAccount(String token) {
        String login = jwtProvider.getLoginFromToken(token);
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Token valid, but user not found!"));
        userEntityRepository.delete(userEntity);
        log.info("Account of {} successful deleted", login);
    }
}
