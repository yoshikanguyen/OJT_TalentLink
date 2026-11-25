package org.talentlink.talentlink.security;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleMappingHelper roleMappingHelper;

    public CustomUserDetailsService(UserRepository userRepository, RoleMappingHelper roleMappingHelper) {
        this.userRepository = userRepository;
        this.roleMappingHelper = roleMappingHelper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Email hoặc mật khẩu không đúng."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException(
                    "Tài khoản của bạn đã bị khoá. Lý do: tài khoản đang ở trạng thái ngưng hoạt động (is_active = false)."
            );
        }

        String roleName = roleMappingHelper.toRoleName(user.getRoleId());
        return new CustomUserDetails(user, roleName);
    }
}
