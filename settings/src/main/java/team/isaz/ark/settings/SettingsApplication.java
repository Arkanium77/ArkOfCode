package team.isaz.ark.settings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class SettingsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SettingsApplication.class, args);
    }
}
