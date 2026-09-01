package com.plands.backend.dto.request;

import lombok.*;

import java.util.List;

/**
 * 할 일 생성 및 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TodoRequestDto {

    /** 생성 완료 후 반환받을 할 일 PK (MyBatis auto-generated key) */
    private Long todoId;

    /** 회원 고유 번호 (PK) */
    private Long memberId;

    /** 할 일 종류 고유 번호 (PK) */
    private Long todoTypeId;

    /** 할 일 수행 지정 날짜 (YYYY-MM-DD) */
    private String dueDate;

    /** 연결할 회원 식물 고유 번호 목록 (PK List) */
    private List<Long> memberPlantIds;
}
