package team.isaz.ark.backup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.isaz.ark.backup.dto.Response;
import team.isaz.ark.backup.repository.SnippetRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {
    private final SnippetRepository snippetRepository;
    private final AuthService authService;

    public Response backup(String path) {
        return null;
    }

    public Response restore(String path) {
        return null;
    }
}
