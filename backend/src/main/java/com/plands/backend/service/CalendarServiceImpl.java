package com.plands.backend.service;

import com.plands.backend.dto.request.CalendarRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    @Override
    public List<CalendarResponseDto> getCalendarList(CalendarRequestDto calendarRequestDto) {
        // 프론트가 보낸 값 잘 넘어오나 확인용 로그
        System.out.println("====== 서비스 레이어 도착 ======");
        System.out.println("조회 타겟 연도: " + calendarRequestDto.getYear());
        System.out.println("조회 타겟 월: " + calendarRequestDto.getMonth());

        List<CalendarResponseDto> mockList = new ArrayList<>();

        // 가짜 데이터 1번
        CalendarResponseDto event1 = new CalendarResponseDto();
        event1.setCalendarId(1L);
        event1.setTitle("점심 굶고 코딩하는 은하");
        event1.setDate("2026-06-24");
        event1.setTime("11:50 AM");
        event1.setDescription("열정이 너무 넘쳐서 사수를 당황하게 만듦");
        mockList.add(event1);

        // 가짜 데이터 2번
        CalendarResponseDto event2 = new CalendarResponseDto();
        event2.setCalendarId(2L);
        event2.setTitle("오토코야마 사케 마시기");
        event2.setDate("2026-06-26");
        event2.setTime("08:00 PM");
        event2.setDescription("검은색 라벨에 흰색 글씨 써진 걸로 마시기 🍶");
        mockList.add(event2);

        return mockList;
    }

//    private final CalendarMapper calendarMapper;

//    public CalendarServiceImpl(CalendarMapper calendarMapper) {
//        this.calendarMapper = calendarMapper;
//    };

//    @Override
//    public List<CalendarResponseDto> getCalendarList(CalendarRequestDto calendarRequestDto) {
//        return calendarMapper.selectCalendarList(calendarRequestDto);
//    }


}
