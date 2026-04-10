package org.makery.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {
    private final User user;
    private Map<String, Object> attributes;

    // 일반 로그인용 생성자
    public PrincipalDetails(User user) {
        this.user = user;
    }

    // OAuth2 로그인용 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    /** OAuth2User 구현 메서드 **/
    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() { return user.getEmail(); }

    /** UserDetails 구현 메서드 **/
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(() -> user.getUserRole().name());
        return collect;
    }

    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getName(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
