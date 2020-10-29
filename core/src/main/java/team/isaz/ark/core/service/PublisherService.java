package team.isaz.ark.core.service;

import org.springframework.stereotype.Service;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;

@Service
public class PublisherService {
    private final SnippetRepository snippetRepository;

    public PublisherService(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    public String publish(Snippet snippet) {
        snippetRepository.save(snippet);
        return "Ok!";
    }
}
