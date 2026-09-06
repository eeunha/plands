package com.plands.backend.service;

import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.mapper.MemberPlantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPlantServiceImpl implements MemberPlantService {

    private final MemberPlantMapper memberPlantMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberPlantResponseDto> findMemberPlantList(Long memberId) {
        return memberPlantMapper.selectMemberPlants(memberId);
    }
}
