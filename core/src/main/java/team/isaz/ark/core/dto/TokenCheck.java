package team.isaz.ark.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import team.isaz.ark.core.constants.Status;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TokenCheck {
    private Status status;
    private String login;
    private String role;
}
