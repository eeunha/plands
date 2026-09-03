package com.plands.backend.controller;

import com.plands.backend.auth.SecurityUtils;
import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import com.plands.backend.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<Void> createDiary(@Valid @ModelAttribute DiaryCreateRequestDto requestDto) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("한 줄 일기 등록 요청 - memberId: {}", memberId);

        diaryService.registerDiary(memberId, requestDto);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<DiaryResponseDto>> getDiaryList(@RequestParam("startDate") String startDate,
                                                               @RequestParam("endDate") String endDate) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("한 줄 일기 목록 조회 요청 - memberId: {}, range: {} ~ {}", memberId, startDate, endDate);

        List<DiaryResponseDto> diaryList = diaryService.findDiaryList(memberId, startDate, endDate);

        return ResponseEntity.ok(diaryList);
    }

    @PostMapping("/{diaryId}")
    public ResponseEntity<Void> updateDiary(@PathVariable Long diaryId,
                                            @Valid @ModelAttribute DiaryUpdateRequestDto requestDto) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("한 줄 일기 수정 요청 - diaryId: {}, memberId: {}", diaryId, memberId);
        log.debug("requestDto: {}", requestDto);

        diaryService.modifyDiary(diaryId, memberId, requestDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        Long memberId = securityUtils.getCurrentMemberId();
        log.debug("한 줄 일기 삭제 요청 - diaryId: {}, memberId: {}", diaryId, memberId);

        diaryService.deleteDiary(diaryId, memberId);

        return ResponseEntity.ok().build();
    }
}
