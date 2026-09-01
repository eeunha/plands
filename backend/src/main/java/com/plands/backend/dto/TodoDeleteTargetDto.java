package com.plands.backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 할 일 삭제 권한 검증용 DTO
 */
@Getter
@Setter
public class TodoDeleteTargetDto {

    /** 할 일 고유 번호 (PK) */
    private Long todoId;

    /** 삭제를 요청한 회원 고유 번호 (작성자 검증용 PK) */
    private Long memberId;
}
