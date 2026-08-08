package com.plands.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 인수나 비즈니스 로직 위반 (예: 이미 존재하는 일기 등록 등) -> 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Business Exception: {}", e.getMessage());

        ErrorResponseDto response = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 서비스 레이어에 권한이 없는 경우
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponseDto> handleSecurityException(SecurityException e) {
        log.warn("Security Exception (Forbidden): {}", e.getMessage());

        ErrorResponseDto response = new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403 Forbidden 응답
    }

    // 그 외 서버 내부의 모든 예기치 못한 에러 -> 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception e) {
        log.error("Unexpected Server Error: {}", e.getMessage(), e);

        ErrorResponseDto response = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
