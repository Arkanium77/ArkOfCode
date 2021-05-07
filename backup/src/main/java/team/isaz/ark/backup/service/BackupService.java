package team.isaz.ark.backup.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.isaz.ark.backup.dto.Response;
import team.isaz.ark.backup.entity.Snippet;
import team.isaz.ark.backup.repository.SnippetRepository;
import team.isaz.ark.libs.sinsystem.model.sin.InternalSin;
import team.isaz.ark.libs.sinsystem.model.sin.ValidationSin;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {
    private final SnippetRepository snippetRepository;
    private final ObjectWriter writer;
    private final ObjectMapper mapper;

    /**
     * Автоматическое копирование.
     * Место назначения - стандартная директория.
     * Копирование происходит каждые 30 дней (значение задано в милисекундах)
     */
    @Scheduled(fixedDelayString = "2592000000")
    public void automaticBackup() {
        log.info("Automatic backup started");
        try {
            Response r = backup(null);
            log.info("Automatic backup end. Status: {}, Description: {}", r.getStatus(), r.getDescription());
        } catch (Exception e) {
            log.warn("Automatic backup fail! Caught {}: {}\nTrace:\n", e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    public Response backup(String path) {
        path = getCorrectPath(path);
        int pageSize = 100;
        String data = "data";
        String json = ".json";
        long count = 0;
        Page<Snippet> page = snippetRepository.findAll(PageRequest.of(0, pageSize));
        int elementCount = page.getNumberOfElements();
        if (elementCount != 0) {
            createDirectory(path);
            while (true) {
                String file = new File(path, data + page.getNumber() + json).getAbsolutePath();
                log.info("Processing file {}", file);
                saveContent(file, page.getContent());
                count += elementCount;
                log.info("Processing file {} complete. File contains {} snippets", file, elementCount);
                if (page.isLast()) {
                    break;
                }
                page = snippetRepository.findAll(page.nextPageable());
                elementCount = page.getNumberOfElements();
            }
        }
        log.info("Backup complete! Successfully saved data of {}/{} snippets", count, snippetRepository.count());
        return Response
                .ok("Backup complete! Successfully saved data of "
                        + count + "/" + snippetRepository.count() + " snippets");
    }

    @SneakyThrows
    public Response restore(String path) {
        path = getCorrectPath(path);
        File[] list = getFiles(path);
        long count = 0;
        for (File f : list) {
            if (!f.canRead()) {
                throw new InternalSin("Cant read file " + f.getAbsolutePath());
            }
            List<Snippet> snippets = mapper.readValue(f, new TypeReference<List<Snippet>>() {
            });
            log.info("File {} contains {} snippets", f.getName(), snippets.size());
            snippetRepository.saveAll(snippets);
            count += snippets.size();
        }
        return Response.ok("Successfully restored " + count + " snippets");
    }

    private String getCorrectPath(String path) {
        if (path == null || path.replaceAll("\\s", "").isEmpty()) {
            log.info("Received path null or empty. Backup will be created in default dir");
            path = System.getProperty("user.dir") + File.separator + "automatic_backup";
        }
        return path;
    }


    @SneakyThrows
    private void saveContent(String path, List<Snippet> list) {
        File f = new File(path);
        if (!f.createNewFile() && !f.delete() || !f.delete() && !f.createNewFile() && !f.delete()) {
            throw new InternalSin("Выбранное расположение файла не доступно для записи");
        }
        writer.writeValue(f, list);
        log.info("File {} created with data of {} snippets", path, list.size());
    }

    @SneakyThrows
    private void createDirectory(String path) {
        File d = new File(path);
        if (!d.exists() && d.mkdirs()) {
            log.info("Directory {} created", path);
        } else {
            log.info("can't create directory {}", path);
        }
        log.info("Setting {} writable => {}", d.getName(), d.setWritable(true));
        File[] list = d.listFiles();
        if (list != null && list.length != 0) {
            log.warn("Directory is not empty! All content will be removed");
            for (File f : list) {
                log.info("File \"{}\" delete => {}", f.getName(), f.delete());
            }
        }
    }

    private File[] getFiles(String path) {
        File d = new File(path);
        if (!d.exists()) {
            throw new ValidationSin("Validation error: directory with backup is not exists!");
        }

        File[] list = d.listFiles();
        if (list == null || list.length == 0) {
            throw new ValidationSin("Validation error: directory with backup is empty!");
        }
        return list;
    }

}
