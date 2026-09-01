package com.plands.backend.service;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;

import java.util.List;

public interface TodoService {

    /**
     * 새로운 할 일을 등록하고 지정된 식물들과 매핑합니다.
     *
     * @param todoRequestDto 할 일 생성 요청 데이터 및 대상 식물 ID 리스트
     * @throws IllegalArgumentException 마스터 정보 등록에 실패하거나 식물 매핑 중 오류가 발생한 경우
     */
    void registerTodo(TodoRequestDto todoRequestDto);

    /**
     * 시스템에 등록된 할 일 종류 목록 전체를 조회합니다.
     *
     * @return 할 일 종류 응답 DTO 리스트
     */
    List<TodoTypeResponseDto> findTodoTypeList();

    /**
     * 특정 회원이 보유한 식물 목록을 조회합니다.
     *
     * @param memberId 조회할 회원의 고유 번호 (PK)
     * @return 회원의 식물 목록 응답 DTO 리스트
     */
    List<MemberPlantResponseDto> findMemberPlantList(Long memberId);

    /**
     * 기존 할 일 정보(내용, 날짜 등)를 수정하고 식물 매핑 정보를 재설정합니다.
     *
     * @param todoId         수정할 할 일 고유 번호 (PK)
     * @param todoRequestDto 수정할 할 일 정보 및 재매핑 대상 식물 ID 리스트
     * @throws IllegalArgumentException 대상 할 일이 존재하지 않거나 식물 재매핑 중 오류 발생 시
     */
    void modifyTodo(Long todoId, TodoRequestDto todoRequestDto);

    /**
     * 특정 할 일의 완료 여부(isDone) 상태를 변경합니다.
     *
     * @param todoId   수정할 할 일 고유 번호 (PK)
     * @param memberId 수정 요청자 회원 고유 번호 (PK)
     * @param isDone   변경할 완료 상태 값
     * @throws IllegalArgumentException 완료 상태(isDone) 값이 null인 경우
     * @throws java.util.NoSuchElementException 대상 할 일이 존재하지 않거나 수정 권한이 없는 경우
     */
    void modifyTodoStatus(Long todoId, Long memberId, Boolean isDone);

    /**
     * 특정 할 일을 논리 삭제(Soft Delete)하고 연관된 식물 매핑 데이터를 정리합니다.
     *
     * @param todoId   삭제할 할 일 고유 번호 (PK)
     * @param memberId 삭제 요청자 회원 고유 번호 (PK)
     * @throws IllegalArgumentException 이미 삭제되었거나 존재하지 않는 할 일인 경우
     * @throws SecurityException        본인이 작성한 할 일이 아닌 경우
     */
    void removeTodo(Long todoId, Long memberId);
}
