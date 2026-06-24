package com.plands.backend.service;

import com.plands.backend.dto.request.CalendarRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;

import java.util.List;

public interface CalendarService {
    List<CalendarResponseDto> getCalendarList(CalendarRequestDto calendarRequestDto);
}
