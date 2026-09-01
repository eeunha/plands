package com.plands.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 할 일 목록에 매핑된 식물 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantInfoDto {

    /** 회원의 식물 고유 번호 (PK) */
    private Long memberPlantId;

    /** 화면에 표시할 식물 이름 */
    private String plantName;
}
