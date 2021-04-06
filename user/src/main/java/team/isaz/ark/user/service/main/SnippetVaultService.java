package team.isaz.ark.user.service.main;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.repository.rest.CoreServiceClient;

@Service
@RequiredArgsConstructor
public class SnippetVaultService {
    private final CoreServiceClient coreServiceClient;
    private final AccountService accountService;

    public void updateLogin(String login, String newLogin) {
        Tokens t = accountService.internalLogin("_tech");
        String bearerToken = "Bearer " + t.getAccessToken();
        coreServiceClient.updateLogin(login, newLogin, bearerToken);
    }
}
