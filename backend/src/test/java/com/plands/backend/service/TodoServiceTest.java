package com.plands.backend.service;

import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        // given - 테스트용 할 일 요청 데이터 생성
        Long memberId = 1L;

        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        TodoRequestDto todoRequestDto = new TodoRequestDto();
        todoRequestDto.setMemberId(memberId);
        todoRequestDto.setTodoTypeId(1L);
        todoRequestDto.setDueDate(todayStr);

        List<Long> memberPlantIds = new ArrayList<>();
        memberPlantIds.add(1L);
        memberPlantIds.add(2L);
        todoRequestDto.setMemberPlantIds(memberPlantIds);

        // when - 할 일 등록 및 기간 조회 발생
        todoService.registerTodo(todoRequestDto);

        String startDate = today.minusDays(1).toString(); // 예: "YYYY-MM-DD"
        String endDate = today.plusDays(1).toString();
        List<CalendarResponseDto> calendarList = calendarService.findCalendarList(memberId, startDate, endDate);

        // then - 등록된 데이터 및 완료 상태(isDone) 기본값 검증
        boolean hasTodayTodo = calendarList.stream()
                .anyMatch(calendar -> todayStr.equals(calendar.getStart()));

        assertThat(hasTodayTodo).isTrue();

        CalendarResponseDto targetTodo = calendarList.stream()
                .filter(calendar -> todayStr.equals(calendar.getStart()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("테스트용 할 일을 찾을 수 없습니다."));

        assertThat(targetTodo.getIsDone()).isFalse();
    }

    @Test
    @DisplayName("할 일 완료 상태 변경 테스트")
    void changeTodoStatusTest() {
        // given - 정상 회원 및 할 일 ID 준비
        Long memberId = 1L;
        Long todoId = 64L;
        Boolean isDone = true;

        // when & then - 예외 발생 없이 무사히 완료되는지 검증
        assertThatCode(() -> todoService.modifyTodoStatus(todoId, memberId, isDone))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("권한이 없는 유저가 상태 변경 시 예외 발생")
    void changeTodoStatus_Forbidden_Test() {
        // given - 존재하지 않는 회원 ID 준비
        Long wrongMemberId = 999L;
        Long todoId = 64L;
        Boolean isDone = true;

        // when & then - NoSuchElementException 예외가 정상적으로 발생하는지 검증
        assertThatThrownBy(() -> todoService.modifyTodoStatus(todoId, wrongMemberId, isDone))
                .isInstanceOf(NoSuchElementException.class);
    }
}
