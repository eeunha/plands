package com.plands.backend.service;

import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;

import java.util.List;

public interface DiaryService {

    /**
     * 새로운 한 줄 일기를 등록합니다.
     *
     * @param memberId   작성자 회원 고유 번호 (PK)
     * @param requestDto 일기 생성 요청 데이터
     * @throws IllegalArgumentException 요청 데이터가 누락되었거나, 이미 작성된 날짜/미래 날짜인 경우
     */
    void registerDiary(Long memberId, DiaryCreateRequestDto requestDto);

    /**
     * 지정된 기간 내의 한 줄 일기 목록을 조회합니다.
     *
     * @param memberId  조회할 회원 고유 번호 (PK)
     * @param startDate 조회 시작일 (YYYY-MM-DD)
     * @param endDate   조회 종료일 (YYYY-MM-DD)
     * @return 해당 기간 내 작성된 한 줄 일기 응답 DTO 리스트
     */
    List<DiaryResponseDto> findDiaryList(Long memberId, String startDate, String endDate);

    /**
     * 특정 한 줄 일기를 수정합니다.
     *
     * @param diaryId  수정할 일기 고유 번호 (PK)
     * @param memberId 수정 요청자 회원 고유 번호 (PK)
     * @param diaryDto 수정할 일기 정보 및 새로운 이미지 파일
     * @throws IllegalArgumentException 대상 일기가 존재하지 않거나, 중복/미래 날짜인 경우
     * @throws SecurityException        작성자 본인이 아닌 경우
     * @throws IllegalStateException    DB 업데이트 처리에 실패한 경우
     */
    void modifyDiary(Long diaryId, Long memberId, DiaryUpdateRequestDto diaryDto);

    /**
     * 작성한 한 줄 일기를 삭제하고, 첨부된 이미지 파일을 서버 로컬에서 물리 삭제합니다.
     *
     * @param diaryId  삭제할 일기 고유 번호 (PK)
     * @param memberId 삭제 요청자 회원 고유 번호 (PK)
     * @throws IllegalArgumentException 일기가 존재하지 않거나 이미 삭제된 경우
     * @throws SecurityException        본인이 작성한 일기가 아닌 경우
     * @throws IllegalStateException    DB 삭제 처리에 실패한 경우
     */
    void deleteDiary(Long diaryId, Long memberId);
}
