package com.seoil.team.service;

import com.seoil.team.domain.member.Member;
import com.seoil.team.domain.member.RoleType;
import com.seoil.team.repository.MemberRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("Loading OAuth2 user");
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getAttribute("sub");

        logger.info("OAuth2 user info - Email: {}, Name: {}, Provider: {}", email, name, provider);

        String accessToken = userRequest.getAccessToken().getTokenValue();
        Instant tokenExpirationTime = userRequest.getAccessToken().getExpiresAt();

        logger.info("Access token received. Expires at: {}", tokenExpirationTime);

        String refreshToken;
        if (userRequest.getAdditionalParameters().containsKey("refresh_token")) {
            refreshToken = userRequest.getAdditionalParameters().get("refresh_token").toString();
            logger.info("Refresh token received");
        } else {
            refreshToken = null;
            logger.warn("No refresh token received");
        }

        Member member = memberRepository.findByEmail(email)
                .map(existingMember -> {
                    logger.info("Updating existing member: {}", existingMember.getEmail());
                    return updateMember(existingMember, name, accessToken, refreshToken, tokenExpirationTime);
                })
                .orElseGet(() -> {
                    logger.info("Creating new member with email: {}", email);
                    return createMember(email, name, provider, providerId, accessToken, refreshToken, tokenExpirationTime);
                });

        logger.info("OAuth2User created successfully");
        return new OAuth2UserImpl(member, oAuth2User.getAttributes());
    }

    private Member createMember(String email, String name, String provider, String providerId,
                                String accessToken, String refreshToken, Instant tokenExpirationTime) {
        logger.info("Creating new member - Email: {}, Provider: {}", email, provider);
        Member newMember = Member.builder()
                .email(email)
                .name(name)
                .role(RoleType.USER)
                .provider(provider)
                .providerId(providerId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenExpirationTime(tokenExpirationTime)
                .build();
        Member savedMember = memberRepository.save(newMember);
        logger.info("New member created and saved - ID: {}", savedMember.getId());
        return savedMember;
    }

    private Member updateMember(Member member, String name, String accessToken, String refreshToken,
                                Instant tokenExpirationTime) {
        logger.info("Updating member - ID: {}, Email: {}", member.getId(), member.getEmail());
        member.setName(name);
        member.updateTokenInfo(accessToken, refreshToken, tokenExpirationTime);
        Member updatedMember = memberRepository.save(member);
        logger.info("Member updated successfully");
        return updatedMember;
    }

    public static class OAuth2UserImpl extends Member implements OAuth2User {
        private Map<String, Object> attributes;

        public OAuth2UserImpl(Member member, Map<String, Object> attributes) {
            // Copy fields from the original Member object
            this.setId(member.getId());
            this.setEmail(member.getEmail());
            this.setPassword(member.getPassword());
            this.setName(member.getName());
            this.setRole(member.getRole());
            this.setProvider(member.getProvider());
            this.setProviderId(member.getProviderId());
            this.setEnabled(member.isEnabled());
            this.setAccessToken(member.getAccessToken());
            this.setRefreshToken(member.getRefreshToken());
            this.setTokenExpirationTime(member.getTokenExpirationTime());

            this.attributes = attributes;
            logger.info("OAuth2UserImpl created for user: {}", this.getEmail());
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return Collections.singleton(new SimpleGrantedAuthority(ROLE_PREFIX + getRole().name()));
        }

        @Override
        public String getName() {
            return getEmail();
        }
    }
}