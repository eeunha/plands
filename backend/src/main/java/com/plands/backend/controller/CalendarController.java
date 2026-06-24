package com.plands.backend.controller;

import com.plands.backend.dto.request.CalendarRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.service.CalendarService;
import com.plands.backend.service.CalendarServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public Map<String, Object> getCalendarList(CalendarRequestDto calendarRequestDto) {

        List<CalendarResponseDto> events = calendarService.getCalendarList(calendarRequestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("allEvents", events);

        return response;
    }
}
