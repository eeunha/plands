package com.plands.backend.service;

import com.plands.backend.dto.DiaryDto;
import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;

import java.util.List;

public interface DiaryService {
    void registerDiary(Long memberId, DiaryCreateRequestDto requestDto);

    List<DiaryResponseDto> findDiaryList(Long memberId, String startDate, String endDate);

    void modifyDiary(DiaryUpdateRequestDto diaryDto);

    void deleteDiary(Long diaryId, Long curUserId);
}
