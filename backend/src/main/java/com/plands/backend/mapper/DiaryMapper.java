package com.plands.backend.mapper;

import com.plands.backend.dto.DiaryDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiaryMapper {

    /**
     * 새로운 일기를 데이터베이스에 저장합니다.
     * @param diaryDto 저장할 일기 데이터 (memberId, content, diaryDate, imagePath 등)
     * @return 성공 시 영향받은 행(Row)의 개수
     */
    int insertDiary(DiaryDto diaryDto);
}
