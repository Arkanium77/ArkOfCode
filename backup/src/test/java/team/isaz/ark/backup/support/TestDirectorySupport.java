package team.isaz.ark.backup.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class TestDirectorySupport {
    private static final Path ROOT = Path.of("C:\\projects\\java\\ArkOfCode\\.local\\test-temp");

    private TestDirectorySupport() {
    }

    public static Path create(String prefix) throws IOException {
        Files.createDirectories(ROOT);
        return Files.createTempDirectory(ROOT, prefix);
    }

    public static void delete(Path path) throws IOException {
        if (path == null || Files.notExists(path)) {
            return;
        }

        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(currentPath -> {
                        try {
                            Files.deleteIfExists(currentPath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }
}
