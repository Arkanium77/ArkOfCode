package team.isaz.ark.core.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;

@Service
@AllArgsConstructor
public class PublisherService {
    private final SnippetRepository snippetRepository;

    public String publish(Snippet snippet) {
        snippetRepository.save(snippet);
        return "Ok!";
    }
}
