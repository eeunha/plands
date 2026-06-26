package com.plands.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberPlantResponseDto {
    private Long memberPlantId;  // 회원의 식물 고유 번호
    private String plantName;     // 식물 품종명 (ex: "몬스테라")
}
