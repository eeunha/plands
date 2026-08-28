package com.plands.backend.service;

import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final TodoMapper todoMapper;

    @Override
    public List<CalendarResponseDto> findCalendarList(Long memberId, String startDate, String endDate) {
        log.debug("캘린더 목록 조회 DB 호출 - memberId: {}, range: {} ~ {}", memberId, startDate, endDate);

        return todoMapper.selectCalendarList(memberId, startDate, endDate);
    }
}
