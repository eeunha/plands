package com.plands.backend.dto.request;

import lombok.*;

/**
 * 할 일 완료 상태 변경 요청 DTO
 */
@Getter
public class TodoStatusRequestDto {

    /** 완료 여부 (true: 완료, false: 미완료) */
    private Boolean isDone;
}
