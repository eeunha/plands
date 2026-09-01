package com.plands.backend.service;

import com.plands.backend.dto.TodoDeleteTargetDto;
import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import com.plands.backend.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;

    @Override
    @Transactional
    public void registerTodo(TodoRequestDto todoRequestDto) {
        int affectedRows = todoMapper.insertTodo(todoRequestDto);
        if (affectedRows <= 0) {
            log.warn("등록 실패 - 마스터 정보 생성 오류: memberId = {}", todoRequestDto.getMemberId());
            throw new IllegalArgumentException("할 일 마스터 정보 등록에 실패했습니다.");
        }

        saveTodoMemberPlantMappings(todoRequestDto.getTodoId(), todoRequestDto.getMemberPlantIds());

        log.info("할 일 생성 완료: todoId={}, memberId={}", todoRequestDto.getTodoId(), todoRequestDto.getMemberId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoTypeResponseDto> findTodoTypeList() {
        return todoMapper.selectTodoTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberPlantResponseDto> findMemberPlantList(Long memberId) {
        return todoMapper.selectMemberPlants(memberId);
    }

    @Override
    @Transactional
    public void modifyTodo(Long todoId, TodoRequestDto todoRequestDto) {
        todoRequestDto.setTodoId(todoId);

        int affectedRows = todoMapper.updateTodo(todoRequestDto);
        if (affectedRows != 1) {
            log.warn("할 일 수정 실패: 존재하지 않는 할 일 ID: {}", todoId);
            throw new IllegalArgumentException("존재하지 않는 할 일 ID입니다: " + todoId);
        }

        todoMapper.deleteTodoMemberPlant(todoId);
        saveTodoMemberPlantMappings(todoId, todoRequestDto.getMemberPlantIds());

        log.info("할 일 수정 완료: todoId={}", todoId);
    }

    @Override
    @Transactional
    public void modifyTodoStatus(Long todoId, Long memberId, Boolean isDone) {
        if (isDone == null) {
            throw new IllegalArgumentException("완료 상태(isDone) 값은 필수입니다.");
        }

        int affectedRows = todoMapper.updateTodoStatus(todoId, memberId, isDone);
        if (affectedRows == 0) {
            log.warn("상태 변경 실패 - 권한 없음 또는 존재하지 않음: todoId={}, memberId={}", todoId, memberId);
            throw new NoSuchElementException("해당 할 일을 찾을 수 없거나 수정 권한이 없습니다.");
        }

        log.info("할 일 완료 상태 변경 성공 - todoId: {}, isDone: {}", todoId, isDone);
    }

    @Override
    @Transactional
    public void removeTodo(Long todoId, Long memberId) {
        validateDeleteTarget(todoId, memberId);

        int affectedRows = todoMapper.updateTodoIsDeleted(todoId);
        if (affectedRows != 1) {
            log.error("DB 오류 - Soft Delete 처리 실패 : todoId={}", todoId);
            throw new IllegalArgumentException("할 일 삭제 처리에 실패했습니다. ID: " + todoId);
        }

        todoMapper.deleteTodoMemberPlant(todoId);

        log.info("할 일 삭제 완료: todoId={}", todoId);
    }

    // =========================================================================
    // Helper Methods (검증 및 매핑 세부 로직 분리)
    // =========================================================================

    private void saveTodoMemberPlantMappings(Long todoId, List<Long> memberPlantIds) {
        if (memberPlantIds == null || memberPlantIds.isEmpty()) {
            return;
        }

        for (Long memberPlantId : memberPlantIds) {
            int insertedRows = todoMapper.insertTodoMemberPlant(todoId, memberPlantId);

            if (insertedRows <= 0) {
                log.warn("식물 매핑 등록 실패: todoId={}, memberPlantId={}", todoId, memberPlantId);
                throw new IllegalArgumentException("식물 매핑 정보 등록에 실패했습니다. id=" + memberPlantId);
            }
        }
    }

    private void validateDeleteTarget(Long todoId, Long memberId) {
        TodoDeleteTargetDto target = todoMapper.selectDeleteTargetById(todoId);

        if (target == null) {
            log.warn("삭제 실패 - 존재하지 않거나 이미 삭제된 할 일: todoId={}", todoId);
            throw new IllegalArgumentException("존재하지 않거나 이미 삭제된 할 일입니다. ID: " + todoId);
        }

        if (!target.getMemberId().equals(memberId)) {
            log.warn("삭제 실패 - 접근 권한 없음: todoId={}, memberId={}", todoId, memberId);
            throw new SecurityException("해당 할 일을 삭제할 권한이 없습니다.");
        }
    }
}
