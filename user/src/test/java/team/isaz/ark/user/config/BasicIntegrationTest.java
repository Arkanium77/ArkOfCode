package team.isaz.ark.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import team.isaz.ark.user.UserApplication;
import team.isaz.ark.user.config.initializer.PostgresInitializer;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.repository.UserEntityRepository;

import java.util.stream.StreamSupport;

@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PostgresInitializer.class)
@ActiveProfiles("test")
public abstract class BasicIntegrationTest {
    protected static final int CORE_SERVICE_PORT = 10102;

    @RegisterExtension
    protected static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(CORE_SERVICE_PORT))
            .build();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserEntityRepository userEntityRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @AfterEach
    void cleanUp() {
        StreamSupport.stream(userEntityRepository.findAll().spliterator(), false)
                .map(UserEntity::getLogin)
                .filter(login -> !"root".equals(login) && !"_tech".equals(login))
                .forEach(login -> userEntityRepository.findByLogin(login).ifPresent(userEntityRepository::delete));
        wireMock.resetAll();
    }
}
