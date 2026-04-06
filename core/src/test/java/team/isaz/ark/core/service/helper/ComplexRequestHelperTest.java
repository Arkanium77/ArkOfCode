package team.isaz.ark.core.service.helper;

import org.assertj.core.api.Assertions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;

class ComplexRequestHelperTest {

    @Test
    void shouldBuildWildcardSearchQuery() {
        ComplexRequestHelper helper = new ComplexRequestHelper();

        BoolQueryBuilder query = helper.findByString("hello world");

        Assertions.assertThat(query.toString()).contains("*hello*world*");
        Assertions.assertThat(query.toString()).contains("\"tags\"");
    }

    @Test
    void shouldBuildAvailableSnippetsQueryForAnonymousUser() {
        ComplexRequestHelper helper = new ComplexRequestHelper();

        BoolQueryBuilder query = helper.findAvailableSnippets(null, new BoolQueryBuilder());

        Assertions.assertThat(query.toString()).doesNotContain("\"author\"");
        Assertions.assertThat(query.toString()).contains("\"hidden\"");
    }

    @Test
    void shouldBuildAvailableSnippetsQueryForAuthor() {
        ComplexRequestHelper helper = new ComplexRequestHelper();

        BoolQueryBuilder query = helper.findAvailableSnippets("captain", new BoolQueryBuilder());

        Assertions.assertThat(query.toString()).contains("\"author\"");
        Assertions.assertThat(query.toString()).contains("\"captain\"");
    }
}
