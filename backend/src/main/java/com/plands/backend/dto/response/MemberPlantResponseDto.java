package com.plands.backend.dto.response;

import lombok.*;

// TODO: MemberPlantResponseDto와 필드 구조 동일. 추후 요구사항 확장 여부 확인 후 하나의 DTO로 통합 검토 필요
/**
 * 회원 보유 식물 목록 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberPlantResponseDto {

    /** 회원의 식물 고유 번호 (PK) */
    private Long memberPlantId;

    /** 식물 품종명 (ex: 몬스테라) */
    private String plantName;
}
