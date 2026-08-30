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

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final TodoService todoService;
    private final SecurityUtils securityUtil;

    // TODO: 도메인 책임 분리를 위해 향후 MemberPlantController / MemberPlantService로 이관 필요
    @GetMapping("/member-plant")
    public ResponseEntity<List<MemberPlantResponseDto>> getMemberPlants() {
        Long memberId = securityUtil.getCurrentMemberId();
        log.debug("회원 식물 목록 조회 - memberId: {}", memberId);

        List<MemberPlantResponseDto> list = todoService.findMemberPlantList(memberId);

        return ResponseEntity.ok(list);
    }
}
