package com.plands.backend.service;

import com.plands.backend.dto.request.TodoRequestDto;

public interface TodoService {
    // 일정 마스터 등록 및 식물 N개 매핑을 처리할 비즈니스 로직
    void createTodo(TodoRequestDto todoRequestDto);
}
