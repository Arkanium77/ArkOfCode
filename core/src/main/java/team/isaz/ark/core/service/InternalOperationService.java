package team.isaz.ark.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.isaz.ark.core.constants.Roles;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.libs.sinsystem.model.ArkOfSinCodes;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalOperationService {
    private final AuthService authService;
    private final SnippetRepository snippetRepository;

    public Status updateLogin(String login, String newLogin, String bearerToken) {
        TokenCheck role = authService.checkToken(bearerToken);
        log.debug("TokenCheckResult = {}", role);
        if (role.getStatus() == null || role.getStatus().equals(Status.ERROR)) {
            throw new AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11000,
                                        "Не удалось извлечь роль из токена");
        }
        if (!Roles.SERVICE.equals(role.getRole())) {
            throw new AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11000,
                                        "Уровень полномочий не соответствует необходимому для совершения операции");
        }
        log.info("Starting update operation...");
        List<Snippet> snippets = snippetRepository
                .findAllByAuthor(login).stream()
                .map(snippet -> snippet.withAuthor(newLogin))
                .map(snippetRepository::save)
                .collect(Collectors.toList());
        log.info("Updated {} snippets", snippets.size());
        return Status.OK;
    }
}
