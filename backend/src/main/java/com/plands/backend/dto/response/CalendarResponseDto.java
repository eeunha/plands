package com.plands.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarResponseDto {
    private Long calendarId;
    private String title;
    private String date;
    private String time;
    private String description;
}
