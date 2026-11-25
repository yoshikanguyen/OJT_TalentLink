package org.talentlink.talentlink.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.talentlink.talentlink.entity.User;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final Long roleId;
    private final boolean active;
    private final String lockReason;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, String roleName) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.roleId = user.getRoleId();
        this.active = Boolean.TRUE.equals(user.getIsActive());
        this.lockReason = this.active
                ? null
                : "Tài khoản của bạn đã bị khoá hoặc vô hiệu hóa. Vui lòng liên hệ quản trị viên để biết chi tiết.";
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getLockReason() {
        return lockReason;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
