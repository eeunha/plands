package com.plands.backend.mapper;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoMapper {
    // 한 달 치 데이터를 긁어오기 위해 startDate와 endDate를 기간으로 받음
    List<CalendarResponseDto> selectCalendarList(
            @Param("memberId") Long memberId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 1. todo 테이블에 기본 일정 정보 저장
    int insertTodo(TodoRequestDto todoRequestDto);

    // 2. 생성된 todoId와 선택된 식물 ID 한 쌍을 매핑 테이블에 저장
    int insertTodoMemberPlant(@Param("todoId") Long todoId, @Param("memberPlantId") Long memberPlantId);

    // 할 일 종류 목록 전체 조회
    List<TodoTypeResponseDto> selectTodoTypes();

    // 특정 회원의 식물 목록 조회
    List<MemberPlantResponseDto> selectMemberPlants(Long memberId); // 매개변수가 하나라 @Param 안써도 됨. 2개이상 시 필수
}
