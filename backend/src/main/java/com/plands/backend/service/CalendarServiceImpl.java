package com.plands.backend.service;

import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // final이 붙은 todoMapper의 생성자를 자동으로 주입해 줌
public class CalendarServiceImpl implements CalendarService {

    private final TodoMapper todoMapper;

    @Override
    public List<CalendarResponseDto> getCalendarList(String startDate, String endDate) {
        System.out.println("====== 캘린더 서비스 레이어 진짜 DB 호출 ======");
        System.out.println("조회 기간(한 달): " + startDate + " ~ " + endDate);

        // 테스트용 계정 id 고정
        Long mockMemberId = 1L;

        // 가짜 데이터 mockList 로직은 싹 지우고, 진짜 한 달 치 쿼리 호출 결과를 리턴
        return todoMapper.selectCalendarList(mockMemberId, startDate, endDate);
    }
}
