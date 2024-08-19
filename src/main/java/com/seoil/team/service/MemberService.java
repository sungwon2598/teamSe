package com.seoil.team.service;

import com.seoil.team.domain.member.Member;
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