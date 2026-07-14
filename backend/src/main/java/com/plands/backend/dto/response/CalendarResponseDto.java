package com.plands.backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponseDto {
    // 1. 달력에 스티커 붙일 때 쓸 겉면 데이터
    private Long id;            // 할 일 고유 번호
    private String title;       // 할 일 종류 이름 (ex: 물주기, 분갈이)
    private String start;       // 할 일 날짜 (due_date)
    private String color;       // 카테고리 색상 코드
    private Long todoTypeId;    // 할 일 종류 고유 번호

    // 2. 우측 탭 아코디언 안에 쏙 들어갈 식물 리스트
    private List<PlantInfoDto> plants;
}
