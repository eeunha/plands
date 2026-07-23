package com.plands.backend.mapper;

import com.plands.backend.dto.DiaryDto;
import com.plands.backend.dto.DiaryDeleteTargetDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiaryMapper {

    /**
     * 새로운 일기를 데이터베이스에 저장합니다.
     * @param diaryDto 저장할 일기 데이터 (memberId, content, diaryDate, imagePath 등)
     * @return 성공 시 영향받은 행(Row)의 개수
     */
    int insertDiary(DiaryDto diaryDto);

    // 한 줄 일기 목록 조회 (한달)
    List<DiaryResponseDto> selectDiaryList(@Param("memberId") Long memberId,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    // 한 줄 일기 삭제 전 정보 조회
    DiaryDeleteTargetDto findDeleteTargetById(@Param("diaryId") Long diaryId);

    // 한 줄 일기 완전 삭제
    int deleteDiary(@Param("diaryId") Long diaryId);
}
