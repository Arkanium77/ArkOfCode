package team.isaz.ark.backup.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "elastic-search")
public class ElasticsearchClientProperties {

    private String scheme;

    private List<String> hosts;

    private List<Integer> ports;
}
