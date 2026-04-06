package team.isaz.ark.core.service;

import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;

import java.io.IOException;

public interface ElasticsearchClientFacade {
    SearchResponse search(SearchRequest request) throws IOException;

    ClearScrollResponse clearScroll(ClearScrollRequest request) throws IOException;
}
