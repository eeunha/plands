package com.plands.backend.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TodoRequestDto {
    // MyBatis가 인서트 성공 후 생성된 PK(todo_id)를 여기에 자동으로 꽂아줄 거야!
    private Long todoId;

    private Long memberId;            // 현재 로그인한 회원 번호
    private Long todoTypeId;          // 드롭다운에서 선택한 할 일 종류 고유 번호
    private String dueDate;           // 선택한 날짜 (ex: "2026-06-27")

    // 여러 식물을 담을 수 있게 Long 타입의 List로 받습니다.
    private List<Long> memberPlantIds;
}
