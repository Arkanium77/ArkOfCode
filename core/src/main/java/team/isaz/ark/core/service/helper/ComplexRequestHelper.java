package team.isaz.ark.core.service.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplexRequestHelper {

    public BoolQueryBuilder findByString(String search) {
        String wildCard = "*" + String.join("*", search.split("\\s")) + "*";
        log.info("WILDCARD: {}", wildCard);
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(QueryBuilders.boolQuery()
                .must(new WildcardQueryBuilder("text", wildCard)));
        query.should(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("title", search).fuzziness(Fuzziness.ONE))
                .should(new WildcardQueryBuilder("title", wildCard))
                .minimumShouldMatch(1));
        query.should(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("tags", search)));
        query.minimumShouldMatch(1);
        return query;
    }

    public BoolQueryBuilder findAvailableSnippets(String author, BoolQueryBuilder mainCondition) {
        BoolQueryBuilder query = new BoolQueryBuilder();

        if (author != null && !author.isEmpty()) {
            query.should(QueryBuilders.matchQuery("author", author));
        }
        query.should(QueryBuilders.matchQuery("hidden", false));
        query.minimumShouldMatch(1);

        query.must(mainCondition);
        return query;
    }
}
