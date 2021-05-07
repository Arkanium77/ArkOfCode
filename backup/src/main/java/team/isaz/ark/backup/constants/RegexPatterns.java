package team.isaz.ark.backup.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegexPatterns {

    public static final String LOGIN = "^[a-zA-Z0-9_][a-zA-Z0-9_]{2,24}$";
    public static final String PASSWORD = "^\\S{6,50}$";
}
