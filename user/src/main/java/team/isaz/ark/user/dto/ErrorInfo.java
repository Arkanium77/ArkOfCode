package team.isaz.ark.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
public class ErrorInfo {
    private final String name;
    private final String description;

    @Override
    public String toString() {
        return name + ": " + description;
    }
}
