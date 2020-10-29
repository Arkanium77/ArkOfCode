package team.isaz.ark.core.service;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import team.isaz.ark.core.entity.Snippet;
import team.isaz.ark.core.repository.SnippetRepository;

import java.util.List;

@Service
public class SearchService {
    private final SnippetRepository snippetRepository;

    public SearchService(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    public List<Snippet> all() {
        return Lists.newArrayList(snippetRepository.findAll());
    }
}
