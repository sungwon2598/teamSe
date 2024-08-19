package com.seoil.team.controller;

import com.seoil.team.controller.SwaggerAnnotations.AuthSwaggerAnnotations.LoginDocumentation;
import com.seoil.team.controller.SwaggerAnnotations.AuthSwaggerAnnotations.UserInfoDocumentation;
import com.seoil.team.dto.request.Auth.LoginRequest;
import com.seoil.team.dto.request.Auth.SignupRequest;
import com.seoil.team.dto.response.Auth.LoginResponseDto;
import com.seoil.team.dto.response.Auth.UserInfoResponse;
import com.seoil.team.dto.response.ErrorResponse;
import com.seoil.team.service.AuthService;
import com.seoil.team.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "사용자 인증 및 등록을 위한 API")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 Role 형식",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        memberService.registerNewUser(signUpRequest);
        return ResponseEntity.ok("회원가입이 성공적으로 이루어졌습니다");
    }

    @LoginDocumentation
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String jwt = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(new LoginResponseDto(jwt));
    }

    @UserInfoDocumentation
    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        UserInfoResponse userInfoResponse = authService.getUserInfo(authentication);
        return ResponseEntity.ok(userInfoResponse);
    }
}