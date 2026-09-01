package com.plands.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 한 줄 일기 수정 및 삭제 권한 검증용 DTO
 */
@Getter
@Setter
public class DiaryTargetDto {

    /** 일기 고유 번호 (PK) */
    private Long diaryId;

    /** 작성자 회원 고유 번호 (작성자 검증용 PK) */
    private Long memberId;

    /** 서버에 저장된 이미지 파일 경로 */
    private String imagePath;

    /** 한 줄 일기 작성 날짜 (YYYY-MM-DD) */
    private LocalDate diaryDate;
}
