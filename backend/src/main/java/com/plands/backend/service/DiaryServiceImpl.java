package com.plands.backend.service;

import com.plands.backend.dto.DiaryDto;
import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.response.DiaryResponseDto;
import com.plands.backend.mapper.DiaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;

    // application.yml에 설정한 파일 저장 경로 주입 (./uploads)
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 새로운 일기를 생성합니다.
     * 1. 미래 날짜 작성 여부 검증
     * 2. 이미지가 존재할 경우 로컬 디렉토리에 UUID 파일명으로 저장
     * 3. DB에 일기 정보(본문, 날짜, 이미지 경로) 저장
     */
    @Override
    @Transactional
    public void createDiary(Long memberId, DiaryCreateRequestDto requestDto) {
        log.info("====== 한 줄 일기 등록 서비스 레이어 DB 호출 ======");

        // 0. 요청 데이터가 null인지 체크
        if (requestDto == null) {
            throw new IllegalArgumentException("요청 데이터가 누락되었습니다.");
        }

        // 1. 미래 날짜 검증
        if (requestDto.getDiaryDate().isAfter(LocalDate.now())) {
            log.warn("[일기 생성 실패] 미래 날짜 입력 시도: memberId={}, date={}", memberId, requestDto.getDiaryDate());
            throw new IllegalArgumentException("미래 날짜로 일기를 작성할 수 없습니다.");
        }

        // 2. 이미지 파일 로컬 저장
        String imagePath = null;
        MultipartFile image = requestDto.getImage();

        if (image != null && !image.isEmpty()) {
            imagePath = saveFileToLocal(image);
        }

        // 3. DB 저장
        DiaryDto diaryDto = DiaryDto.builder()
                .memberId(memberId)
                .content(requestDto.getContent())
                .diaryDate(requestDto.getDiaryDate())
                .imagePath(imagePath)
                .build();

        diaryMapper.insertDiary(diaryDto);

        log.info("[일기 생성 성공] memberId={}, diaryDate={}, imagePath={}", memberId, requestDto.getDiaryDate(), imagePath);
    }

    @Override
    public List<DiaryResponseDto> getDiaryList(Long memberId, String startDate, String endDate) {
        log.info("====== 한 줄 일기 조회 서비스 레이어 DB 호출 ======");
        log.debug("조회 유저 ID: {}, 조회 기간(한 달): {} ~ {}", memberId, startDate, endDate);

        return diaryMapper.selectDiaryList(memberId, startDate, endDate);
    }

    /**
     * 업로드된 이미지를 로컬 디렉토리에 안전하게 저장하고, DB에 기록할 경로를 반환합니다.
     */
    private String saveFileToLocal(MultipartFile image) {
        try {
            // 업로드 디렉토리 객체 생성
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 폴더가 없으면 하위 폴더까지 전부 생성
            }

            // 원본 파일 이름 추출 및 UUID 결합 (파일명 덮어쓰기 방지)
            String originalFilename = image.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedFilename = uuid + "." + originalFilename;

            // 최종 저장 경로 설정 및 파일 전송
            File destinationFile = new File(uploadDir, savedFilename);
            image.transferTo(destinationFile);

            log.debug("[파일 저장 완료] 원본 이름: {}, 저장된 경로: {}", originalFilename, destinationFile.getAbsolutePath());

            // DB에 저장할 상대 경로 혹은 절대 경로 문자열 반환 (예: /uploads/uuid_filename.jpg)
            return "/uploads/" + savedFilename;
        } catch (IOException e) {
            log.error("[파일 저장 에러] 이미지 파일 처리 중 오류 발생", e);
            throw new RuntimeException("🚨 이미지 파일 저장에 실패했습니다.", e);
        }
    }

}
