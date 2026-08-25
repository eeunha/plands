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
            log.warn("등록 실패: 마스터 테이블 인서트 중 오류 발생 (memberPlantId = {})", todoRequestDto.getMemberId());
            throw new IllegalArgumentException("할 일 마스터 정보 등록에 실패했습니다.");
        }

        // 2. 복수형 리스트(memberPlantIds)가 비어있지 않은지 체크!
        if (todoRequestDto.getMemberPlantIds() != null && !todoRequestDto.getMemberPlantIds().isEmpty()) {

            // 💡 리스트 안에서 단수형 'memberPlantId'를 하나씩 쏙쏙 꺼내서 매퍼로 던집니다!
            for (Long memberPlantId : todoRequestDto.getMemberPlantIds()) {
                int insertedRows = todoMapper.insertTodoMemberPlant(todoRequestDto.getTodoId(), memberPlantId);

                // 루프 안에서 인서트가 한 건이라도 실패하면 바로 전체 실패 및 롤백
                if (insertedRows <= 0) {
                    // 💡 System.out 대신 log.error 사용, 예외를 던져서 트랜잭션 롤백 유도
                    log.warn("등록 실패: 식물 매핑 인서트 중 오류 발생 (memberPlantId = {})", memberPlantId);
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
    @Transactional // 💡 마스터 수정, 매핑 삭제 및 재등록이 한 세트이므로 트랜잭션 필수!
    public void modifyTodo(Long todoId, TodoRequestDto todoRequestDto) {
        log.info("====== 할 일 수정 서비스 레이어 진입 ======");

        // 쿼리에서 사용 가능하게 todoId 강제로 DTO에 넣어주기
        todoRequestDto.setTodoId(todoId);

        // 1. 마스터 테이블(todo) 데이터 수정
        int affectedRows = todoMapper.updateTodo(todoRequestDto);
        if (affectedRows != 1) {
            log.warn("수정 실패: 존재하지 않는 할 일 ID = {}", todoId);
            throw new IllegalArgumentException("존재하지 않는 할 일 ID입니다: " + todoId);
        }

        // 2. 매핑 테이블(todo_member_plant)에서 기존에 맵핑되어 있던 식물들 삭제
        todoMapper.deleteTodoMemberPlant(todoId);

        // 3. 수정창에서 새로 선택되어서 넘어온 식물 ID 리스트가 있다면 다시 인서트(재매핑)
        if (todoRequestDto.getMemberPlantIds() != null && !todoRequestDto.getMemberPlantIds().isEmpty()) {
            for (Long memberPlantId : todoRequestDto.getMemberPlantIds()) {
                int insertRows = todoMapper.insertTodoMemberPlant(todoId, memberPlantId);

                // 만약 식물을 넣으라고 지시했는데 DB에 인서트가 안 됐다(0)? 이건 문제 있는 상황!
                if (insertRows != 1) {
                    log.warn("수정 실패: 식물 매핑 인서트 중 오류 발생 (memberPlantId = {})", memberPlantId);
                    throw new IllegalArgumentException("식물 매핑 정보 수정 실패: id = " + memberPlantId);
                }
            }
        }
        log.info("====== 할 일 수정 및 식물 재매핑 완료 ======");
    }

    /**
     * 할 일 완료 상태 변경
     */
    @Override
    @Transactional
    public void modifyTodoStatus(Long todoId, Long memberId, Boolean isDone) {
        log.info("====== 할 일 완료 상태 변경 서비스 진입 ======");
        log.debug("todoId: {}, memberId: {}, isDone: {}", todoId, memberId, isDone);

        if (isDone == null) {
            throw new IllegalArgumentException("완료 상태(isDone) 값은 필수입니다.");
        }

        int affectedRows = todoMapper.updateTodoStatus(todoId, memberId, isDone);

        if (affectedRows == 0) {
            throw new NoSuchElementException("해당 할 일을 찾을 수 없거나 수정 권한이 없습니다.");
        }

        log.info("할 일 완료 상태 변경 성공 (todoId: {}, isDone: {})", todoId, isDone);
    }

    @Override
    @Transactional
    public void removeTodo(Long todoId, Long memberId) {
        log.info("====== 할 일 삭제 서비스 레이어 진입 (todoId: {}) ======", todoId);

        // ----------------------------------------------------
        // Step 1: 삭제 대상 데이터 사전 조회 (존재 여부 및 권한 검증용)
        // ----------------------------------------------------
        TodoDeleteTargetDto target = todoMapper.selectDeleteTargetById(todoId);

        // 1-1. 할 일 존재 여부 확인
        if (target == null) {
            log.warn("삭제 실패: 존재하지 않는 할 일 ID = {}", todoId);
            throw new IllegalArgumentException("존재하지 않거나 이미 삭제된 할 일입니다. ID: " + todoId);
        }

        // 1-2. 본인 글인지 권한 검증 (Security UserDetails에서 온 ID 비교)
        if (!target.getMemberId().equals(memberId)) {
            log.warn("권한 부족: 유효하지 않은 사용자의 삭제 시도 (요청 유저 ID: {}, 글 작성자 ID: {}, 할 일 ID: {})", memberId, target.getMemberId(), todoId);
            throw new SecurityException("해당 할 일을 삭제할 권한이 없습니다.");
        }

        // ----------------------------------------------------
        // Step 2: Soft Delete 진행
        // ----------------------------------------------------
        int affectedRows = todoMapper.updateTodoIsDeleted(todoId);

        // 만약 삭제된 행이 1개가 아니라면 예외 처리
        if (affectedRows != 1) {
            log.error("DB 에러: 투두 Soft Delete 실패 (affectedRows: {})", affectedRows);
            throw new IllegalArgumentException("존재하지 않는 할 일 ID입니다: " + todoId);
        }

        // ----------------------------------------------------
        // Step 3: 매핑 테이블 청소
        // ----------------------------------------------------
        todoMapper.deleteTodoMemberPlant(todoId);

        log.info("====== 할 일 삭제 및 매핑 데이터 정리 완료 (todoId: {}) ======", todoId);
    }
}
