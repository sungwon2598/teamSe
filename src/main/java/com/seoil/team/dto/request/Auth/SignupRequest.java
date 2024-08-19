package com.seoil.team.dto.request.Auth;

import com.seoil.team.domain.member.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 데이터")
public record SignupRequest(
        @Schema(description = "사용자 이름", example = "홍길동",
                minLength = Name.MIN_LENGTH, maxLength = Name.MAX_LENGTH)
        @NotBlank(message = ErrorMessages.NAME_REQUIRED)
        @Size(min = Name.MIN_LENGTH, max = Name.MAX_LENGTH, message = Name.MESSAGE)
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = ErrorMessages.NAME_PATTERN)
        String name,

        @Schema(description = "이메일 주소", example = "user@example.com")
        @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
        @Email(message = ErrorMessages.EMAIL_INVALID)
        String email,

        @Schema(description = "비밀번호", example = "strongPassword123!",
                minLength = Password.MIN_LENGTH, maxLength = Password.MAX_LENGTH)
        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        @Size(min = Password.MIN_LENGTH, max = Password.MAX_LENGTH, message = Password.MESSAGE)
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$", message = ErrorMessages.PASSWORD_PATTERN)
        String password,

        @Schema(description = "사용자 역할", defaultValue = "USER", example = "USER")
        RoleType role
) {
    public static final class Name {
        public static final int MIN_LENGTH = 2;
        public static final int MAX_LENGTH = 50;
        public static final String MESSAGE =
                "이름은 반드시 " + MIN_LENGTH + "자 이상 " + MAX_LENGTH + "자 이하여야 합니다";
    }

    public static final class Password {
        public static final int MIN_LENGTH = 8;
        public static final int MAX_LENGTH = 40;
        public static final String MESSAGE =
                "비밀번호는 반드시 " + MIN_LENGTH + "자 이상 " + MAX_LENGTH + "자 이하여야 합니다";
    }

    public static final class ErrorMessages {
        public static final String NAME_REQUIRED = "이름은 필수입니다";
        public static final String NAME_PATTERN = "이름은 한글 또는 영문자만 포함해야 합니다";
        public static final String EMAIL_REQUIRED = "이메일은 필수입니다";
        public static final String EMAIL_INVALID = "올바른 이메일 형식이어야 합니다";
        public static final String PASSWORD_REQUIRED = "비밀번호는 필수입니다";
        public static final String PASSWORD_PATTERN = "비밀번호는 숫자, 소문자, 대문자, 특수문자를 각각 하나 이상 포함해야 합니다";
    }
}