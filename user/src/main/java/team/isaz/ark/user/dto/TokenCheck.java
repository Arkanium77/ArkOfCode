package team.isaz.ark.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import team.isaz.ark.user.constants.Status;

@Builder
@Getter
@ToString
@RequiredArgsConstructor
public class TokenCheck {
    private final Status status;
    private final String login;
}
