package com.plands.backend.controller;

import com.plands.backend.auth.SecurityUtils;
import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import com.plands.backend.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final SecurityUtils securityUtils;

    // 새 한 줄 일기 등록 API
    @PostMapping
    public ResponseEntity<String> createDiary(@AuthenticationPrincipal UserDetails userDetails,
                                              @ModelAttribute DiaryCreateRequestDto requestDto) {

        log.info("====== 한 줄 일기 등록 컨트롤러 진입 ======");

        Long memberId = securityUtils.getCurrentMemberId();

        diaryService.registerDiary(memberId, requestDto);

        return ResponseEntity.ok("일기가 성공적으로 등록되었습니다.");
    }

    // 한 줄 일기 목록 조회 API
    @GetMapping
    public ResponseEntity<List<DiaryResponseDto>> getDiaryList(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {

        log.info("====== 한 줄 일기 목록 조회 컨트롤러 진입 ======");

        // 1. 시큐리티 세션에서 유저 고유 PK(memberId) 안전하게 추출
        Long memberId = securityUtils.getCurrentMemberId();

        log.debug("🔍 DB에서 조회된 진짜 회원 번호(memberId) -> {}", memberId);
        log.debug("요청 파라미터 -> startDate: {}, endDate: {}", startDate, endDate);

        // 2. 서비스 레이어를 통해 월별 일기 목록 조회
        List<DiaryResponseDto> diaryList = diaryService.findDiaryList(memberId, startDate, endDate);

        log.info("[일기 목록 조회 성공] memberId={}, 조회 건수: {}건", memberId, diaryList.size());

        return ResponseEntity.ok(diaryList);
    }

    @PostMapping("/{diaryId}")
    public ResponseEntity<Void> updateDiary(@PathVariable Long diaryId,
                                            @ModelAttribute DiaryUpdateRequestDto requestDto,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("====== 한 줄 일기 수정 컨트롤러 진입 ======");
        log.debug("프론트에서 넘어온 수정 대상 ID: {}", diaryId);

        Long memberId = securityUtils.getCurrentMemberId();

        diaryService.modifyDiary(diaryId, memberId, requestDto);

        return ResponseEntity.ok().build();
    }

    // 한 줄 일기 삭제 API
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("====== 한 줄 일기 삭제 컨트롤러 진입 ======");
        log.debug("프론트에서 넘어온 삭제 대상 ID: {}", diaryId);

        Long curUserId = securityUtils.getCurrentMemberId();

        diaryService.deleteDiary(diaryId, curUserId);

        return ResponseEntity.ok().build();
    }
}
