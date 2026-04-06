package team.isaz.ark.backup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import team.isaz.ark.backup.entity.Snippet;
import team.isaz.ark.backup.repository.SnippetRepository;
import team.isaz.ark.backup.support.TestDirectorySupport;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final ObjectWriter writer = mapper.writerFor(mapper.getTypeFactory().constructCollectionType(List.class, Snippet.class));

    @Test
    void shouldBackupIntoProvidedDirectory() throws Exception {
        BackupService backupService = new BackupService(snippetRepository, writer, mapper);
        Snippet snippet = Snippet.builder().id("s-1").author("captain").title("hello").text("world").tags(Set.of("java")).build();
        Page<Snippet> page = new PageImpl<>(List.of(snippet), PageRequest.of(0, 100), 1);
        Mockito.when(snippetRepository.findAll(PageRequest.of(0, 100))).thenReturn(page);
        Mockito.when(snippetRepository.count()).thenReturn(1L);

        Path tempDir = TestDirectorySupport.create("backup-service-");
        try {
            Path backupDir = tempDir.resolve("backup");
            String description = backupService.backup(backupDir.toString()).getDescription();

            Assertions.assertThat(description).contains("1/1");
            Assertions.assertThat(Files.list(backupDir)).hasSize(1);
        } finally {
            TestDirectorySupport.delete(tempDir);
        }
    }

    @Test
    void shouldBackupToDefaultDirectoryWhenPathBlank() {
        BackupService backupService = new BackupService(snippetRepository, writer, mapper);
        Page<Snippet> page = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        Mockito.when(snippetRepository.findAll(PageRequest.of(0, 100))).thenReturn(page);
        Mockito.when(snippetRepository.count()).thenReturn(0L);

        String description = backupService.backup("   ").getDescription();

        Assertions.assertThat(description).contains("0/0");
    }

    @Test
    void shouldRestoreSnippetsFromJsonFiles() throws Exception {
        BackupService backupService = new BackupService(snippetRepository, writer, mapper);
        Path tempDir = TestDirectorySupport.create("backup-service-");
        try {
            Snippet first = Snippet.builder().id("s-1").author("one").build();
            Snippet second = Snippet.builder().id("s-2").author("two").build();
            Path file = tempDir.resolve("data0.json");
            writer.writeValue(file.toFile(), List.of(first, second));

            String description = backupService.restore(tempDir.toString()).getDescription();

            Assertions.assertThat(description).contains("2 snippets");
            Mockito.verify(snippetRepository).saveAll(Mockito.argThat(items -> StreamSupport.stream(items.spliterator(), false).count() == 2));
        } finally {
            TestDirectorySupport.delete(tempDir);
        }
    }

    @Test
    void shouldIgnoreUnreadableOrBrokenFilesOnRestore() throws Exception {
        BackupService backupService = new BackupService(snippetRepository, writer, mapper);
        Path tempDir = TestDirectorySupport.create("backup-service-");
        try {
            Path brokenFile = tempDir.resolve("broken.json");
            Files.writeString(brokenFile, "{broken");
            File unreadableFile = tempDir.resolve("unreadable.json").toFile();
            Files.writeString(unreadableFile.toPath(), "[]");
            unreadableFile.setReadable(false, false);

            String description = backupService.restore(tempDir.toString()).getDescription();

            Assertions.assertThat(description).contains("0 snippets");
        } finally {
            File unreadableFile = tempDir.resolve("unreadable.json").toFile();
            unreadableFile.setReadable(true, false);
            TestDirectorySupport.delete(tempDir);
        }
    }

    @Test
    void shouldRejectMissingRestoreDirectory() {
        BackupService backupService = new BackupService(snippetRepository, writer, mapper);

        Assertions.assertThatThrownBy(() -> backupService.restore("C:\\projects\\java\\ArkOfCode\\.local\\missing-" + System.nanoTime()))
                .isInstanceOf(ValidationSin.class)
                .hasMessageContaining("not exists");
    }

    @Test
    void shouldRejectEmptyRestoreDirectory() throws Exception {
        BackupService backupService = new BackupService(snippetRepository, writer, mapper);
        Path tempDir = TestDirectorySupport.create("backup-service-");

        try {
            Assertions.assertThatThrownBy(() -> backupService.restore(tempDir.toString()))
                    .isInstanceOf(ValidationSin.class)
                    .hasMessageContaining("empty");
        } finally {
            TestDirectorySupport.delete(tempDir);
        }
    }

    @Test
    void shouldContinueAutomaticBackupWhenBackupThrows() {
        BackupService backupService = Mockito.spy(new BackupService(snippetRepository, writer, mapper));
        Mockito.doThrow(new RuntimeException("boom")).when(backupService).backup(null);

        backupService.automaticBackup();

        Mockito.verify(backupService).backup(null);
    }
}
