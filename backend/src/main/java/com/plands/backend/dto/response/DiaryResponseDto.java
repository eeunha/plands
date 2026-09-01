package com.plands.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 한 줄 일기 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResponseDto {

    /** 한 줄 일기 고유 번호 (PK) */
    private Long diaryId;

    /** 일기 작성자 회원 고유 번호 (PK) */
    private Long memberId;

    /** 업로드된 이미지 파일 경로 (URL) */
    private String imagePath;

    /** 한 줄 일기 내용 */
    private String content;

    /** 일기 작성 날짜 (YYYY-MM-DD) */
    private String diaryDate;
}
