package com.plands.backend.controller;

import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.service.MemberService;
import com.plands.backend.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final MemberService memberService;
    private final TodoService todoService;

    // UserDetails에서 memberId를 안전하게 꺼내오는 든든한 헬퍼 메서드!
    private Long getAuthenticatedMemberId(UserDetails userDetails) {

        // 1. 토큰의 Subject에서 로그인한 유저의 이메일(혹은 소셜 식별 아이디) 추출
        String email = userDetails.getUsername();

        log.debug("[인증] 유저 이메일로 memberId 조회 시작 -> {}", email);

        // 2. memberService를 통해 DB에서 해당 이메일을 가진 진짜 회원 정보(memberId) 찾아 진짜 유저 고유 ID(PK) 꺼내기
        return memberService.findByEmail(email)
                .orElseThrow(() -> {
                    // 예외가 터지기 직전에 경고 로그를 남겨두면 나중에 서버 관리자가 보기 편함!
                    log.warn("[인증 실패] 존재하지 않는 유저 이메일 접근 시도: {}", email);
                    return new IllegalArgumentException("🚨 에러: 토큰 정보에 해당하는 회원이 DB에 없습니다!");
                })
                .getMemberId();
    }

    @GetMapping("/member-plant")
    public ResponseEntity<List<MemberPlantResponseDto>> getMemberPlants(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("====== 회원 식물 목록 조회 컨트롤러 진입 ======");

        Long memberId = getAuthenticatedMemberId(userDetails);

        List<MemberPlantResponseDto> list = todoService.findMemberPlantList(memberId);

        return ResponseEntity.ok(list);
    }
}
