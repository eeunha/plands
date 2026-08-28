package com.plands.backend.controller;

import com.plands.backend.auth.SecurityUtils;
import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.request.TodoStatusRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import com.plands.backend.service.CalendarService;
import com.plands.backend.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 할 일 (Todo) 관련 API 요청을 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    private final CalendarService calendarService;
    private final SecurityUtils securityUtils;

    /**
     * 특정 기간(startDate ~ endDate) 동안의 할 일 목록을 조회
     */
    @GetMapping
    public ResponseEntity<List<CalendarResponseDto>> getTodoCalendarList(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("달력 목록 조회 - memberId: {}, range: {} ~ {}", memberId, startDate, endDate);

        List<CalendarResponseDto> todoList = calendarService.findCalendarList(memberId, startDate, endDate);

        return ResponseEntity.ok(todoList);
    }

    /**
     * 새 할 일 등록
     */
    @PostMapping
    public ResponseEntity<String> createTodo(@RequestBody TodoRequestDto todoRequestDto) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("할 일 등록 - memberId: {}", memberId);

        todoRequestDto.setMemberId(memberId);
        todoService.registerTodo(todoRequestDto);

        return ResponseEntity.ok("할 일이 성공적으로 등록되었습니다.");
    }

    /**
     * 할 일 종류 목록 전체를 조회
     */
    @GetMapping("/type")
    public ResponseEntity<List<TodoTypeResponseDto>> getTodoTypes() {
        log.debug("할 일 종류 목록 전체 조회");

        List<TodoTypeResponseDto> list = todoService.findTodoTypeList();

        return ResponseEntity.ok(list);
    }

    /**
     * 기존 할 일 정보(내용, 날짜 등)를 수정
     */
    @PutMapping("/{todoId}")
    public ResponseEntity<String> updateTodo(@PathVariable Long todoId, @RequestBody TodoRequestDto todoRequestDto) {
        log.debug("할 일 수정 - todoId: {}", todoId);

        todoService.modifyTodo(todoId, todoRequestDto);

        return ResponseEntity.ok("할 일이 성공적으로 수정되었습니다.");
    }

    /**
     * 특정 할 일 완료상태를 변경
     */
    @PatchMapping("/{todoId}/status")
    public ResponseEntity<Void> updateTodoStatus(
            @PathVariable Long todoId,
            @RequestBody TodoStatusRequestDto requestDto) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("할 일 완료 상태 변경 - todoId: {}, memberId: {}", todoId, memberId);

        todoService.modifyTodoStatus(todoId, memberId, requestDto.getIsDone());

        return ResponseEntity.ok().build();
    }

    /**
     * 특정 할 일을 삭제 (Soft Delete 적용)
     */
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long todoId) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("할 일 삭제 - todoId: {}, memberId: {}", todoId, memberId);

        todoService.removeTodo(todoId, memberId);

        return ResponseEntity.ok().build();
    }
}
