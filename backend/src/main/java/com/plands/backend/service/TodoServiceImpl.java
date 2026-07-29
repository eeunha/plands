package com.plands.backend.service;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import com.plands.backend.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j // 💡 롬복 로깅 어노테이션 적용
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;

    @Override
    @Transactional // 하나라도 실패하면 롤백해주는 안전장치
    public void registerTodo(TodoRequestDto todoRequestDto) {

        // 1. 마스터 테이블(todo)에 인서트 -> todoId가 가방 주소에 자동으로 채워짐
        int affectedRows = todoMapper.insertTodo(todoRequestDto);

        if (affectedRows <= 0) {
            throw new org.springframework.dao.DataAccessException("할 일 마스터 정보 등록에 실패했습니다.") {};
        }

        // 2. 복수형 리스트(memberPlantIds)가 비어있지 않은지 체크!
        if (todoRequestDto.getMemberPlantIds() != null && !todoRequestDto.getMemberPlantIds().isEmpty()) {

            // 💡 리스트 안에서 단수형 'memberPlantId'를 하나씩 쏙쏙 꺼내서 매퍼로 던집니다!
            for (Long memberPlantId : todoRequestDto.getMemberPlantIds()) {
                int insertedRows = todoMapper.insertTodoMemberPlant(todoRequestDto.getTodoId(), memberPlantId);

                // 루프 안에서 인서트가 한 건이라도 실패하면 바로 전체 실패 및 롤백
                if (insertedRows <= 0) {
                    // 💡 System.out 대신 log.error 사용, 예외를 던져서 트랜잭션 롤백 유도
                    log.error("등록 실패: 식물 매핑 인서트 중 오류 발생 (memberPlantId = {})", memberPlantId);
                    throw new IllegalArgumentException("식물 매핑 정보 등록 실패: id = " + memberPlantId);
                }
            }
        }
    }

    @Override
    public List<TodoTypeResponseDto> findTodoTypeList() {
        return todoMapper.selectTodoTypes();
    }

    @Override
    public List<MemberPlantResponseDto> findMemberPlantList(Long memberId) {
        return todoMapper.selectMemberPlants(memberId);
    }

    @Override
    @Transactional // 💡 마스터 수정과 매핑 삭제가 한 세트로 묶여야 하므로 트랜잭션 필수!
    public void removeTodo(Long todoId) {
        log.info("====== 할 일 삭제 서비스 레이어 진입 (todoId: {}) ======", todoId);

        // 1. Soft Delete 진행
        int affectedRows = todoMapper.updateTodoIsDeleted(todoId);

        // 💡 만약 삭제된 행이 0개라면 존재하지 않거나 이미 지워진 타깃이므로 false 반환!
        if (affectedRows <= 0) {
            log.warn("삭제 실패: 존재하지 않거나 이미 삭제된 투두 ID = {}", todoId);
            throw new IllegalArgumentException("존재하지 않는 할 일 ID입니다: " + todoId);
        }
        // 2. 매핑 테이블 청소
        todoMapper.deleteTodoMemberPlant(todoId);

        log.info("====== 할 일 삭제 및 매핑 데이터 정리 완료 ======");
    }

    @Override
    @Transactional // 💡 마스터 수정, 매핑 삭제 및 재등록이 한 세트이므로 트랜잭션 필수!
    public void modifyTodo(Long todoId, TodoRequestDto todoRequestDto) {
        log.info("====== 할 일 수정 서비스 레이어 진입 ======");

        // 쿼리에서 사용 가능하게 todoId 강제로 DTO에 넣어주기
        todoRequestDto.setTodoId(todoId);

        // 1. 마스터 테이블(todo) 데이터 수정
        int affectedRows = todoMapper.updateTodo(todoRequestDto);
        if (affectedRows <= 0) {
            log.warn("수정 실패: 존재하지 않는 투두 ID = {}", todoId);
            throw new IllegalArgumentException("존재하지 않는 할 일 ID입니다: " + todoId);
        }

        // 2. 매핑 테이블(todo_member_plant)에서 기존에 맵핑되어 있던 식물들 삭제
        todoMapper.deleteTodoMemberPlant(todoId);

        // 3. 수정창에서 새로 선택되어서 넘어온 식물 ID 리스트가 있다면 다시 인서트(재매핑)
        if (todoRequestDto.getMemberPlantIds() != null && !todoRequestDto.getMemberPlantIds().isEmpty()) {
            for (Long memberPlantId : todoRequestDto.getMemberPlantIds()) {
                int insertRows = todoMapper.insertTodoMemberPlant(todoId, memberPlantId);

                // 만약 식물을 넣으라고 지시했는데 DB에 인서트가 안 됐다(0)? 이건 문제 있는 상황!
                if (insertRows <= 0) {
                    log.error("수정 실패: 식물 매핑 인서트 중 오류 발생 (memberPlantId = {})", memberPlantId);
                    throw new IllegalArgumentException("식물 매핑 정보 수정 실패: id = " + memberPlantId);
                }
            }
        }
        log.info("====== 할 일 수정 및 식물 재매핑 완료 ======");
    }
}
