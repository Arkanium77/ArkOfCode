package team.isaz.ark.backup.config.initializer;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class ElasticsearchInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final DockerImageName IMAGE_NAME =
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.3");
    private static final ElasticsearchContainer CONTAINER = createContainer();

    private static ElasticsearchContainer createContainer() {
        ElasticsearchContainer container = new ElasticsearchContainer(IMAGE_NAME);
        container.withEnv("xpack.security.enabled", "false");
        container.withEnv("discovery.type", "single-node");
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
                "elastic-search.hosts[0]: %s".formatted(CONTAINER.getHost()),
                "elastic-search.ports[0]: %s".formatted(CONTAINER.getMappedPort(9200)),
                "elastic-search.scheme: http"
        ).applyTo(applicationContext);
    }
}
