package team.isaz.ark.user.configuration;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team.isaz.ark.user.entity.UserEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {

    private String login;
    private UUID tokenVerifyCode;
    private String password;
    private boolean userBanned;
    private Collection<? extends GrantedAuthority> grantedAuthorities;

    public static CustomUserDetails fromUserEntityToCustomUserDetails(UserEntity userEntity) {
        CustomUserDetails customUserDetails = new CustomUserDetails();
        customUserDetails.login = userEntity.getLogin();
        customUserDetails.password = userEntity.getPassword();
        customUserDetails.tokenVerifyCode = userEntity.getTokenVerifyCode();
        customUserDetails.grantedAuthorities = Collections.singletonList(new SimpleGrantedAuthority(userEntity.getRole().getName()));
        customUserDetails.userBanned = userEntity.isUserBanned();
        return customUserDetails;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}