package com.plands.backend.controller;

import com.plands.backend.auth.SecurityUtils;
import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import com.plands.backend.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 한 줄 일기(Diary) 관련 API 요청을 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final SecurityUtils securityUtils;

    /**
     * 새 한 줄 일기를 등록
     */
    @PostMapping
    public ResponseEntity<String> createDiary(@ModelAttribute DiaryCreateRequestDto requestDto) {
        log.debug("====== 한 줄 일기 등록 컨트롤러 진입 ======");

        Long memberId = securityUtils.getCurrentMemberId();

        diaryService.registerDiary(memberId, requestDto);

        return ResponseEntity.ok("일기가 성공적으로 등록되었습니다.");
    }

    /**
     * 특정 기간(startDate ~ endDate) 동안의 한 줄 일기 목록을 조회한다.
     */
    @GetMapping
    public ResponseEntity<List<DiaryResponseDto>> getDiaryList(@RequestParam("startDate") String startDate,
                                                               @RequestParam("endDate") String endDate) {
        log.debug("====== 한 줄 일기 목록 조회 컨트롤러 진입 ======");

        Long memberId = securityUtils.getCurrentMemberId();

        log.debug("🔍 DB에서 조회된 진짜 회원 번호(memberId) -> {}", memberId);
        log.debug("요청 파라미터 -> startDate: {}, endDate: {}", startDate, endDate);

        List<DiaryResponseDto> diaryList = diaryService.findDiaryList(memberId, startDate, endDate);

        return ResponseEntity.ok(diaryList);
    }

    /**
     * 기존 한 줄 일기를 수정
     */
    @PostMapping("/{diaryId}")
    public ResponseEntity<Void> updateDiary(@PathVariable Long diaryId,
                                            @ModelAttribute DiaryUpdateRequestDto requestDto) {
        log.debug("====== 한 줄 일기 수정 컨트롤러 진입 - diaryId: {} ======", diaryId);

        Long memberId = securityUtils.getCurrentMemberId();

        diaryService.modifyDiary(diaryId, memberId, requestDto);

        return ResponseEntity.ok().build();
    }

    /**
     * 특정 한 줄 일기 삭제
     */
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        log.debug("====== 한 줄 일기 삭제 컨트롤러 진입 - diaryId: {} ======", diaryId);

        Long memberId = securityUtils.getCurrentMemberId();

        diaryService.deleteDiary(diaryId, memberId);

        return ResponseEntity.ok().build();
    }
}
