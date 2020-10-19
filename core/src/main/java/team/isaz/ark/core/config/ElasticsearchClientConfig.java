package team.isaz.ark.core.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class ElasticsearchClientConfig {

    private final ElasticsearchClientProperties properties;

    public ElasticsearchClientConfig(ElasticsearchClientProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestHighLevelClient elasticHighLevelClient() {
        final HttpHost[] hosts = new HttpHost[properties.getHosts().size()];
        for (int i = 0; i < properties.getHosts().size(); i++) {
            final String host = properties.getHosts().get(i);
            final Integer port = properties.getPorts().get(i);
            final HttpHost httpHost = new HttpHost(host, port, properties.getScheme());
            hosts[i] = httpHost;
        }
        return new RestHighLevelClient(RestClient.builder(hosts));
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(elasticHighLevelClient());
    }
}
