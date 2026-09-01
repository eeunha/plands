package com.plands.backend.dto.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 한 줄 일기 생성 요청 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiaryCreateRequestDto {

    /** 일기 내용 */
    private String content;

    /** 일기 작성 날짜 (YYYY-MM-DD) */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    /** 업로드할 이미지 파일 */
    private MultipartFile image;
}
