package com.plands.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
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
@ToString
public class DiaryUpdateRequestDto {

    /** 수정할 일기 내용 */
    @NotBlank(message = "일기 내용은 필수 입력 항목입니다.")
    @Size(max = 255, message = "일기 내용은 255자 이하로 입력해주세요.")
    private String content;

    /** 수정할 일기 작성 날짜 (YYYY-MM-DD) */
    @NotNull(message = "작성 날짜는 필수 입력 항목입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    /** 새로 업로드할 이미지 파일 (수정 시 미첨부 가능하므로 @NotNull 사용 안함) */
    private MultipartFile image;
}
