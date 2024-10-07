package com.seoil.team.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("JwtAuthorizationFilter를 통해 요청 처리 중: {}", request.getRequestURI());

        String token = resolveToken(request);
        log.debug("추출된 토큰: {}", token != null ? "존재함" : "존재하지 않음");

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            if (auth != null && auth.getPrincipal() != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("'{}' 사용자에 대한 인증 정보를 보안 컨텍스트에 설정했습니다. URI: {}", auth.getName(),
                        request.getRequestURI());
            } else {
                log.warn("인증 정보 설정에 실패했습니다. 인증 객체 또는 주체가 null입니다.");
            }
        } else {
            log.debug("유효한 JWT 토큰을 찾을 수 없습니다. URI: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
        log.debug("JwtAuthorizationFilter를 통한 요청 처리 완료: {}", request.getRequestURI());
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    log.debug("쿠키에서 JWT 토큰을 찾았습니다");
                    return cookie.getValue();
                }
            }
        }
        log.debug("쿠키에서 JWT 토큰을 찾을 수 없습니다");
        return null;
    }
}