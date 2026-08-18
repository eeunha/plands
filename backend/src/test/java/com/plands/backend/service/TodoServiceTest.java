package com.plands.backend.service;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
public class TodoServiceTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private CalendarService calendarService;

    @Test
    @DisplayName("할 일 등록 및 조회 통합 테스트")
    void registerAndFindTodoTest() {
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        TodoRequestDto todoRequestDto = new TodoRequestDto();

        todoRequestDto.setMemberId(memberId);
        todoRequestDto.setTodoTypeId(1L);
        todoRequestDto.setDueDate("2026-08-18");

        List<Long> memberPlantIds = new ArrayList<>();
        memberPlantIds.add(1L);
        memberPlantIds.add(2L);
        todoRequestDto.setMemberPlantIds(memberPlantIds);

        // when: 2. 서비스의 할 일 등록 메서드 호출
        todoService.registerTodo(todoRequestDto);

        // when: 3. 등록한 날짜가 포함된 기간(어제~내일)으로 조회 메서드 호출
        String startDate = today.minusDays(1).toString(); // 예: "YYYY-MM-DD"
        String endDate = today.plusDays(1).toString();
        List<CalendarResponseDto> calendarList = calendarService.findCalendarList(memberId, startDate, endDate);

        // then: 4. 검증
        // 1) 2026-08-18 날짜의 할 일이 조회되는지 확인
        boolean hasTodayTodo = calendarList.stream()
                .anyMatch(calendar -> calendar.getStart().equals("2026-08-18"));

        assertThat(hasTodayTodo).isTrue();

        // 2) 새로 추가한 is_done(또는 isDone) 값이 정상적으로 가져와졌는지(기본값 0/false) 확인
        CalendarResponseDto targetTodo = calendarList.stream()
                .filter(calendar -> calendar.getStart().equals("2026-08-18"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("테스트용 할 일을 찾을 수 없습니다."));

        // DTO 필드명이 isDone (혹은 is_done 매핑 방식)에 맞춰 호출해주세요
        assertThat(targetTodo.getIsDone()).isFalse(); // 또는 0 검증
    }
}
