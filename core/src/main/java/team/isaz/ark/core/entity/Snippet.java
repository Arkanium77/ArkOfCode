package team.isaz.ark.core.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@With
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "snippets")
public class Snippet {
    @Id
    private String id;
    @Field(type = FieldType.Date_Nanos, format = DateFormat.date_time)
    private OffsetDateTime createDttm;
    @Field(type = FieldType.Date_Nanos, format = DateFormat.date_time)
    private OffsetDateTime modifyDttm;
    private boolean hidden = false;
    private String author;
    private String title;
    private String text;
    private Set<String> tags;
}
