package com.plands.backend.service;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import com.plands.backend.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;

    @Override
    @Transactional // 하나라도 실패하면 롤백해주는 안전장치
    public void createTodo(TodoRequestDto todoRequestDto) {

        // 1. 마스터 테이블(todo)에 인서트 -> todoId가 가방 주소에 자동으로 채워짐
        todoMapper.insertTodo(todoRequestDto);

        // 2. 복수형 리스트(memberPlantIds)가 비어있지 않은지 체크!
        if (todoRequestDto.getMemberPlantIds() != null && !todoRequestDto.getMemberPlantIds().isEmpty()) {

            // 💡 리스트 안에서 단수형 'memberPlantId'를 하나씩 쏙쏙 꺼내서 매퍼로 던집니다!
            for (Long memberPlantId : todoRequestDto.getMemberPlantIds()) {
                todoMapper.insertTodoMemberPlant(todoRequestDto.getTodoId(), memberPlantId);
            }
        }
    }

    @Override
    public List<TodoTypeResponseDto> getTodoTypeList() {
        return todoMapper.selectTodoTypes();
    }

    @Override
    public List<MemberPlantResponseDto> getMemberPlantList(Long memberId) {
        return todoMapper.selectMemberPlants(memberId);
    }
}
