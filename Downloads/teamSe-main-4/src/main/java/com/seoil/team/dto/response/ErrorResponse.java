package com.seoil.team.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "에러 메시지") String message,
        @Schema(description = "HTTP 상태 코드") int status,
        @Schema(description = "필드별 에러 목록") List<FieldError> errors,
        @Schema(description = "에러 발생 시간") LocalDateTime timestamp
) {
    @Schema(description = "필드별 에러")
    public record FieldError(@Schema(description = "에러가 발생한 필드") String field,
                             @Schema(description = "에러 메시지") String message) {}

    public static ErrorResponse of(String message, int status, List<FieldError> errors) {
        return new ErrorResponse(message, status, errors, LocalDateTime.now());
    }

    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status, List.of(), LocalDateTime.now());
    }
}