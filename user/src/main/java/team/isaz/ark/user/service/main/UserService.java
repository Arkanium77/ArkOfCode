package team.isaz.ark.user.service.main;

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
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(final UserEntityRepository userEntityRepository,
                       final PasswordEncoder passwordEncoder,
                       final JwtProvider jwtProvider) {
        this.userEntityRepository = userEntityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
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
