package com.seoil.team.config;

import com.seoil.team.security.jwt.JwtTokenProvider;
import com.seoil.team.service.CustomOAuth2UserService.OAuth2UserImpl;
import com.seoil.team.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final MemberService memberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient("google",
                authentication.getName());

        if (authorizedClient != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

            logger.info("Access Token: " + accessToken.getTokenValue());
            if (refreshToken != null) {
                logger.info("Refresh Token: " + refreshToken.getTokenValue());
            } else {
                logger.warn("Refresh Token is null");
            }

            String jwt = jwtTokenProvider.createToken(authentication);

            logger.info("Created JWT: " + jwt);
            logger.info("User: " + authentication.getName());

            // Member DB에 리프레시 토큰 업데이트
            OAuth2UserImpl oAuth2User = (OAuth2UserImpl) authentication.getPrincipal();
            String email = oAuth2User.getEmail();
            String name = oAuth2User.getName();
            String provider = oAuth2User.getProvider();
            String providerId = oAuth2User.getProviderId();
            String accessTokenValue = accessToken.getTokenValue();
            String refreshTokenValue = refreshToken != null ? refreshToken.getTokenValue() : null;
            Instant tokenExpirationTime = accessToken.getExpiresAt();

            memberService.createOrUpdateOAuthMember(email, name, provider, providerId,
                    accessTokenValue, refreshTokenValue, tokenExpirationTime);

            Cookie cookie = new Cookie("jwt", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            getRedirectStrategy().sendRedirect(request, response, "/api/auth/home");
        } else {
            logger.warn("Authorized client not found for user: " + authentication.getName());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}