package com.plands.backend.mapper;

import com.plands.backend.dto.TodoDeleteTargetDto;
import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 할 일(Todo) 데이터베이스 매퍼 인터페이스
 */
@Mapper
public interface TodoMapper {

    /**
     * 지정된 기간 내의 월별 캘린더 및 할 일 목록을 조회합니다.
     *
     * @param memberId  조회할 회원 고유 번호 (PK)
     * @param startDate 조회 시작일 (YYYY-MM-DD)
     * @param endDate   조회 종료일 (YYYY-MM-DD)
     * @return 캘린더 일자별 할 일 응답 DTO 리스트
     */
    List<CalendarResponseDto> selectCalendarList(@Param("memberId") Long memberId,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    /**
     * 삭제 대상 할 일의 정보(작성자, 연관 데이터 등)를 조회합니다.
     *
     * @param todoId 삭제할 할 일 고유 번호 (PK)
     * @return 삭제 대상 정보 DTO
     */
    TodoDeleteTargetDto selectDeleteTargetById(Long todoId);

    /**
     * 새로운 할 일 기본 정보를 데이터베이스에 저장합니다.
     *
     * @param todoRequestDto 저장할 할 일 요청 DTO
     * @return 등록된 행(Row)의 개수
     */
    int insertTodo(TodoRequestDto todoRequestDto);

    /**
     * 생성된 할 일과 선택된 대표 식물 매핑 정보를 저장합니다.
     *
     * @param todoId        할 일 고유 번호 (PK)
     * @param memberPlantId 회원 식물 고유 번호 (PK)
     * @return 등록된 행(Row)의 개수
     */
    int insertTodoMemberPlant(@Param("todoId") Long todoId,
                              @Param("memberPlantId") Long memberPlantId);

    /**
     * 전체 할 일 종류(타입) 목록을 조회합니다.
     *
     * @return 할 일 종류 응답 DTO 리스트
     */
    List<TodoTypeResponseDto> selectTodoTypes();

    /**
     * 특정 회원이 등록한 식물 목록을 조회합니다.
     *
     * @param memberId 회원 고유 번호 (PK)
     * @return 회원의 식물 목록 응답 DTO 리스트
     */
    List<MemberPlantResponseDto> selectMemberPlants(Long memberId);

    /**
     * 할 일 기본 정보를 수정합니다.
     *
     * @param todoRequestDto 수정할 할 일 정보가 담긴 요청 DTO
     * @return 수정된 행(Row)의 개수
     */
    int updateTodo(TodoRequestDto todoRequestDto);

    /**
     * 할 일의 완료 상태(isDone)를 변경합니다.
     *
     * @param todoId   수정할 할 일 고유 번호 (PK)
     * @param memberId 작성자 회원 고유 번호 (PK)
     * @param isDone   완료 여부 (true/false)
     * @return 수정된 행(Row)의 개수
     */
    int updateTodoStatus(@Param("todoId") Long todoId,
                         @Param("memberId") Long memberId,
                         @Param("isDone") Boolean isDone);

    /**
     * 할 일 데이터를 논리 삭제(Soft Delete: is_deleted = 1) 처리합니다.
     *
     * @param todoId 삭제할 할 일 고유 번호 (PK)
     * @return 수정된 행(Row)의 개수
     */
    int updateTodoIsDeleted(Long todoId);

    /**
     * 할 일과 식물 간의 매핑 데이터를 데이터베이스에서 완전 삭제(Hard Delete)합니다.
     *
     * @param todoId 삭제할 할 일 고유 번호 (PK)
     * @return 삭제된 행(Row)의 개수
     */
    int deleteTodoMemberPlant(Long todoId);
}
