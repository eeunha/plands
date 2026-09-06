package com.plands.backend.controller;

import com.plands.backend.auth.SecurityUtils;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.service.MemberPlantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/member-plant")
@RequiredArgsConstructor
public class MemberPlantController {

    private final MemberPlantService memberPlantService;
    private final SecurityUtils securityUtil;

    @GetMapping
    public ResponseEntity<List<MemberPlantResponseDto>> getMemberPlants() {
        Long memberId = securityUtil.getCurrentMemberId();
        log.debug("회원 식물 목록 조회 - memberId: {}", memberId);

        List<MemberPlantResponseDto> list = memberPlantService.findMemberPlantList(memberId);

        return ResponseEntity.ok(list);
    }
}
