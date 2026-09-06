package com.plands.backend.service;

import com.plands.backend.dto.response.MemberPlantResponseDto;

import java.util.List;

public interface MemberPlantService {

    /**
     * 특정 회원이 보유한 식물 목록을 조회합니다.
     *
     * @param memberId 조회할 회원의 고유 번호 (PK)
     * @return 회원의 식물 목록 응답 DTO 리스트
     */
    List<MemberPlantResponseDto> findMemberPlantList(Long memberId);
}
