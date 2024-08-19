package com.seoil.team.service;

import com.seoil.team.domain.member.Member;
import com.seoil.team.dto.request.Auth.LoginRequest;
import com.seoil.team.dto.response.Auth.UserInfoResponse;
import com.seoil.team.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public String authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.createToken(authentication);
    }

    public UserInfoResponse getUserInfo(Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();

        return new UserInfoResponse(
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}