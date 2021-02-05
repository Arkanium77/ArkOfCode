package team.isaz.ark.core.service.helper;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

@Service
public class ComplexRequestHelper {

    public BoolQueryBuilder findByString(String search) {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("text", search)));
        query.should(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title", search)));
        query.should(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("tags", search)));
        query.minimumShouldMatch(1);
        return query;
    }

    public BoolQueryBuilder findAvailableSnippets(String author, BoolQueryBuilder mainCondition) {
        BoolQueryBuilder query = new BoolQueryBuilder();

        query.should(QueryBuilders.matchQuery("author", author));
        query.should(QueryBuilders.matchQuery("hidden", false));
        query.minimumShouldMatch(1);

        query.must(mainCondition);
        return query;
    }
}
