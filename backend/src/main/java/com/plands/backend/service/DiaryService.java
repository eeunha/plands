package com.plands.backend.service;

import com.plands.backend.dto.request.DiaryCreateRequestDto;

public interface DiaryService {
    void createDiary(Long memberId, DiaryCreateRequestDto requestDto);
}
