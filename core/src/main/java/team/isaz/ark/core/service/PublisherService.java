package team.isaz.ark.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.isaz.ark.core.constants.Status;
import team.isaz.ark.core.dto.TokenCheck;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;
import team.isaz.ark.libs.sinsystem.model.ArkOfSinCodes;
import team.isaz.ark.libs.sinsystem.model.sin.AuthenticationSin;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PublisherService {
    private final SnippetRepository snippetRepository;
    private final AuthService authService;


    public Status publish(Snippet snippet) {
        snippetRepository.save(snippet);
        return Status.OK;
    }

    public Status publish(String bearerToken, boolean hidden, String title, Set<String> tags, String text) {
        TokenCheck t = authService.getLogin(bearerToken);
        if (Status.ERROR.equals(t.getStatus())) {
            throw new AuthenticationSin(ArkOfSinCodes.AuthenticationErrorCode.ERR_CODE_11001);
        }
        String login = t.getLogin();
        return publish(Snippet.builder()
                               .author(login)
                               .hidden(hidden)
                               .title(title)
                               .tags(tags)
                               .text(text)
                               .build());
    }
}
