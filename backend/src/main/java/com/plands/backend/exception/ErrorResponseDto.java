package com.plands.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 전역 예외 처리 공통 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    /** HTTP 상태 코드 (ex: 400, 403, 500) */
    private int status;

    /** 클라이언트에게 노출할 에러 상세 메시지 */
    private String message;
}
