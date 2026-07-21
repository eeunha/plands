package com.plands.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiaryDto {
    private Long diaryId;       // 일기 고유 번호 (DB 생성용)
    private Long memberId;      // 작성자 회원 ID
    private String content;     // 일기 본문 내용
    private LocalDate diaryDate;// 작성 날짜
    private String imagePath;   // 서버에 저장된 이미지 파일 경로
}
