<script setup>
import { computed } from 'vue'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'

const props = defineProps({
  selectedDate: {
    type: Date,
    required: true,
  },
  diaryData: {
    type: Object,
    default: null, // 일기가 없으면 null
  },
})

// 백엔드 서버 주소
const BACKEND_URL = 'http://localhost:8081'

// 이미지 경로에 백엔드 주소를 붙여주는 computed 속성
const resolvedImageUrl = computed(() => {
  const path = props.diaryData?.imagePath
  if (!path) return ''

  // 만약 DB 경로가 이미 http로 시작한다면 그대로 쓰고, 아니라면 백엔드 주소 결합
  return path.startsWith('http') ? path : `${BACKEND_URL}${path}`
})

const formattedDate = computed(() => {
  return format(props.selectedDate, 'yyyy년 M월 d일 (EEE)', { locale: ko })
})
</script>

<template>
  <div class="diary-pane">
    <!-- 상단 선택된 날짜 타이틀 -->
    <div class="diary-date-title">
      <h3>{{ formattedDate }}</h3>
    </div>

    <!-- 일기가 존재하는 경우 -->
    <div v-if="props.diaryData" class="diary-content-box">
      <!-- 이미지 영역 (이미지가 있을 때만 렌더링) -->
      <div v-if="props.diaryData.imagePath" class="diary-image-wrapper">
        <img :src="resolvedImageUrl" alt="일기 이미지" />
      </div>

      <!-- 본문 내용 영역 -->
      <div class="diary-text">
        <p>{{ props.diaryData.content }}</p>
      </div>
    </div>

    <!-- 일기가 없는 빈 상태인 경우 -->
    <div v-else class="diary-empty-box">
      <div class="empty-icon">🌿</div>
      <p>이 날은 작성된 일기가 없어요.</p>
      <span class="empty-subtext">우측 상단 '등록' 버튼을 눌러 소중한 하루를 기록해 보세요!</span>
    </div>
  </div>
</template>

<style scoped>
.diary-pane {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 456px;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.diary-date-title {
  font-size: 18px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 20px;
  border-bottom: 2px solid #edf2f7;
}

/* 일기가 있을 때의 스타일 박스 */
.diary-content-box {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex-grow: 1;
}

.diary-image-wrapper {
  width: 100%;
  /* max-height를 없애고 박스 크기가 이미지에 맞춰 유연하게 조절되도록 변경 */
  border-radius: 8px;
  overflow: hidden;
  background-color: #f7fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px; /* 사진 주변에 여유 공간(패딩)을 줘서 더 깔끔해 보이게 함 */
}

.diary-image-wrapper img {
  max-width: 100%; /* 박스가 너무 커지지 않게 가로 폭 제한 */
  max-height: 350px; /* 세로가 아무리 긴 사진이어도 최대 350px까지만 커지도록 제한 */
  width: auto; /* 비율 유지 */
  height: auto; /* 비율 유지 */
  object-fit: contain;
  display: block;
}

.diary-text {
  font-size: 15px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap; /* 줄바꿈 문자(\n)가 실제 화면에서도 반영되도록! */
}

/* 일기가 없을 때의 빈 상태 스타일 */
.diary-empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-grow: 1;
  text-align: center;
  color: #a0aec0;
  gap: 10px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 5px;
}

.diary-empty-box p {
  font-size: 16px;
  font-weight: 600;
  color: #718096;
}

.empty-subtext {
  font-size: 13px;
  color: #a0aec0;
}
</style>
