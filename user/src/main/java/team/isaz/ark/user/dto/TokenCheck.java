package team.isaz.ark.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import team.isaz.ark.user.constants.Status;

@Builder
@Getter
@ToString
@AllArgsConstructor
public class TokenCheck {
    private final Status status;
    private final String login;
}
