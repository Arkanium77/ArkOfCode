package team.isaz.ark.user.config.initializer;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final DockerImageName IMAGE_NAME = DockerImageName.parse("postgres:16-alpine");
    private static final PostgreSQLContainer<?> CONTAINER = createContainer();

    private static PostgreSQLContainer<?> createContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(IMAGE_NAME);
        container.withDatabaseName("user_service");
        container.withUsername("noah");
        container.withPassword("righteous");
        container.withReuse(true);
        return container;
    }

    private static void start() {
        if (!CONTAINER.isRunning()) {
            CONTAINER.start();
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        start();
        TestPropertyValues.of(
                "spring.datasource.url: %s".formatted(CONTAINER.getJdbcUrl()),
                "spring.datasource.username: %s".formatted(CONTAINER.getUsername()),
                "spring.datasource.password: %s".formatted(CONTAINER.getPassword()),
                "spring.datasource.driver-class-name: %s".formatted(CONTAINER.getDriverClassName())
        ).applyTo(applicationContext);
    }
}
