package com.plands.backend.controller;

import com.plands.backend.auth.SecurityUtils;
import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.request.TodoStatusRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import com.plands.backend.service.CalendarService;
import com.plands.backend.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final CalendarService calendarService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<CalendarResponseDto>> getTodoCalendarList(@RequestParam("startDate") String startDate,
                                                                         @RequestParam("endDate") String endDate) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("달력 목록 조회 요청 - memberId: {}, range: {} ~ {}", memberId, startDate, endDate);

        List<CalendarResponseDto> todoList = calendarService.findCalendarList(memberId, startDate, endDate);

        return ResponseEntity.ok(todoList);
    }

    @PostMapping
    public ResponseEntity<Void> createTodo(@Valid @RequestBody TodoRequestDto todoRequestDto) {

        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("할 일 등록 요청 - request: {}, memberId: {}", todoRequestDto, memberId);

        todoService.registerTodo(memberId, todoRequestDto);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/type")
    public ResponseEntity<List<TodoTypeResponseDto>> getTodoTypes() {
        log.debug("할 일 종류 목록 전체 조회");

        List<TodoTypeResponseDto> list = todoService.findTodoTypeList();

        return ResponseEntity.ok(list);
    }

    @PutMapping("/{todoId}")
    public ResponseEntity<Void> updateTodo(@PathVariable Long todoId,
                                           @Valid @RequestBody TodoRequestDto todoRequestDto) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("할 일 수정 요청 - todoId: {}, memberId: {}", todoId, memberId);

        todoService.modifyTodo(todoId, memberId, todoRequestDto);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{todoId}/status")
    public ResponseEntity<Void> updateTodoStatus(@PathVariable Long todoId,
                                                 @Valid @RequestBody TodoStatusRequestDto requestDto) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("할 일 완료 상태 변경 요청 - todoId: {}, memberId: {}", todoId, memberId);

        todoService.modifyTodoStatus(todoId, memberId, requestDto.getIsDone());

        return ResponseEntity.ok().build();
    }

    // DB 데이터 보존을 위해 Soft Delete(논리 삭제) 방식으로 처리됨
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long todoId) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("할 일 삭제 요청 - todoId: {}, memberId: {}", todoId, memberId);

        todoService.removeTodo(todoId, memberId);

        return ResponseEntity.ok().build();
    }
}
