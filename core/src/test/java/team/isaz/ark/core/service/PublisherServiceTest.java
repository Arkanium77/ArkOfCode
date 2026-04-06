package team.isaz.ark.core.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    @Mock
    private AuthService authService;

    @Test
    void shouldPublishSnippetForAuthenticatedUser() {
        PublisherService publisherService = new PublisherService(snippetRepository, authService);
        Snippet savedSnippet = Snippet.builder().id("s-1").author("captain").build();
        Mockito.when(authService.getLogin("Bearer token")).thenReturn("captain");
        Mockito.when(snippetRepository.save(Mockito.any(Snippet.class))).thenReturn(savedSnippet);

        String id = publisherService.publish("Bearer token", true, "Title", Set.of("java"), "Text").getId();

        Assertions.assertThat(id).isEqualTo("s-1");
    }

    @Test
    void shouldRejectUpdateWhenSnippetNotFound() {
        PublisherService publisherService = new PublisherService(snippetRepository, authService);
        Mockito.when(authService.getLogin("Bearer token")).thenReturn("captain");
        Mockito.when(snippetRepository.findById("missing")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> publisherService.update("missing", "Bearer token", true, "Title", Set.of("java"), "Text"))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("was not found");
    }

    @Test
    void shouldRejectUpdateForAnotherAuthor() {
        PublisherService publisherService = new PublisherService(snippetRepository, authService);
        Mockito.when(authService.getLogin("Bearer token")).thenReturn("captain");
        Mockito.when(snippetRepository.findById("s-1"))
                .thenReturn(Optional.of(Snippet.builder().id("s-1").author("other").build()));

        Assertions.assertThatThrownBy(() -> publisherService.update("s-1", "Bearer token", true, "Title", Set.of("java"), "Text"))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("not belong");
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        PublisherService publisherService = new PublisherService(snippetRepository, authService);
        Snippet source = Snippet.builder()
                .id("s-1")
                .author("captain")
                .hidden(true)
                .title("Old")
                .text("Body")
                .tags(Set.of("legacy"))
                .build();
        Snippet saved = source.withHidden(false).withTitle("New").withTags(Set.of("java")).withText("Updated");
        Mockito.when(authService.getLogin("Bearer token")).thenReturn("captain");
        Mockito.when(snippetRepository.findById("s-1")).thenReturn(Optional.of(source));
        Mockito.when(snippetRepository.save(Mockito.any(Snippet.class))).thenReturn(saved);

        String id = publisherService.update("s-1", "Bearer token", false, "New", Set.of("java"), "Updated").getId();

        Assertions.assertThat(id).isEqualTo("s-1");
        Mockito.verify(snippetRepository).save(saved);
    }
}
