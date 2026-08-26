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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    // 프론트(FullCalendar)가 요청하는 기간(startDate, endDate)을 파라미터로 직접 바인딩함
    @GetMapping
    public ResponseEntity<List<CalendarResponseDto>> getTodoCalendarList(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {

        log.info("====== 달력 목록 조회 컨트롤러 진입 ======");

        Long memberId = securityUtils.getCurrentMemberId();

        log.debug("🔍 DB에서 조회된 진짜 회원 번호(memberId) -> {}", memberId);
        log.debug("요청 파라미터 -> startDate: {}, endDate: {}", startDate, endDate);

        // 서비스 레이어를 호출하여 한 달 치 데이터 가득 담긴 상자 더미 수령
        List<CalendarResponseDto> todoList = calendarService.findCalendarList(memberId, startDate, endDate);

        // 상태 코드 200(OK)과 함께 프론트엔드로 응답 전송
        return ResponseEntity.ok(todoList);
    }

    // 새 할 일 등록 API
    @PostMapping
    public ResponseEntity<String> createTodo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody TodoRequestDto todoRequestDto) { // RequestBody는 http body 내의 json 속 데이터를 dto에 매핑

        log.info("====== 할 일 등록 컨트롤러 진입 ======");
        log.debug("프론트에서 넘어온 데이터: {}", todoRequestDto.toString());

        Long memberId = securityUtils.getCurrentMemberId();
        todoRequestDto.setMemberId(memberId);

        // 💡 @RequestBody가 프론트에서 쏜 JSON 데이터를 자바 DTO 객체(참조변수 주소값)로 찰떡같이 변환해줘!
        todoService.registerTodo(todoRequestDto);

        return ResponseEntity.ok("할 일이 성공적으로 등록되었습니다.");
    }

    // 할 일 종류 목록 전체 조회 API
    @GetMapping("/type")
    public ResponseEntity<List<TodoTypeResponseDto>> getTodoTypes() {
        log.info("====== 할 일 종류 조회 컨트롤러 진입 ======");

        List<TodoTypeResponseDto> list = todoService.findTodoTypeList();

        return ResponseEntity.ok(list);
    }

    // 할 일 수정 API
    @PutMapping("/{todoId}")
    public ResponseEntity<String> updateTodo(@PathVariable Long todoId, @RequestBody TodoRequestDto todoRequestDto) {

        log.info("====== 할 일 수정 컨트롤러 진입 ======");
        log.debug("수정할 할 일 ID: {}", todoId);

        todoService.modifyTodo(todoId, todoRequestDto);

        return ResponseEntity.ok("할 일이 성공적으로 수정되었습니다.");
    }

    // 할 일 완료 상태 변경 API
    @PatchMapping("/{todoId}/status")
    public ResponseEntity<Void> updateTodoStatus(
            @PathVariable Long todoId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TodoStatusRequestDto requestDto) {
        log.info("====== 할 일 완료 상태 변경 컨트롤러 진입 ======");
        log.debug("상태를 변경할 할 일 ID: {}", todoId);

        Long memberId = securityUtils.getCurrentMemberId();

        todoService.modifyTodoStatus(todoId, memberId, requestDto.getIsDone());

        return ResponseEntity.ok().build();
    }

    // 할 일 삭제 API (Soft Delete)
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long todoId, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("====== 할 일 삭제 컨트롤러 진입 ======");
        log.debug("프론트에서 넘어온 삭제 대상 ID: {}", todoId);

        Long memberId = securityUtils.getCurrentMemberId();

        todoService.removeTodo(todoId, memberId);

        return ResponseEntity.ok().build();
    }
}
