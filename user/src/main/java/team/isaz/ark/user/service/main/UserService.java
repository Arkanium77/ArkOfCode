package team.isaz.ark.user.service.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.UserEntityRepository;

import javax.transaction.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEntityRepository userEntityRepository;
    private final SnippetVaultService snippetVaultService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void changeLogin(String token, String newLogin) {
        String login = jwtProvider.getLoginFromToken(token);
        if (userEntityRepository.existsByLogin(newLogin))
            throw new RuntimeException("User with your new login (" + newLogin + ") already exists!");
        UserEntity userEntity = userEntityRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Token valid, but user not found!"));
        userEntity.setLogin(newLogin);
        userEntityRepository.save(userEntity);
        snippetVaultService.updateLogin(login, newLogin);
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
