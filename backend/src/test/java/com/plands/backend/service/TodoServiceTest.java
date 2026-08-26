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
        String todayStr = today.toString(); // "YYYY-MM-DD"

        TodoRequestDto todoRequestDto = new TodoRequestDto();
        todoRequestDto.setMemberId(memberId);
        todoRequestDto.setTodoTypeId(1L);
        todoRequestDto.setDueDate(todayStr);

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
        // 1) 오늘 날짜(todayStr)의 할 일이 정상적으로 조회되는지 확인
        boolean hasTodayTodo = calendarList.stream()
                .anyMatch(calendar -> todayStr.equals(calendar.getStart()));

        assertThat(hasTodayTodo).isTrue();

        // 2) 새로 추가한 isDone 값이 기본값(false/0)으로 정상 설정되었는지 확인
        CalendarResponseDto targetTodo = calendarList.stream()
                .filter(calendar -> todayStr.equals(calendar.getStart()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("테스트용 할 일을 찾을 수 없습니다."));

        // DTO 필드명이 isDone (혹은 is_done 매핑 방식)에 맞춰 호출해주세요
        assertThat(targetTodo.getIsDone()).isFalse(); // 또는 0 검증
    }

    @Test
    @DisplayName("할 일 완료 상태 변경 테스트")
    void changeTodoStatusTest() {
        // given
        Long memberId = 1L;
        Long todoId = 64L;
        Boolean isDone = true;

        // when & then - 💡 예외가 발생하지 않고 무사히 실행되는지 검증
        org.assertj.core.api.Assertions.assertThatCode(() ->
                todoService.modifyTodoStatus(todoId, memberId, isDone)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("권한이 없는 유저가 상태 변경 시 예외 발생")
    void changeTodoStatus_Forbidden_Test() {
        // given
        Long wrongMemberId = 999L; // 존재하지 않는 유저 ID
        Long todoId = 64L;
        Boolean isDone = true;

        // when & then - 💡 NoSuchElementException 예외가 정상적으로 터지는지 검증
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                todoService.modifyTodoStatus(todoId, wrongMemberId, isDone)
        ).isInstanceOf(java.util.NoSuchElementException.class);
    }
}
