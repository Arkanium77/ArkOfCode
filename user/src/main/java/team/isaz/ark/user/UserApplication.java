package team.isaz.ark.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;

@Slf4j
@EnableJpaRepositories
@EnableTransactionManagement
@SpringBootApplication
public class UserApplication {
    private static String port;

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        log.info("GUI here: http://{}:{}/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/", InetAddress.getLoopbackAddress()
                .getHostAddress(), port);
    }

    @Value("${server.port}")
    private void setPort(String port) {
        UserApplication.port = port;
    }
}
