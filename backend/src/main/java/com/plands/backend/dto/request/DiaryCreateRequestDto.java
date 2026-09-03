package com.plands.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @NotBlank(message = "일기 내용은 필수 입력 항목입니다.")
    @Size(max = 255, message = "일기 내용은 255자 이하로 입력해주세요.")
    private String content;

    /** 일기 작성 날짜 (YYYY-MM-DD) */
    @NotNull(message = "작성 날짜는 필수 입력 항목입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    /** 업로드할 이미지 파일 */
    @NotNull(message = "이미지 파일은 필수 입력 항목입니다.")
    private MultipartFile image;
}
