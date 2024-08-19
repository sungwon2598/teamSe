package com.seoil.team.controller.SwaggerAnnotations;

import com.seoil.team.dto.response.ErrorResponse;
import com.seoil.team.dto.response.Auth.LoginResponseDto;
import com.seoil.team.dto.response.Auth.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AuthSwaggerAnnotations {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "새 사용자 등록",
            description = "이메일, 비밀번호, 이름 등의 정보를 사용하여 새 사용자를 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이메일 중복",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public @interface SignupDocumentation {
        @Parameter(description = "사용자 등록 정보", required = true,
                content = @Content(examples = {
                        @ExampleObject(name = "valid", value = """
                           {
                             "email": "user@example.com",
                             "password": "password123",
                             "name": "홍길동"
                           }
                           """),
                        @ExampleObject(name = "invalid", value = """
                           {
                             "email": "invalid-email",
                             "password": "short",
                             "name": ""
                           }
                           """)
                }))
        String value() default "";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "사용자 인증",
            description = "이메일과 비밀번호를 사용하여 인증하고 JWT 토큰을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공 및 JWT 토큰 반환",
                            headers = @Header(name = "Authorization", description = "Bearer JWT 토큰"),
                            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public @interface LoginDocumentation {
        @Parameter(description = "로그인 정보", required = true,
                content = @Content(examples = {
                        @ExampleObject(name = "valid", value = """
                           {
                             "email": "user@example.com",
                             "password": "password123"
                           }
                           """),
                        @ExampleObject(name = "invalid", value = """
                           {
                             "email": "invalid-email",
                             "password": ""
                           }
                           """)
                }))
        String value() default "";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "사용자 정보 조회",
            description = "JWT 토큰을 사용하여 인증된 사용자의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public @interface UserInfoDocumentation {}
}