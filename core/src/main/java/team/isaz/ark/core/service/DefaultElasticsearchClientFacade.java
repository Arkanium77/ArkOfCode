package team.isaz.ark.core.service;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DefaultElasticsearchClientFacade implements ElasticsearchClientFacade {

    @Qualifier("elasticHighLevelClient")
    private final RestHighLevelClient client;

    @Override
    public SearchResponse search(SearchRequest request) throws IOException {
        return client.search(request, RequestOptions.DEFAULT);
    }

    @Override
    public ClearScrollResponse clearScroll(ClearScrollRequest request) throws IOException {
        return client.clearScroll(request, RequestOptions.DEFAULT);
    }
}
