package com.plands.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 한 줄 일기 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryUpdateRequestDto {

    /** 수정할 일기 내용 */
    private String content;

    /** 수정할 일기 작성 날짜 (YYYY-MM-DD) */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    /** 새로 업로드할 이미지 파일 */
    private MultipartFile image;
}
