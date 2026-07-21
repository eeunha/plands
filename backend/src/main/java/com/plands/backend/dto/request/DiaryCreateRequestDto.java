package com.plands.backend.dto.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiaryCreateRequestDto {
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    private MultipartFile image; // 업로드된 이미지 파일
}
