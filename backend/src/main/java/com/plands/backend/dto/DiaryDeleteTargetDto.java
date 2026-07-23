package com.plands.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryDeleteTargetDto {
    private Long diaryId;
    private Long memberId;     // 작성자 검증용
    private String imagePath;  // 로컬 파일 경로 (예: "/uploads/diary/abcd-123.jpg")
}
