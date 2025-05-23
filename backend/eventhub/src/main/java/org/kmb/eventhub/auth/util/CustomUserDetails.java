package org.kmb.eventhub.auth.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kmb.eventhub.enums.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final RoleType role;
    private final Collection<? extends  GrantedAuthority> authorities;

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
