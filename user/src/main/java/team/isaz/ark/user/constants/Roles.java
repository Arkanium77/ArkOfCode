package team.isaz.ark.user.constants;

import com.google.common.collect.Sets;

import java.util.Set;

public class Roles {
    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";

    public Set<String> getAllRoles() {
        return Sets.newHashSet(USER, ADMIN);
    }
}
