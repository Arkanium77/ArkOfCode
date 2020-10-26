package team.isaz.ark.user.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserInfo {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;
}
