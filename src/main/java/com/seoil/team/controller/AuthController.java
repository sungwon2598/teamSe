package com.seoil.team.controller;

import com.seoil.team.controller.SwaggerAnnotations.AuthSwaggerAnnotations.SignupDocumentation;
import com.seoil.team.controller.SwaggerAnnotations.AuthSwaggerAnnotations.LoginDocumentation;
import com.seoil.team.controller.SwaggerAnnotations.AuthSwaggerAnnotations.UserInfoDocumentation;
import com.seoil.team.domain.member.Member;
import com.seoil.team.dto.request.Auth.LoginRequest;
import com.seoil.team.dto.request.Auth.SignupRequest;
import com.seoil.team.dto.response.Auth.LoginResponseDto;
import com.seoil.team.dto.response.Auth.UserInfoResponse;
import com.seoil.team.repository.MemberRepository;
import com.seoil.team.service.AuthService;
import com.seoil.team.service.MemberService;
import com.seoil.team.service.CustomOAuth2UserService.OAuth2UserImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "사용자 인증 및 등록을 위한 API")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @SignupDocumentation
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        memberService.registerNewUser(signUpRequest);
        return ResponseEntity.ok("회원가입이 성공적으로 이루어졌습니다");
    }

    @UserInfoDocumentation
    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        UserInfoResponse userInfoResponse = authService.getUserInfo(authentication);
        return ResponseEntity.ok(userInfoResponse);
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }



    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String jwt = authService.authenticateUser(loginRequest);
        addJwtCookie(response, jwt);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/home")
    public String homePage(@AuthenticationPrincipal Object principal, Model model) {
        if (principal instanceof OAuth2UserImpl) {
            OAuth2UserImpl oauth2User = (OAuth2UserImpl) principal;
            model.addAttribute("member", oauth2User);
            log.info("OAuth2 User: {}", oauth2User.getName());
        } else if (principal instanceof Member) {
            Member member = (Member) principal;
            model.addAttribute("member", member);
            log.info("Regular User: {}", member.getName());
        } else if (principal instanceof String) {
            String email = (String) principal;
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
            model.addAttribute("member", member);
            log.info("User from JWT: {}", member.getName());
        } else {
            log.warn("Unknown principal type: {}", principal != null ? principal.getClass() : "null");
            return "redirect:/api/auth/login";
        }
        return "home";
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // JWT 쿠키 삭제
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().body("Logged out successfully");
    }

    private void addJwtCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // cookie.setSecure(true); // HTTPS를 사용하는 경우에만 활성화
        response.addCookie(cookie);
    }
}