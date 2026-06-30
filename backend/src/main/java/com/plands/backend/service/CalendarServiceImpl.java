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
    public List<CalendarResponseDto> getCalendarList(Long memberId, String startDate, String endDate) {
        System.out.println("====== 캘린더 서비스 레이어 DB 호출 ======");
        System.out.println("조회 유저 ID: " + memberId);
        System.out.println("조회 기간(한 달): " + startDate + " ~ " + endDate);

        return todoMapper.selectCalendarList(memberId, startDate, endDate);
    }
}
