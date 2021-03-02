package team.isaz.ark.core.dto;

import lombok.Builder;
import lombok.Getter;

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