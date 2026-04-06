package team.isaz.ark.core.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class InternalOperationServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private SnippetRepository snippetRepository;

    @Test
    void shouldRejectTokenWithoutStatus() {
        InternalOperationService service = new InternalOperationService(authService, snippetRepository);
        Mockito.when(authService.checkToken("Bearer token")).thenReturn(new TokenCheck(null, "captain", "ROLE_SERVICE"));

        Assertions.assertThatThrownBy(() -> service.updateLogin("old", "new", "Bearer token"))
                .isInstanceOf(AuthenticationSin.class);
    }

    @Test
    void shouldRejectTokenWithoutServiceRole() {
        InternalOperationService service = new InternalOperationService(authService, snippetRepository);
        Mockito.when(authService.checkToken("Bearer token")).thenReturn(new TokenCheck(Status.OK, "captain", "ROLE_USER"));

        Assertions.assertThatThrownBy(() -> service.updateLogin("old", "new", "Bearer token"))
                .isInstanceOf(AuthenticationSin.class);
    }

    @Test
    void shouldUpdateAllAuthorSnippetsForServiceToken() {
        InternalOperationService service = new InternalOperationService(authService, snippetRepository);
        Snippet first = Snippet.builder().id("s-1").author("old").build();
        Snippet second = Snippet.builder().id("s-2").author("old").build();
        Mockito.when(authService.checkToken("Bearer token")).thenReturn(new TokenCheck(Status.OK, "_tech", "ROLE_SERVICE"));
        Mockito.when(snippetRepository.findAllByAuthor("old")).thenReturn(List.of(first, second));
        Mockito.when(snippetRepository.save(Mockito.any(Snippet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateLogin("old", "new", "Bearer token");

        Mockito.verify(snippetRepository).save(first.withAuthor("new"));
        Mockito.verify(snippetRepository).save(second.withAuthor("new"));
    }
}
