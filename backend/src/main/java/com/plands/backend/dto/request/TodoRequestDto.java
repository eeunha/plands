package com.plands.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

/**
 * 할 일 생성 및 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TodoRequestDto {

    /** 할 일 종류 고유 번호 (PK) */
    @NotNull(message = "할 일 종류는 필수 선택 항목입니다.")
    private Long todoTypeId;

    /** 할 일 수행 지정 날짜 (YYYY-MM-DD) */
    @NotBlank(message = "수행 지정 날짜는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.")
    private String dueDate;

    /** 연결할 회원 식물 고유 번호 목록 (PK List) */
    @NotEmpty(message = "최소 하나 이상의 식물을 선택해야 합니다.")
    private List<Long> memberPlantIds;
}
