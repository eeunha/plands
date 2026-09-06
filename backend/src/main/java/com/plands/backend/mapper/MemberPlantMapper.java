package com.plands.backend.mapper;

import com.plands.backend.dto.response.MemberPlantResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 회원 식물(MemberPlant) 데이터베이스 매퍼 인터페이스
 */
@Mapper
public interface MemberPlantMapper {

    /**
     * 특정 회원이 등록한 식물 목록을 조회합니다.
     *
     * @param memberId 회원 고유 번호 (PK)
     * @return 회원의 식물 목록 응답 DTO 리스트
     */
    List<MemberPlantResponseDto> selectMemberPlants(Long memberId);
}
