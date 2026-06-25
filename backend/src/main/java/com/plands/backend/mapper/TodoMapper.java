package com.plands.backend.mapper;

import com.plands.backend.dto.response.CalendarResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoMapper {
    // 한 달 치 데이터를 긁어오기 위해 startDate와 endDate를 기간으로 받음
    List<CalendarResponseDto> selectCalendarList(
            @Param("memberId") Long memberId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
