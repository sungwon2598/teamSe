package com.seoil.team.oauthTest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 인증된 사용자 정보를 콘솔에 출력
        System.out.println("OAuth2 로그인 성공!");

        // Authentication 객체에서 사용자 정보 가져오기
        var oauth2User = authentication.getPrincipal();
        System.out.println("사용자 정보: " + oauth2User);

        // 추가로 필요한 사용자 정보를 직접 출력 (OAuth2User 인터페이스 이용)
        if (oauth2User instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            var user = (org.springframework.security.oauth2.core.user.OAuth2User) oauth2User;
            System.out.println("사용자 이름: " + user.getAttribute("name"));
            System.out.println("이메일: " + user.getAttribute("email"));
        }

        // 인증 성공 후 리다이렉트
        response.sendRedirect("/");
    }
}
