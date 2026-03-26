package com.bolsaempleo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails wrapper that carries the authenticated user's
 * internal ID and role (EMPRESA, OFERENTE, or ADMIN).
 */
public class CustomUserDetails implements UserDetails {

    private final Long   id;
    private final String username;  // correo or identificacion
    private final String password;  // BCrypt hash
    private final String role;      // ROLE_EMPRESA | ROLE_OFERENTE | ROLE_ADMIN

    public CustomUserDetails(Long id, String username, String password, String role) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    public Long getId() { return id; }

    public String getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getPassword()  { return password; }
    @Override public String getUsername()  { return username; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}
