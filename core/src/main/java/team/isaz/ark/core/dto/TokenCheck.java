package team.isaz.ark.core.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import team.isaz.ark.core.constants.Status;

@Builder
@Getter
@ToString
@RequiredArgsConstructor
public class TokenCheck {
    private final Status status;
    private final String login;
}
