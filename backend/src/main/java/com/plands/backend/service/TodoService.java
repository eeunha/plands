package com.plands.backend.service;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;

import java.util.List;

public interface TodoService {
    // 할 일 마스터 등록 및 식물 N개 매핑을 처리할 비즈니스 로직
    void registerTodo(TodoRequestDto todoRequestDto);

    // 할 일 종류 목록 전체 조회
    List<TodoTypeResponseDto> findTodoTypeList();

    // 특정 회원의 식물 목록 조회
    List<MemberPlantResponseDto> findMemberPlantList(Long memberId);

    // 할 일 삭제 비즈니스 로직 (Soft Delete)
    void removeTodo(Long todoId);

    // 할 일 수정
    void modifyTodo(Long todoId, TodoRequestDto todoRequestDto);
}
