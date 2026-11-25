package org.talentlink.talentlink.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RoleMappingHelper roleMappingHelper;

    public RoleBasedAuthenticationSuccessHandler(RoleMappingHelper roleMappingHelper) {
        this.roleMappingHelper = roleMappingHelper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        Long roleId = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            roleId = userDetails.getRoleId();
        }

        String targetUrl = roleMappingHelper.resolveHomeUrl(roleId);
        response.sendRedirect(request.getContextPath() + targetUrl);
    }
}
