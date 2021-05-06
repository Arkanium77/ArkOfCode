package team.isaz.ark.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.isaz.ark.core.dto.Response;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PublisherService {
    private final SnippetRepository snippetRepository;
    private final AuthService authService;


    private Response publish(Snippet snippet) {
        String id = snippetRepository.save(snippet).getId();
        return Response.ok("Snippet successfully saved").withId(id);
    }

    public Response publish(String bearerToken, boolean hidden, String title, Set<String> tags, String text) {
        String login = authService.getLogin(bearerToken);
        return publish(Snippet.builder()
                .author(login)
                .hidden(hidden)
                .title(title)
                .tags(tags)
                .text(text)
                .build());
    }

    public Response update(String snippetId, String bearerToken,
                           Boolean hidden, String title, Set<String> tags, String text) {
        String login = authService.getLogin(bearerToken);
        Snippet snippet = snippetRepository.findById(snippetId)
                .orElseThrow(() -> new ValidationSin("Snippet with id " + snippetId + " was not found"));
        if (!snippet.getAuthor().equals(login)) {
            throw new ValidationSin("Snippet with id " + snippetId + " not belong to user [" + login + "]");
        }
        if (hidden != null) {
            snippet = snippet.withHidden(hidden);
        }
        if (title != null) {
            snippet = snippet.withTitle(title);
        }
        if (tags != null) {
            snippet = snippet.withTags(tags);
        }
        if (text != null) {
            snippet = snippet.withText(text);
        }
        return publish(snippet);
    }
}
