package team.isaz.ark.user.service.auxiliary;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.isaz.ark.user.configuration.CustomUserDetails;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.service.main.AccountService;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountService accountService;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = accountService.findByLogin(username).orElse(getEmpty());
        return CustomUserDetails.fromUserEntityToCustomUserDetails(userEntity);
    }

    private UserEntity getEmpty() {
        return UserEntity.builder().role(RoleEntity.builder().name("Unknown").build()).build();
    }
}
