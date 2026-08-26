package com.plands.backend.auth;

import com.plands.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final MemberService memberService;

    /**
     * SecurityContext에서 현재 인증된 사용자의 memberId를 안전하게 가져온다.
     */
    public Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[인증 실패] SecurityContext에 인증 정보가 없습니다.");
            throw new IllegalArgumentException("🚨 에러: 인증 정보가 존재하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            log.warn("[인증 실패] 유효하지 않은 Principal 객체 타입입니다.");
            throw new IllegalArgumentException("🚨 에러: 유효하지 않은 인증 정보입니다.");
        }

        String email = userDetails.getUsername();
        log.debug("[인증] 유저 이메일로 memberId 조회 시작 -> {}", email);

        return memberService.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[인증 실패] DB에 존재하지 않는 유저 이메일 접근 시도: {}", email);
                    return new IllegalArgumentException("🚨 에러: 토큰 정보에 해당하는 회원이 DB에 없습니다!");
                })
                .getMemberId();
    }
}
