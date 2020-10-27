package team.isaz.ark.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Builder
@Getter
public class Tokens {
    final String accessToken;
    final String refreshToken;
}
