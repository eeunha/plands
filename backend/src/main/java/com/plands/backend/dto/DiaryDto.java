package com.plands.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 한 줄 일기 데이터 전달 객체 (Internal DTO)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiaryDto {

    /** 일기 고유 번호 (PK) */
    private Long diaryId;

    /** 작성자 회원 고유 번호 (PK) */
    private Long memberId;

    /** 일기 본문 내용 */
    private String content;

    /** 작성 날짜 (YYYY-MM-DD) */
    private LocalDate diaryDate;

    /** 서버에 저장된 이미지 파일 경로 */
    private String imagePath;
}
