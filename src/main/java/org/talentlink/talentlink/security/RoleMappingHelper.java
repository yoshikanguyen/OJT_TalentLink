package org.talentlink.talentlink.security;

import org.springframework.stereotype.Component;

@Component
public class RoleMappingHelper {

    public static final long ADMIN_ROLE_ID = 1L;
    public static final long STUDENT_ROLE_ID = 2L;
    public static final long PARTNER_ROLE_ID = 3L;

    public String toRoleName(Long roleId) {
        if (roleId == null) {
            return "USER";
        }
        return switch (roleId.intValue()) {
            case 1 -> "ADMIN";
            case 2 -> "STUDENT";
            case 3 -> "PARTNER";
            default -> "USER";
        };
    }

    public String toSpringRole(Long roleId) {
        return "ROLE_" + toRoleName(roleId);
    }

    public String resolveHomeUrl(Long roleId) {
        return switch (roleId != null ? roleId.intValue() : -1) {
            case 1 -> "/admin/dashboard";
            case 2 -> "/me/home";
            case 3 -> "/partner/dashboard";
            default -> "/";
        };
    }
}
