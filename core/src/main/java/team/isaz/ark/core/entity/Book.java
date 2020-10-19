package team.isaz.ark.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.PrePersist;

@Data
@With
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "books")
public class Book {
    @Id
    private String id;
    private Boolean isHidden;
    private String author;
    private String title;
    private String text;

    @PrePersist
    private void persist() {
        if (isHidden == null) {
            isHidden = false;
        }
    }
}
