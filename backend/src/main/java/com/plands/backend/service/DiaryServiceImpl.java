package com.plands.backend.service;

import com.plands.backend.dto.DiaryDeleteTargetDto;
import com.plands.backend.dto.DiaryDto;
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

    // application.yml에 설정한 파일 저장 경로 주입 (C:/plands/uploads/)
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 새로운 일기를 생성합니다.
     * 1. 요청 날짜에 작성된 일기가 이미 있는지 검증합니다.
     * 2. 요청 데이터 유효성 및 미래 날짜 입력 여부를 검증합니다.
     * 3. 첨부 이미지가 존재할 경우 로컬 디렉토리에 저장합니다.
     * 4. 일기 정보 및 이미지 접근 경로를 DB에 등록합니다.
     *
     * @param memberId   작성자 회원 ID
     * @param requestDto 일기 생성 요청 데이터 (내용, 날짜, 이미지 파일 등)
     * @throws IllegalArgumentException 요청 데이터가 null이거나 미래 날짜로 일기를 작성하려는 경우
     */
    @Override
    @Transactional
    public void registerDiary(Long memberId, DiaryCreateRequestDto requestDto) {
        log.info("====== 한 줄 일기 등록 서비스 레이어 DB 호출 ======");

        // 0. 요청 데이터가 null인지 체크
        if (requestDto == null) {
            throw new IllegalArgumentException("요청 데이터가 누락되었습니다.");
        }

        // 1. 이미 해당 날짜에 작성된 일기가 있는지 중복 체크
        if (diaryMapper.existsByMemberIdAndDiaryDate(memberId, requestDto.getDiaryDate())) {
            log.warn("[일기 생성 실패] 이미 작성된 일기가 존재함: memberId={}, date={}", memberId, requestDto.getDiaryDate());
            throw new IllegalArgumentException("이미 해당 날짜에 작성된 일기가 존재합니다.");
        }

        // 2. 미래 날짜 검증
        if (requestDto.getDiaryDate().isAfter(LocalDate.now())) {
            log.warn("[일기 생성 실패] 미래 날짜 입력 시도: memberId={}, date={}", memberId, requestDto.getDiaryDate());
            throw new IllegalArgumentException("미래 날짜로 일기를 작성할 수 없습니다.");
        }

        // 3. 이미지 파일 로컬 저장
        String imagePath = null;
        MultipartFile image = requestDto.getImage();

        if (image != null && !image.isEmpty()) {
            imagePath = saveFileToLocal(image);
        }

        // 4. DB 저장
        DiaryDto diaryDto = DiaryDto.builder()
                .memberId(memberId)
                .content(requestDto.getContent())
                .diaryDate(requestDto.getDiaryDate())
                .imagePath(imagePath)
                .build();

        diaryMapper.insertDiary(diaryDto);

        log.info("[일기 생성 성공] memberId={}, diaryDate={}, imagePath={}", memberId, requestDto.getDiaryDate(), imagePath);
    }

    /**
     * 지정된 기간 내의 한 줄 일기 목록을 조회합니다.
     *
     * @param memberId  조회할 회원 ID
     * @param startDate 조회 시작일 (YYYY-MM-DD)
     * @param endDate   조회 종료일 (YYYY-MM-DD)
     * @return 해당 기간 내 작성된 한 줄 일기 응답 DTO 리스트
     */
    @Override
    public List<DiaryResponseDto> findDiaryList(Long memberId, String startDate, String endDate) {
        log.info("====== 한 줄 일기 조회 서비스 레이어 DB 호출 ======");
        log.debug("조회 유저 ID: {}, 조회 기간(한 달): {} ~ {}", memberId, startDate, endDate);

        return diaryMapper.selectDiaryList(memberId, startDate, endDate);
    }

    /**
     * 특정 한 줄 일기를 수정합니다.
     *
     * <p>수정 요청 시 전달된 DTO를 바탕으로 소유권을 검증하며,
     * 새로운 이미지 파일이 포함된 경우 기존 파일을 대체하고 로컬 스토리지를 정리합니다.
     * 새로운 이미지 파일이 없는 경우 기존 이미지 경로는 유지됩니다.</p>
     *
     * @param diaryDto   수정할 일기 정보와 새로운 이미지 파일이 포함된 요청 DTO (diaryId, memberId 필수)
     * @throws IllegalArgumentException 필수 값이 누락되었거나 일기가 존재하지 않는 경우
     * @throws SecurityException        요청한 사용자가 해당 일기의 작성자가 아닌 경우
     * @throws IllegalStateException    DB 업데이트 처리가 정상적으로 완료되지 않은 경우
     */
    @Override
    @Transactional
    public void modifyDiary(DiaryUpdateRequestDto diaryDto) {
        log.info("====== 한 줄 일기 수정 서비스 레이어 진입 (diaryId: {}) ======", diaryDto.getDiaryId());

        // 0. 필수 값 누락 방어
        if (diaryDto.getDiaryId() == null) {
            throw new IllegalArgumentException("수정할 일기 ID가 누락되었습니다.");
        }
        if (diaryDto.getMemberId() == null) {
            throw new IllegalArgumentException("회원 정보가 누락되었습니다.");
        }

        Long diaryId = diaryDto.getDiaryId();
        Long curMemberId = diaryDto.getMemberId();

        // ----------------------------------------------------
        // 1. 수정하려는 일기가 존재하는가?
        // ----------------------------------------------------
        DiaryDeleteTargetDto target = diaryMapper.selectDeleteTargetById(diaryId);

        if (target == null) {
            throw new IllegalArgumentException("존재하지 않거나 이미 삭제된 한 줄 일기입니다. ID: " + diaryId);
        }

        // ----------------------------------------------------
        // 2. 수정하려는 사람과 일기 작성자가 동일한가? (권한 검증)
        // ----------------------------------------------------
        if (!target.getMemberId().equals(curMemberId)) {
            throw new SecurityException("해당 한 줄 일기를 수정할 권한이 없습니다.");
        }

        // ----------------------------------------------------
        // 3. 바뀐 대상에 따른 검증 및 처리
        // ----------------------------------------------------

        // 3.1. 날짜가 변경된 경우 -> 이미 작성된 일자인지 중복 검사 + 미래 날짜 검증
        LocalDate newDiaryDate = diaryDto.getDiaryDate();
        LocalDate existingDate = target.getDiaryDate();

        if (newDiaryDate != null) {
            // 1) 미래 날짜 검증
            if (newDiaryDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("미래 날짜로 일기를 수정할 수 없습니다.");
            }

            // 2) 날짜 중복 검사
            if (!newDiaryDate.equals(existingDate)) {
                boolean exists = diaryMapper.existsByMemberIdAndDiaryDate(curMemberId, newDiaryDate);
                if (exists) {
                    throw new IllegalArgumentException("이미 해당 날짜에 작성된 일기가 존재합니다.");
                }
            }
        }

        // 3.2. 사진이 새로 업로드 된 경우 -> 기존 사진 로컬 제거 후 새 사진 저장 및 경로 세팅
        MultipartFile newImage = diaryDto.getImage();
        if (newImage != null && !newImage.isEmpty()) {
            // 새 파일 로컬 저장
            String imagePath = saveFileToLocal(newImage);
            diaryDto.setImagePath(imagePath);

            // 서버에 남아있던 기존(옛날) 사진 물리 삭제
            if (target.getImagePath() != null && !target.getImagePath().isBlank()) {
                deleteLocalFile(target.getImagePath());
            }
        }

        // 3.3. 글(내용) 및 기타 항목 수정 포함하여 DB 업데이트 수행
        int updatedRows = diaryMapper.updateDiary(diaryDto);

        if (updatedRows != 1) {
            throw new IllegalStateException("한 줄 일기 수정 처리에 실패했습니다.");
        }

        log.info("====== 한 줄 일기 수정 완료 (diaryId: {}) ======", diaryId);
    }

    /**
     * 작성한 한 줄 일기를 삭제하고, 첨부된 이미지 파일을 서버 로컬에서 물리 삭제합니다.
     *
     * @param diaryId   삭제할 일기 ID
     * @param curUserId 현재 로그인한 회원 ID (권한 검증용)
     * @throws IllegalArgumentException 일기가 존재하지 않거나 이미 삭제된 경우
     * @throws SecurityException        본인이 작성한 일기가 아닌 경우
     * @throws IllegalStateException    DB 삭제 처리에 실패한 경우
     */
    @Override
    @Transactional
    public void deleteDiary(Long diaryId, Long curUserId) {
        log.info("====== 한 줄 일기 삭제 서비스 레이어 진입 (diaryId: {}) ======", diaryId);

        // ----------------------------------------------------
        // Step 1: 삭제 대상 데이터 사전 조회
        // ----------------------------------------------------
        DiaryDeleteTargetDto target = diaryMapper.selectDeleteTargetById(diaryId);

        // 1-1. 일기 존재 여부 확인
        if (target == null) {
            throw new IllegalArgumentException("존재하지 않거나 이미 삭제된 일기입니다. ID: " + diaryId);
        }

        // 1-2. 본인 글인지 권한 검증 (Security UserDetails에서 온 ID 비교)
        if (!target.getMemberId().equals(curUserId)) {
            throw new SecurityException("해당 일기를 삭제할 권한이 없습니다.");
        }

        // ----------------------------------------------------
        // Step 2: DB 하드 삭제 수행
        // ----------------------------------------------------
        int affectedRows = diaryMapper.deleteDiary(diaryId);

        // [중요] MyBatis 반환값 검증! 1개 행이 안 지워졌다면 예외 처리
        if (affectedRows != 1) {
            throw new IllegalStateException("일기 삭제 처리에 실패했습니다.");
        }

        // ----------------------------------------------------
        // Step 3: DB 삭제 성공 후 로컬 파일(사진) 물리 삭제
        // ----------------------------------------------------
        if (target.getImagePath() != null && !target.getImagePath().isBlank()) {
            deleteLocalFile(target.getImagePath());
        }

        log.info("====== 한 줄 일기 삭제 완료 (diaryId: {}) ======", diaryId);
    }

    /**
     * DB에 저장된 이미지 경로를 기반으로 로컬 디렉터리의 실제 파일을 물리 삭제합니다.
     *
     * 파일 삭제 중 오류가 발생해도 DB 트랜잭션 롤백을 방지하기 위해 예외를 상위로 던지지 않고 로그만 기록합니다.
     *
     * @param imagePath DB에 저장된 웹 접근 이미지 경로 (예: /uploads/diary/uuid_filename.jpg)
     */
    private void deleteLocalFile(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        try {
            // 1. DB 경로에서 매핑 접두사('/uploads/diary/' 또는 '/uploads/diary') 제거
            String filenameOrRelativePath = cleanDbPath(imagePath);

            // 2. C:/plands/uploads/diary/ + 남은 경로 결합
            Path targetFilePath = Paths.get(uploadDir).resolve("diary").resolve(filenameOrRelativePath).normalize();

            log.debug("실제 로컬 삭제 경로: {}", targetFilePath);

            // 3. 파일 삭제 실행
            boolean isDeleted = Files.deleteIfExists(targetFilePath);

            if (isDeleted) {
                log.info("로컬 일기 사진 삭제 성공: {}", targetFilePath);
            } else {
                log.warn("삭제할 파일이 로컬 디렉터리에 없습니다: {}", targetFilePath);
            }

        } catch (IOException e) {
            log.error("로컬 파일 삭제 실패 (DB 경로: {})", imagePath, e);
        }
    }

    /**
     * DB에 저장된 웹 접근 경로에서 WebConfig 정적 매핑용 URL prefix('/uploads/diary/')를 제거합니다.
     *
     * @param dbPath DB에 저장된 이미지 경로
     * @return URL prefix가 제거된 순수 파일명 또는 상대 경로
     */
    private String cleanDbPath(String dbPath) {
        if (dbPath != null && dbPath.startsWith("/uploads/diary/")) {
            return dbPath.substring("/uploads/diary/".length());
        }
        return dbPath; // 규칙과 다를 경우 원본 반환
    }

    /**
     * 업로드된 이미지 파일의 덮어쓰기를 방지하기 위해 UUID를 부여하여 로컬 디렉터리에 저장하고,
     * DB에 기록할 웹 접근 URL 경로를 반환합니다.
     *
     * @param image 업로드된 이미지 파일
     * @return DB에 저장될 웹 접근 경로 (예: /uploads/diary/uuid_파일명)
     * @throws RuntimeException 이미지 저장 중 I/O 에러가 발생한 경우
     */
    private String saveFileToLocal(MultipartFile image) {
        try {
            // 1. 💡 윈도우 절대경로 뒤에 "diary" 폴더를 자바 코드에서 안전하게 붙여줌
            // 예: C:/plands/uploads/ + "diary/" -> C:/plands/uploads/diary/
            File diaryDirectory = new File(uploadDir, "diary");
            if (!diaryDirectory.exists()) {
                diaryDirectory.mkdirs(); // 폴더가 없으면 하위 폴더까지 전부 생성
            }

            // 2. 원본 파일 이름 추출 및 UUID 결합 (파일명 덮어쓰기 방지)
            String originalFilename = image.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedFilename = uuid + "_" + originalFilename; // . 대신 _를 쓰면 구분하기 더 편해!

            // 3. 최종 물리 저장 위치 (C:/plands/uploads/diary/uuid_파일명)
            File destinationFile = new File(diaryDirectory, savedFilename);
            image.transferTo(destinationFile);

            log.debug("[파일 저장 완료] 원본 이름: {}, 저장된 경로: {}", originalFilename, destinationFile.getAbsolutePath());

            // 4. 💡 DB에 저장될 웹 접근 경로 (/uploads/diary/uuid_파일명)
            return "/uploads/diary/" + savedFilename;
        } catch (IOException e) {
            log.error("[파일 저장 에러] 이미지 파일 처리 중 오류 발생", e);
            throw new RuntimeException("🚨 이미지 파일 저장에 실패했습니다.", e);
        }
    }
}
