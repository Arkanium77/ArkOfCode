package team.isaz.ark.user.service.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.isaz.ark.libs.sinsystem.model.ArkOfSinCodes;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserEntityRepository userEntityRepository;
    private final RoleEntityRepository roleEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserEntity registerUser(UserInfo userInfo) {
        return register(userInfo.getLogin(), userInfo.getPassword(), getUserRole());
    }

    private UserEntity register(String login, String password, RoleEntity role) {
        if (userEntityRepository.existsByLogin(login)) {
            throw new ValidationSin("User with this login (" + login + ") already exists!");
        }

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
                .orElseThrow(() -> new InternalSin("Can't find basic \"USER\" role"));
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

    public Tokens internalLogin(String login) {
        Optional<UserEntity> userEntity = userEntityRepository.findByLogin(login);
        return userEntity
                .map(jwtProvider::generateTokens)
                .orElseThrow(() -> new InternalSin(ArkOfSinCodes.InternalErrorCode.ERR_CODE_10000,
                        "Не удалось получить привелегии для обновления сниппетов"));
    }

    public Optional<UserEntity> findByLogin(String username) {
        log.debug("Searching user \"{}\"", username);
        return userEntityRepository.findByLogin(username);
    }

    public Tokens refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ValidationSin("It's not valid token!");
        }
        if (jwtProvider.isThatAccessToken(refreshToken)) {
            throw new ValidationSin("It's not refresh token!");
        }
        String login = jwtProvider.getLoginFromToken(refreshToken);
        log.info("Refreshing token for {}", login);
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(
                () -> new InternalSin("Unexpected error: token valid, but user not found!"));
        Tokens tokens = jwtProvider.generateTokens(userEntity);
        log.info("Token successful refreshed");
        return tokens;

    }
}
