package team.isaz.ark.user.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.isaz.ark.user.configuration.CustomUserDetails;
import team.isaz.ark.user.entity.RoleEntity;
import team.isaz.ark.user.entity.UserEntity;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userService.findByLogin(username).orElse(getEmpty());
        return CustomUserDetails.fromUserEntityToCustomUserDetails(userEntity);
    }

    private UserEntity getEmpty() {
        return UserEntity.builder().role(RoleEntity.builder().name("Unknown").build()).build();
    }
}
