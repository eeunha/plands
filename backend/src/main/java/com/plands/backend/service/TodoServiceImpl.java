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

    @Override
    @Transactional // 💡 마스터 수정과 매핑 삭제가 한 세트로 묶여야 하므로 트랜잭션 필수!
    public void deleteTodo(Long todoId) {
        System.out.println("====== 할 일 삭제 서비스 레이어 진입 ======");
        System.out.println("삭제할 투두 ID: " + todoId);

        // 1. 원칙 1: 마스터 테이블(todo)의 is_deleted를 1로 변경 (Soft Delete)
        todoMapper.updateTodoIsDeleted(todoId);

        // 2. 원칙 2: 매핑 테이블(todo_member_plant)에서 해당 todo_id와 연관된 데이터 가차없이 삭제 (Hard Delete)
        // 💡 어차피 마스터가 지워져서 화면엔 안 나오지만, 불필요한 매핑 찌꺼기 데이터를 깔끔하게 정리하는 실무 정석!
        todoMapper.deleteTodoMemberPlant(todoId);

        System.out.println("====== 할 일 삭제 및 매핑 데이터 정리 완료 ======");
    }
}
