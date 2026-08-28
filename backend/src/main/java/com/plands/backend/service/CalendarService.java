package com.plands.backend.service;

import com.plands.backend.dto.response.CalendarResponseDto;

import java.util.List;

/**
 * 캘린더 관련 비즈니스 로직을 제공하는 서비스 인터페이스
 */
public interface CalendarService {
    /**
     * 특정 회원의 지정된 기간(startDate ~ endDate) 동안의 캘린더 할 일 목록을 조회
     *
     * @param memberId  조회할 회원의 식별자 ID
     * @param startDate 조회 시작일 (YYYY-MM-DD)
     * @param endDate   조회 종료일 (YYYY-MM-DD)
     * @return 캘린더 응답 DTO 리스트
     */
    List<CalendarResponseDto> findCalendarList(Long memberId, String startDate, String endDate);
}
