package com.plands.backend.service;

import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;

import java.util.List;

public interface DiaryService {
    void registerDiary(Long memberId, DiaryCreateRequestDto requestDto);

    List<DiaryResponseDto> findDiaryList(Long memberId, String startDate, String endDate);

    void deleteDiary(Long diaryId, Long curUserId);
}
