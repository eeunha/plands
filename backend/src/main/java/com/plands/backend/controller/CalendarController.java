package com.plands.backend.controller;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.service.CalendarService;
import com.plands.backend.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final TodoService todoService;

    // 프론트(FullCalendar)가 요청하는 기간(startDate, endDate)을 파라미터로 직접 바인딩함
    @GetMapping("/todo")
    public ResponseEntity<List<CalendarResponseDto>> getTodoCalendarList(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {

        System.out.println("====== 컨트롤러 진입 ======");
        System.out.println("요청 파라미터 -> startDate: " + startDate + ", endDate: " + endDate);

        // 서비스 레이어를 호출하여 한 달 치 데이터 가득 담긴 상자 더미 수령
        List<CalendarResponseDto> todoList = calendarService.getCalendarList(startDate, endDate);

        // 상태 코드 200(OK)과 함께 프론트엔드로 응답 전송
        return ResponseEntity.ok(todoList);
    }

    // 새 일정 등록 API
    @PostMapping("/todo")
    public ResponseEntity<String> createTodo(@RequestBody TodoRequestDto todoRequestDto) { // RequestBody는 http body 내의 json 속 데이터를 dto에 매핑

        System.out.println("====== 일정 등록 컨트롤러 진입 ======");
        System.out.println("프론트에서 넘어온 데이터: " + todoRequestDto.toString());

        // 💡 @RequestBody가 프론트에서 쏜 JSON 데이터를 자바 DTO 객체(참조변수 주소값)로 찰떡같이 변환해줘!
        todoService.createTodo(todoRequestDto);

        // 💡 성공적으로 등록되면 200 OK 사인과 함께 완료 메시지 전송!
        return ResponseEntity.ok("일정이 성공적으로 등록되었습니다.");
    }
}
