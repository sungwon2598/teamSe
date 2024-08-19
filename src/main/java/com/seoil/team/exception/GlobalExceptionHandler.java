package com.seoil.team.exception;

import com.seoil.team.exception.Auth.EmailAlreadyExistsException;
import com.seoil.team.exception.Auth.UserNotFoundException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    public static final String EMAIL_ALREADY_EXISTS_ERROR = "이메일이 이미 존재합니다";
    public static final String USER_NOT_FOUND_ERROR = "회원을 찾지 못했습니다";
    public static final String ERROR_KEY = "error";
    public static final String MESSAGE_KEY = "message";

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ApiResponse(responseCode = "400", description = "이메일 중복",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR_KEY, EMAIL_ALREADY_EXISTS_ERROR);
        response.put(MESSAGE_KEY, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR_KEY, USER_NOT_FOUND_ERROR);
        response.put(MESSAGE_KEY, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}