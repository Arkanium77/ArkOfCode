package team.isaz.ark.backup.config;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchClientConfig extends ElasticsearchConfiguration {

    private final ElasticsearchClientProperties properties;

    @Bean(name = "elasticHighLevelClient")
    public RestHighLevelClient elasticHighLevelClient() {
        final HttpHost[] hosts = new HttpHost[properties.getHosts().size()];
        for (int i = 0; i < properties.getHosts().size(); i++) {
            hosts[i] = new HttpHost(properties.getHosts().get(i), properties.getPorts().get(i), properties.getScheme());
        }
        return new RestHighLevelClient(RestClient.builder(hosts));
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        List<String> endpoints = new ArrayList<>();
        for (int i = 0; i < properties.getHosts().size(); i++) {
            endpoints.add(properties.getHosts().get(i) + ":" + properties.getPorts().get(i));
        }

        return ClientConfiguration.builder()
                .connectedTo(endpoints.toArray(new String[0]))
                .usingSsl("https".equalsIgnoreCase(properties.getScheme()))
                .build();
    }
}
