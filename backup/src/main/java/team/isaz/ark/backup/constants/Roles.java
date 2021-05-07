package team.isaz.ark.backup.constants;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class Roles {
    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String SERVICE = "ROLE_SERVICE";

    public static Set<String> getAllRoles() {
        return Sets.newHashSet(USER, ADMIN, SERVICE);
    }
}
