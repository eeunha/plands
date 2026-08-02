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


@SpringBootTest // 스프링 부트 컨테이너를 띄워서 실제 환경과 똑같이 테스트합니다.
@Transactional  // 테스트가 끝나면 DB를 원래 상태로 롤백(되돌리기)시켜서 데이터가 더러워지지 않게 막아줍니다!
public class DiaryServiceTest {
    @Autowired
    private DiaryService diaryService; // 테스트할 서비스 주입

    @Test
    @DisplayName("사진 첨부 된 한 줄 일기 등록 및 목록 조회 통합 테스트")
    void registerAndFindDiaryTest() {
        // given: 1. 테스트에 사용할 회원 ID와 일기 등록 요청 DTO 준비
        Long memberId = 1L; // 주의: 실제 DB의 members 테이블에 존재하는 회원 ID여야 합니다!
        LocalDate today = LocalDate.now();

        // 💡 2. 테스트용 가짜 이미지 파일(MultipartFile) 생성
        MockMultipartFile fakeImage = new MockMultipartFile(
                "image",                          // 파라미터 이름 (DTO의 이미지 필드명)
                "test-image.jpg",                 // 원본 파일명
                "image/jpeg",                     // Content-Type
                "fake image binary content".getBytes() // 파일 내용 (바이트 배열)
        );

        DiaryCreateRequestDto requestDto = new DiaryCreateRequestDto();
        requestDto.setContent("테스트 코드로 작성하는 한 줄 일기");
        requestDto.setDiaryDate(today);
        requestDto.setImage(fakeImage);

        // when: 2. 서비스의 일기 등록 메서드 호출!
        diaryService.registerDiary(memberId, requestDto);

        // when: 3. 등록한 날짜가 포함된 기간(어제 ~ 내일)으로 목록 조회 메서드 호출!
        String startDate = today.minusDays(1).toString(); // 예: "YYYY-MM-DD"
        String endDate = today.plusDays(1).toString();
        List<DiaryResponseDto> diaryList = diaryService.findDiaryList(memberId, startDate, endDate);

        // then: 4. 검증 (조회된 목록이 비어있지 않고, 작성한 내용이 잘 들어있는지 확인)
        assertThat(diaryList).isNotEmpty();
        assertThat(diaryList.get(0).getContent()).isEqualTo("테스트 코드로 작성하는 한 줄 일기");
        // 필요하다면 이미지 경로가 null이 아닌지도 함께 검증 가능
        assertThat(diaryList.get(0).getImagePath()).isNotNull();
    }

    @Test
    @DisplayName("한 줄 일기 이미지 변경 테스트")
    void modifyDiaryImageTest() {
        // given: 1. 테스트에 사용할 회원 ID와 일기 등록 요청 DTO 준비
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        MockMultipartFile originalImage = new MockMultipartFile(
                "image", "old-image.jpg", "image/jpeg", "old image content".getBytes()
        );

        DiaryCreateRequestDto createDto = new DiaryCreateRequestDto();
        createDto.setContent("이미지 변경 테스트용 일기");
        createDto.setDiaryDate(today);
        createDto.setImage(originalImage);

        diaryService.registerDiary(memberId, createDto);

        // 방금 등록된 일기 ID와 기존 이미지 경로 확보
        List<DiaryResponseDto> initialList = diaryService.findDiaryList(memberId, today.toString(), today.toString());
        Long diaryId = initialList.get(0).getDiaryId();
        String oldImagePath = initialList.get(0).getImagePath();

        // given: 2. 완전히 새로운 이미지 준비
        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new-image.jpg", "image/jpeg", "new image content".getBytes()
        );

        DiaryUpdateRequestDto updateDto = new DiaryUpdateRequestDto();
        updateDto.setDiaryId(diaryId);
        updateDto.setMemberId(memberId);
        updateDto.setContent("이미지만 바꿀래요"); // 텍스트는 유지하거나 변경
        updateDto.setDiaryDate(today);
        updateDto.setImage(newImage); //새 이미지 주입

        // when: 3. 수정 서비스 호출
        diaryService.modifyDiary(updateDto);

        // then: 4. 수정 후 이미지 경로가 기존과 달라졌는지(새로 잘 교체되었는지) 검증
        List<DiaryResponseDto> updatedList = diaryService.findDiaryList(memberId, today.toString(), today.toString());
        String newImagePath = updatedList.get(0).getImagePath();

        assertThat(newImagePath).isNotNull();
        assertThat(newImagePath).isNotEqualTo(oldImagePath); // 기존 경로와 달라야 성공!
    }
}
