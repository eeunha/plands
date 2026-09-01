package com.plands.backend.dto.response;

import lombok.*;

/**
 * 할 일 종류 목록 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TodoTypeResponseDto {

    /** 할 일 종류 고유 번호 (PK) */
    private Long todoTypeId;

    /** 종류 이름 (ex: 물주기, 분갈이) */
    private String typeName;

    /** 할 일 종류별 고유 색상값 (ex: #FFD3D3) */
    private String colorCode;
}
