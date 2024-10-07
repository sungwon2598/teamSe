package com.seoil.team.security.jwt;

import com.seoil.team.domain.member.Member;
import com.seoil.team.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final ObjectProvider<MemberService> memberServiceProvider;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    private SecretKey key;

    public JwtTokenProvider(ObjectProvider<MemberService> memberServiceProvider) {
        this.memberServiceProvider = memberServiceProvider;
    }

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JwtTokenProvider가 비밀 키로 초기화되었습니다");
    }

    public String createToken(Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        String username = member.getEmail();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        log.info("사용자를 위한 토큰 생성 중: {}", authentication.getName());
        String token = Jwts.builder()
                .subject(username)
                .claim("auth", authorities)
                .claim("id", member.getId())
                .claim("name", member.getName())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        log.debug("토큰이 생성되었습니다: {}", token);
        return token;
    }

    public Authentication getAuthentication(String token) {
        log.debug("토큰으로부터 인증 정보 추출 중");
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        String username = claims.getSubject();
        log.info("사용자에 대한 인증 정보가 추출되었습니다: {}", username);

        return new UsernamePasswordAuthenticationToken(username, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            log.debug("토큰이 성공적으로 검증되었습니다");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }
}