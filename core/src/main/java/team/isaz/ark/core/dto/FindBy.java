package team.isaz.ark.core.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.util.Pair;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@With
@RequiredArgsConstructor
public class FindBy {
    private Pair<OffsetDateTime, OffsetDateTime> createDttm;
    private Pair<OffsetDateTime, OffsetDateTime> modifyDttm;
    private String author;
    private String title;
    private String text;
    private Set<String> tags;

    private String searchString;
}
