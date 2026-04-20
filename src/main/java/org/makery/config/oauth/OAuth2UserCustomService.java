package org.makery.config.oauth;

import lombok.RequiredArgsConstructor;
import org.makery.domain.OAuthProvider;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.User;
import org.makery.domain.UserRole;
import org.makery.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        User userEntity = saveOrUpdate(oAuth2User);
        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }

    private User saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        return userRepository.findByEmail(email)
                .map(entity -> entity.update(name)) // 이미 있으면 이름 업데이트
                .orElseGet(() -> userRepository.save(User.builder() // 없으면 신규 가입
                        .email(email)
                        .name(name)
                        .userRole(UserRole.ROLE_GUEST)
                        .oAuthProvider(OAuthProvider.GOOGLE)
                        .build()));
    }
}