package team.isaz.ark.backup.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
@Getter
public class Tokens {
    final String accessToken;
    final String refreshToken;
}
