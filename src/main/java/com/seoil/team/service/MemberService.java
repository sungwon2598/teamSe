package com.seoil.team.service;

import com.seoil.team.domain.member.Member;
import com.seoil.team.domain.member.RoleType;
import com.seoil.team.dto.request.Auth.SignupRequest;
import com.seoil.team.exception.Auth.EmailAlreadyExistsException;
import com.seoil.team.exception.Auth.UserNotFoundException;
import com.seoil.team.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerNewUser(SignupRequest signUpRequest) {
        validateEmailNotExists(signUpRequest.email());

        Member member = createMemberFromRequest(signUpRequest);
        Member savedMember = memberRepository.save(member);
    }

    @Transactional
    public Member createOrUpdateOAuthMember(String email, String name, String provider, String providerId,
                                            String accessToken, String refreshToken, Instant tokenExpirationTime) {
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> createOAuthMember(email, name, provider, providerId));

        member.updateTokenInfo(accessToken, refreshToken, tokenExpirationTime);
        return memberRepository.save(member);
    }

    private Member createOAuthMember(String email, String name, String provider, String providerId) {
        return Member.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .role(RoleType.USER)  // OAuth 사용자의 기본 역할 설정
                .build();
    }

    private void validateEmailNotExists(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    private Member createMemberFromRequest(SignupRequest signUpRequest) {
        return Member.builder()
                .name(signUpRequest.name())
                .email(signUpRequest.email())
                .password(passwordEncoder.encode(signUpRequest.password()))
                .role(signUpRequest.role())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}