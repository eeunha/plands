package com.plands.backend.service;

import com.plands.backend.dto.DiaryDto;
import com.plands.backend.dto.DiaryTargetDto;
import com.plands.backend.dto.request.DiaryCreateRequestDto;
import com.plands.backend.dto.request.DiaryUpdateRequestDto;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public void registerDiary(Long memberId, DiaryCreateRequestDto requestDto) {
        validateCreateRequest(memberId, requestDto);

        String imagePath = processImageUpload(requestDto.getImage());

        DiaryDto diaryDto = DiaryDto.builder()
                .memberId(memberId)
                .content(requestDto.getContent())
                .diaryDate(requestDto.getDiaryDate())
                .imagePath(imagePath)
                .build();

        diaryMapper.insertDiary(diaryDto);
        log.info("한 줄 일기 생성 완료: memberId={}, date={}", memberId, requestDto.getDiaryDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryResponseDto> findDiaryList(Long memberId, String startDate, String endDate) {
        log.debug("한 줄 일기 목록 조회: memberId={}, range={} ~ {}", memberId, startDate, endDate);
        return diaryMapper.selectDiaryList(memberId, startDate, endDate);
    }

    @Override
    @Transactional
    public void modifyDiary(Long diaryId, Long memberId, DiaryUpdateRequestDto diaryDto) {
        DiaryTargetDto target = getValidDiaryTarget(diaryId, memberId);

        validateDateForUpdate(memberId, diaryDto.getDiaryDate(), target.getDiaryDate());

        String finalImagePath = processImageReplacement(target.getImagePath(), diaryDto.getImage());

        int updatedRows = diaryMapper.updateDiary(diaryId, memberId, diaryDto, finalImagePath);
        if (updatedRows != 1) {
            throw new IllegalStateException("한 줄 일기 수정 처리에 실패했습니다.");
        }

        log.info("한 줄 일기 수정 완료: diaryId={}", diaryId);
    }

    @Override
    @Transactional
    public void deleteDiary(Long diaryId, Long curUserId) {
        DiaryTargetDto target = getValidDiaryTarget(diaryId, curUserId);

        int affectedRows = diaryMapper.deleteDiary(diaryId);
        if (affectedRows != 1) {
            throw new IllegalStateException("한 줄 일기 삭제 처리에 실패했습니다.");
        }

        if (hasImagePath(target.getImagePath())) {
            deleteLocalFile(target.getImagePath());
        }

        log.info("한 줄 일기 삭제 완료: diaryId={}", diaryId);
    }

    // =========================================================================
    // Helper Methods (검증 및 세부 처리 로직 분리)
    // =========================================================================

    private void validateCreateRequest(Long memberId, DiaryCreateRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("요청 데이터가 누락되었습니다.");
        }

        if (diaryMapper.existsByMemberIdAndDiaryDate(memberId, requestDto.getDiaryDate())) {
            log.warn("일기 생성 실패 - 중복 작성 시도: memberId={}, date={}", memberId, requestDto.getDiaryDate());
            throw new IllegalArgumentException("이미 해당 날짜에 작성된 일기가 존재합니다.");
        }

        if (requestDto.getDiaryDate().isAfter(LocalDate.now())) {
            log.warn("일기 생성 실패 - 미래 날짜 입력 시도: memberId={}, date={}", memberId, requestDto.getDiaryDate());
            throw new IllegalArgumentException("미래 날짜로 일기를 작성할 수 없습니다.");
        }
    }

    private DiaryTargetDto getValidDiaryTarget(Long diaryId, Long memberId) {
        DiaryTargetDto target = diaryMapper.selectTargetById(diaryId);

        if (target == null) {
            log.warn("작업 실패 - 존재하지 않는 일기 ID: {}", diaryId);
            throw new IllegalArgumentException("존재하지 않거나 이미 삭제된 한 줄 일기입니다. ID: " + diaryId);
        }

        if (!target.getMemberId().equals(memberId)) {
            log.warn("작업 실패 - 접근 권한 없음: diaryId={}, memberId={}", diaryId, memberId);
            throw new SecurityException("해당 한 줄 일기에 대한 접근 권한이 없습니다.");
        }

        return target;
    }

    private void validateDateForUpdate(Long memberId, LocalDate newDate, LocalDate existingDate) {
        if (newDate == null) {
            return;
        }

        if (newDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("미래 날짜로 일기를 수정할 수 없습니다.");
        }

        if (!newDate.equals(existingDate) && diaryMapper.existsByMemberIdAndDiaryDate(memberId, newDate)) {
            throw new IllegalArgumentException("이미 해당 날짜에 작성된 일기가 존재합니다.");
        }
    }

    private String processImageUpload(MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            return saveFileToLocal(image);
        }
        return null;
    }

    private String processImageReplacement(String currentImagePath, MultipartFile newImage) {
        if (newImage == null || newImage.isEmpty()) {
            return currentImagePath;
        }

        String newImagePath = saveFileToLocal(newImage);

        if (hasImagePath(currentImagePath)) {
            deleteLocalFile(currentImagePath);
        }

        return newImagePath;
    }

    private boolean hasImagePath(String imagePath) {
        return imagePath != null && !imagePath.isBlank();
    }

    /**
     * DB 트랜잭션 롤백 방지를 위해 로컬 파일 삭제 실패 예외는 상쇄 후 로그 기록
     */
    private void deleteLocalFile(String imagePath) {
        if (!hasImagePath(imagePath)) {
            return;
        }

        try {
            String filenameOrRelativePath = cleanDbPath(imagePath);
            Path targetFilePath = Paths.get(uploadDir).resolve("diary").resolve(filenameOrRelativePath).normalize();

            boolean isDeleted = Files.deleteIfExists(targetFilePath);
            if (!isDeleted) {
                log.warn("삭제할 파일이 로컬 디렉터리에 없습니다: {}", targetFilePath);
            }
        } catch (IOException e) {
            log.error("로컬 파일 삭제 실패 (DB 경로: {})", imagePath, e);
        }
    }

    private String cleanDbPath(String dbPath) {
        if (dbPath != null && dbPath.startsWith("/uploads/diary/")) {
            return dbPath.substring("/uploads/diary/".length());
        }
        return dbPath;
    }

    private String saveFileToLocal(MultipartFile image) {
        try {
            File diaryDirectory = new File(uploadDir, "diary");
            if (!diaryDirectory.exists()) {
                diaryDirectory.mkdirs();
            }

            String originalFilename = image.getOriginalFilename();
            String savedFilename = UUID.randomUUID() + "_" + originalFilename;

            File destinationFile = new File(diaryDirectory, savedFilename);
            image.transferTo(destinationFile);

            return "/uploads/diary/" + savedFilename;
        } catch (IOException e) {
            log.error("이미지 파일 저장 에러", e);
            throw new RuntimeException("이미지 파일 저장에 실패했습니다.", e);
        }
    }
}