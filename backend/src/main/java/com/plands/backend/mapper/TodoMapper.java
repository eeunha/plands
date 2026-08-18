package com.plands.backend.mapper;

import com.plands.backend.dto.TodoDeleteTargetDto;
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

    // 삭제 대상 할 일의 정보를 조회
    TodoDeleteTargetDto selectDeleteTargetById(@Param("todoId") Long todoId);

    // todo 테이블에 기본 할 일 정보 저장
    int insertTodo(TodoRequestDto todoRequestDto);

    // 생성된 todoId와 선택된 식물 ID 한 쌍을 매핑 테이블에 저장
    int insertTodoMemberPlant(@Param("todoId") Long todoId, @Param("memberPlantId") Long memberPlantId);

    // 할 일 종류 목록 전체 조회
    List<TodoTypeResponseDto> selectTodoTypes();

    // 특정 회원의 식물 목록 조회
    List<MemberPlantResponseDto> selectMemberPlants(@Param("memberId") Long memberId); // 매개변수가 하나라 @Param 안써도 됨. 2개이상 시 필수

    // 할 일 마스터 데이터 수정 쿼리 호출용
    int updateTodo(TodoRequestDto todoRequestDto);

    // 할 일 마스터 상태 논리 삭제 (is_deleted = 1로 변경)
    int updateTodoIsDeleted(@Param("todoId") Long todoId);

    // 할 일 매핑 데이터 완전 삭제 (Hard Delete)
    int deleteTodoMemberPlant(@Param("todoId") Long todoId);
}
