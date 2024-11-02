package com.seoil.team.service;

import com.seoil.team.domain.member.Member;
import com.seoil.team.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2TokenRefreshService {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenRefreshService.class);

    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;

    @Value("${oauth2.client-id}")
    private String clientId;

    @Value("${oauth2.client-secret}")
    private String clientSecret;

    @Transactional
    public String refreshAccessToken(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (member.getRefreshToken() == null) {
            throw new RuntimeException("Refresh token not available");
        }

        String refreshToken = member.getRefreshToken();

        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&refresh_token=" + refreshToken +
                "&grant_type=refresh_token";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();
            String newAccessToken = (String) responseBody.get("access_token");
            Integer expiresIn = (Integer) responseBody.get("expires_in");

            Instant newExpirationTime = Instant.now().plusSeconds(expiresIn);

            member.updateTokenInfo(newAccessToken, refreshToken, newExpirationTime);
            memberRepository.save(member);

            logger.info("New access token received for user: {}. Token expires in {} seconds", email, expiresIn);

            return newAccessToken;
        } else {
            logger.error("Failed to refresh access token for user: {}. Response status: {}", email, response.getStatusCode());
            throw new RuntimeException("Failed to refresh access token");
        }
    }
}