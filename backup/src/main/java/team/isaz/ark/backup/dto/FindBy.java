package team.isaz.ark.backup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.util.Pair;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@With
@AllArgsConstructor
public class FindBy {
    private Pair<OffsetDateTime, OffsetDateTime> createDttm;
    private Pair<OffsetDateTime, OffsetDateTime> modifyDttm;
    private String author;
    private String title;
    private String text;
    private Set<String> tags;

    private String searchString;
}
