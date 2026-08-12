package com.plands.backend.mapper;

import com.plands.backend.dto.DiaryDto;
import com.plands.backend.dto.DiaryTargetDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DiaryMapper {

    /**
     * 특정 회원의 해당 날짜에 이미 작성된 일기가 존재하는지 확인합니다.
     *
     * @param memberId  회원 ID
     * @param diaryDate 작성 날짜
     * @return 일기 존재 여부 (true: 존재함, false: 없음)
     */
    boolean existsByMemberIdAndDiaryDate(@Param("memberId") Long memberId, @Param("diaryDate") LocalDate diaryDate);

    /**
     * 새로운 일기를 데이터베이스에 저장합니다.
     * @param diaryDto 저장할 일기 데이터 (memberId, content, diaryDate, imagePath 등)
     * @return 성공 시 영향받은 행(Row)의 개수
     */
    int insertDiary(DiaryDto diaryDto);

    /**
     * 지정된 기간 내의 한 줄 일기 목록을 조회합니다.
     *
     * @param memberId  조회할 회원 ID
     * @param startDate 조회 시작일 (YYYY-MM-DD)
     * @param endDate   조회 종료일 (YYYY-MM-DD)
     * @return 해당 기간 내 작성된 한 줄 일기 응답 DTO 리스트
     */
    List<DiaryResponseDto> selectDiaryList(@Param("memberId") Long memberId,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    /**
     * 한 줄 일기 삭제 및 로컬 파일 삭제 처리를 위해 삭제 대상 일기의 정보를 조회합니다.
     *
     * @param diaryId 삭제할 일기 ID
     * @return 삭제 대상 정보 (작성자 memberId, 저장된 imagePath 등)
     */
    DiaryTargetDto selectTargetById(@Param("diaryId") Long diaryId);

    /**
     * 한 줄 일기 정보를 수정합니다. (일부 항목 선택적 수정 가능)
     *
     * @param diaryDto 수정할 한 줄 일기 정보가 담긴 DTO (diaryId, memberId 및 변경할 필드)
     * @return 성공 시 영향받은 행(Row)의 개수
     */
    int updateDiary(@Param("diaryId") Long diaryId, @Param("memberId") Long memberId, @Param("dto") DiaryUpdateRequestDto diaryDto, @Param("finalImagePath") String finalImagePath);

    /**
     * 한 줄 일기 데이터를 데이터베이스에서 완전 삭제(Hard Delete)합니다.
     *
     * @param diaryId 삭제할 일기 ID
     * @return 성공 시 영향받은 행(Row)의 개수
     */
    int deleteDiary(@Param("diaryId") Long diaryId);
}
