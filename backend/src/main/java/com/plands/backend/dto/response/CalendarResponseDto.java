package com.plands.backend.dto.response;

import lombok.*;

import java.util.List;

/**
 * 달력 및 할 일 목록 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponseDto {

    /** 할 일 고유 번호 (PK) */
    private Long id;

    /** 할 일 종류 이름 (ex: 물주기, 분갈이) */
    private String title;

    /** 할 일 지정 날짜 (YYYY-MM-DD) */
    private String start;

    /** 완료 여부 (true: 완료, false: 미완료) */
    private Boolean isDone;

    /** 카테고리 색상 코드 (PK) */
    private String color;

    /** 할 일 종류 고유 번호 */
    private Long todoTypeId;

    /** 할 일에 연결된 회원 식물 목록 */
    private List<PlantInfoDto> plants;
}
