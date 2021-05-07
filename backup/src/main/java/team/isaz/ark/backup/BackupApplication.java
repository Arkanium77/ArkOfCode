package team.isaz.ark.backup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@Slf4j
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class BackupApplication {
    private static String port;

    public static void main(String[] args) {
        SpringApplication.run(BackupApplication.class, args);
        log.info("GUI here: http://{}:{}/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/", InetAddress.getLoopbackAddress()
                .getHostAddress(), port);
    }

    @Value("${server.port}")
    private void setPort(String port) {
        BackupApplication.port = port;
    }
}
