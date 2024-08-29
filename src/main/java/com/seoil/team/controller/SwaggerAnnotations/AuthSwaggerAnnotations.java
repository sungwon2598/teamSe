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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.hibernate.validator.constraints.Currency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AuthSwaggerAnnotations {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "잘못된 이메일",
                                                    value = """
                            {
                              "errors": [
                                {
                                  "field": "email",
                                  "message": "올바른 이메일 형식이어야 합니다"
                                }
                              ]
                            }
                        """
                                            ),
                                            @ExampleObject(
                                                    name = "이미 존재하는 이메일",
                                                    value = """
                            {
                              "errors": [
                                {
                                  "field": "email",
                                  "message": "이미 사용 중인 이메일입니다"
                                }
                              ]
                            }
                        """
                                            ),
                                            @ExampleObject(
                                                    name = "잘못된 이름",
                                                    value = """
                            {
                              "errors": [
                                {
                                  "field": "name",
                                  "message": "이름은 한글 또는 영문자만 포함해야 합니다"
                                }
                              ]
                            }
                        """
                                            ),
                                            @ExampleObject(
                                                    name = "이름의 길이",
                                                    value = """
                            {
                              "errors": [
                                {
                                  "field": "name",
                                  "message": "이름은 반드시 2자 이상 50자 이하여야 합니다"
                                }
                              ]
                            }
                        """
                                            ),
                                            @ExampleObject(
                                                    name = "잘못된 비밀번호",
                                                    value = """
                            {
                              "errors": [
                                {
                                  "field": "password",
                                  "message": "비밀번호는 숫자, 소문자, 대문자, 특수문자를 각각 하나 이상 포함해야 합니다"
                                }
                              ]
                            }
                        """
                                            ),
                                            @ExampleObject(
                                                    name = "비밀번호의 길이",
                                                    value = """
                            {
                              "errors": [
                                {
                                  "field": "password",
                                  "message": "비밀번호는 반드시 8자 이상 40자 이하여야 합니다"
                                }
                              ]
                            }
                        """
                                            )

                                    }
                            )
                    }
            )
    })
    public @interface SignupDocumentation {
        @Parameter(description = "사용자 등록 정보", required = true,
                content = @Content(examples = {
                        @ExampleObject(name = "valid", value = """
                           {
                             "email": "user@example.com",
                             "password": "strongPassword123!",
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
            description = "이메일과 비밀번호를 사용하여 인증하고 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 및 JWT 토큰 반환",
                    headers = @Header(name = "Authorization", description = "Bearer JWT 토큰"),
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "잘못된 자격 증명",
                                    value = """
                                {
                                  "errors": [
                                    {
                                      "field": "credentials",
                                      "message": "잘못된 이메일 또는 비밀번호입니다."
                                    }
                                  ]
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "사용자 없음",
                                    value = """
                                {
                                  "errors": [
                                    {
                                      "field": "email",
                                      "message": "해당 사용자가 존재하지 않습니다."
                                    }
                                  ]
                                }
                                """
                            )
                    )
            )
    })
    public @interface LoginDocumentation {
        @Parameter(description = "로그인 정보", required = true,
                content = @Content(examples = {
                        @ExampleObject(name = "valid", value = """
                           {
                             "email": "user@example.com",
                             "password": "strongPassword123!"
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
            description = "JWT 토큰을 사용하여 인증된 사용자의 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = UserInfoResponse.class),
                            examples = @ExampleObject(
                                    name = "성공적인 응답",
                                    value = """
                                {
                                  "id": 1,
                                  "name": "홍길동",
                                  "email": "user@example.com",
                                  "roles": ["USER"]
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패 응답",
                                    value = """
                                {
                                  "error": "Unauthorized",
                                  "message": "인증되지 않은 사용자입니다.",
                                }
                                """
                            )
                    )
            )
    })
    public @interface UserInfoDocumentation {}
}