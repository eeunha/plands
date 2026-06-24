package com.plands.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarRequestDto {
    private int year;
    private int month;
}
