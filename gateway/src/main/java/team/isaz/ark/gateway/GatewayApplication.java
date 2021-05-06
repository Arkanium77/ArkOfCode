package team.isaz.ark.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication
public class GatewayApplication {
    private static String port;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        log.info("GUI here: http://{}:{}/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/", InetAddress.getLoopbackAddress()
                .getHostAddress(), port);
    }

    @Value("${server.port}")
    private void setPort(String port) {
        GatewayApplication.port = port;
    }
}
