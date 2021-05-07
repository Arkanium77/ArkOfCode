package team.isaz.ark.backup.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import team.isaz.ark.backup.constants.Status;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TokenCheck {
    private Status status;
    private String login;
    private String role;
}
