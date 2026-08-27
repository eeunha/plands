package com.plands.backend.controller;

import com.plands.backend.auth.SecurityUtils;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 달력 관련 API 요청을 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final TodoService todoService;
    private final SecurityUtils securityUtil;

    // TODO: 식물 도메인 분리 필요 (MemberPlantController 및 MemberPlantService 신규 생성 후 이관)
    /**
     * 현재 로그인한 회원의 식물 목록을 조회
     */
    @GetMapping("/member-plant")
    public ResponseEntity<List<MemberPlantResponseDto>> getMemberPlants() {
        log.debug("====== 회원 식물 목록 조회 컨트롤러 진입 ======");

        Long memberId = securityUtil.getCurrentMemberId();

        List<MemberPlantResponseDto> list = todoService.findMemberPlantList(memberId);

        return ResponseEntity.ok(list);
    }
}
