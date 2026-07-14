package com.plands.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TodoTypeResponseDto {
    private Long todoTypeId;   // 할 일 종류 고유 번호 (ex: 1, 2)
    private String typeName;   // 종류 이름 (ex: "물주기", "분갈이")
    private String colorCode;  // 할 일 종류별 고유 색상값 (ex: "#FFD3D3)
}
