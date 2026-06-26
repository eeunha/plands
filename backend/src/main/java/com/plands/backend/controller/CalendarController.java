package com.plands.backend.controller;

import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

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
}
