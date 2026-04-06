package team.isaz.ark.user.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import team.isaz.ark.user.UserApplication;
import team.isaz.ark.user.constants.Status;
import team.isaz.ark.user.repository.rest.CoreServiceClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserInfrastructureIntegrationTest {

    private static final boolean DOCKER_AVAILABLE = DockerClientFactory.instance().isDockerAvailable();

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_service")
            .withUsername("noah")
            .withPassword("righteous");

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance().build();

    @BeforeAll
    static void setUpContainers() {
        if (DOCKER_AVAILABLE) {
            postgres.start();
        }
    }

    @AfterAll
    static void tearDownContainers() {
        if (postgres.isRunning()) {
            postgres.stop();
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.discovery.enabled", () -> true);

        if (DOCKER_AVAILABLE) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:user_service;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
            registry.add("spring.liquibase.enabled", () -> false);
        }

        registry.add("feign.core.name", () -> "ark-core-test");
        registry.add("spring.cloud.discovery.client.simple.instances.ark-core-test[0].uri", wiremock::baseUrl);
    }

    @Autowired
    private CoreServiceClient coreServiceClient;

    @Test
    void shouldCallCoreStubThroughFeign() {
        wiremock.stubFor(put(urlPathEqualTo("/internal/snippets/login"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("\"OK\"")));

        Status status = coreServiceClient.updateLogin("old_login", "new_login", "Bearer test");

        assertThat(status).isEqualTo(Status.OK);
    }
}
