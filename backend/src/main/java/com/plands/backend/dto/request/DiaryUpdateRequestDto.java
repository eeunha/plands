package com.plands.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryUpdateRequestDto {

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    private MultipartFile image; // 새로 업로드할 실제 이미지 파일
}
