package com.plands.backend.service;

import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
public class DiaryServiceTest {
    @Autowired
    private DiaryService diaryService;

    @Test
    @DisplayName("사진 첨부 된 한 줄 일기 등록 및 목록 조회 통합 테스트")
    void registerAndFindDiaryTest() {
        // given - 테스트 데이터 및 가짜 이미지 파일 준비
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        MockMultipartFile fakeImage = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "fake image binary content".getBytes()
        );

        DiaryCreateRequestDto requestDto = new DiaryCreateRequestDto();
        requestDto.setContent("테스트 코드로 작성하는 한 줄 일기");
        requestDto.setDiaryDate(today);
        requestDto.setImage(fakeImage);

        // when - 일기 등록 및 기간 조회 실행
        diaryService.registerDiary(memberId, requestDto);

        String startDate = today.minusDays(1).toString(); // 예: "YYYY-MM-DD"
        String endDate = today.plusDays(1).toString();
        List<DiaryResponseDto> diaryList = diaryService.findDiaryList(memberId, startDate, endDate);

        // then - 등록된 데이터 검증
        assertThat(diaryList).isNotEmpty();
        assertThat(diaryList)
                .extracting(DiaryResponseDto::getContent)
                .contains("테스트 코드로 작성하는 한 줄 일기");
    }

    @Test
    @DisplayName("한 줄 일기 이미지 변경 테스트")
    void modifyDiaryImageTest() {
        // given - 초기 일기 등록 및 수정 데이터 준비
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        MockMultipartFile originalImage = new MockMultipartFile(
                "image",
                "old-image.jpg",
                "image/jpeg",
                "old image content".getBytes()
        );

        DiaryCreateRequestDto createDto = new DiaryCreateRequestDto();
        createDto.setContent("이미지 변경 테스트용 일기");
        createDto.setDiaryDate(today);
        createDto.setImage(originalImage);

        diaryService.registerDiary(memberId, createDto);

        List<DiaryResponseDto> initialList = diaryService.findDiaryList(memberId, today.toString(), today.toString());
        Long diaryId = initialList.get(0).getDiaryId();
        String oldImagePath = initialList.get(0).getImagePath();

        MockMultipartFile newImage = new MockMultipartFile(
                "image",
                "new-image.jpg",
                "image/jpeg",
                "new image content".getBytes()
        );

        DiaryUpdateRequestDto updateDto = new DiaryUpdateRequestDto();
        updateDto.setContent("이미지만 바꿀래요");
        updateDto.setDiaryDate(today);
        updateDto.setImage(newImage);

        // when - 일기 수정 서비스 호출
        diaryService.modifyDiary(diaryId, memberId, updateDto);

        // then - 이미지 경로 변경 여부 검증
        List<DiaryResponseDto> updatedList = diaryService.findDiaryList(memberId, today.toString(), today.toString());
        String newImagePath = updatedList.get(0).getImagePath();

        assertThat(newImagePath).isNotNull();
        assertThat(newImagePath).isNotEqualTo(oldImagePath);
    }
}
