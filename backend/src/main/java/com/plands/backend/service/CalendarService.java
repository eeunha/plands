package com.plands.backend.service;

import com.plands.backend.dto.response.CalendarResponseDto;

import java.util.List;

public interface CalendarService {
    List<CalendarResponseDto> getCalendarList(Long memberId, String startDate, String endDate);
}
