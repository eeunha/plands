package com.plands.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResponseDto {
    private Long diary_id;     // 한 줄 일기 고유 번호
    private Long member_id;    // 한 줄 일기 작성자 고유 번호
    private String image_path; // 이미지 경로
    private String content;    // 한 줄 일기 내용
    private String diary_date; // 한 줄 일기 날짜
}
