package team.isaz.ark.user.service.main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.repository.rest.CoreServiceClient;

@ExtendWith(MockitoExtension.class)
class SnippetVaultServiceTest {

    @Mock
    private CoreServiceClient coreServiceClient;

    @Mock
    private AccountService accountService;

    @Test
    void shouldDelegateLoginUpdateToCoreService() {
        SnippetVaultService snippetVaultService = new SnippetVaultService(coreServiceClient, accountService);
        Mockito.when(accountService.internalLogin("_tech"))
                .thenReturn(Tokens.builder().accessToken("service-token").refreshToken("refresh").build());

        snippetVaultService.updateLogin("old_login", "new_login");

        Mockito.verify(coreServiceClient).updateLogin("old_login", "new_login", "Bearer service-token");
    }
}
