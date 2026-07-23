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

// 💡 부모 컴포넌트로 삭제 요청 이벤트를 던지기 위한 emit 선언
const emit = defineEmits(['delete-diary'])

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

// 💡 미래 날짜 여부 판단 computed (시간대 버그 방지 문자열 비교)
const isFutureDate = computed(() => {
  if (!props.selectedDate) return false

  // 오늘 날짜를 YYYY-MM-DD 문자열로 구하기
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  const todayStr = `${year}-${month}-${day}`

  // 선택된 날짜를 YYYY-MM-DD 문자열로 구하기
  const selectedYear = props.selectedDate.getFullYear()
  const selectedMonth = String(props.selectedDate.getMonth() + 1).padStart(2, '0')
  const selectedDay = String(props.selectedDate.getDate()).padStart(2, '0')
  const selectedStr = `${selectedYear}-${selectedMonth}-${selectedDay}`

  return selectedStr > todayStr
})

// 🌟 삭제 버튼을 클릭했을 때 작동하는 함수
const clickDeleteDiary = (diaryId) => {
  if (confirm('이 일기를 정말 삭제하시겠습니까? 복구 불가!')) {
    emit('delete-diary', diaryId)
  }
}
</script>

<template>
  <div class="diary-pane">
    <!-- 상단 선택된 날짜 타이틀 -->
    <div class="diary-date-title">
      <h3>{{ formattedDate }}</h3>

      <!-- 일기가 존재할 때만 삭제 버튼 노출 -->
      <button
        v-if="props.diaryData"
        class="btn-delete"
        @click="clickDeleteDiary(props.diaryData.diaryId)"
        title="일기 삭제"
      >
        🗑️
      </button>
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
      <!-- 1. 미래 날짜인 경우 -->
      <template v-if="isFutureDate">
        <div class="empty-icon">🚀</div>
        <p>아직 다가오지 않은 날짜입니다.</p>
        <span class="empty-subtext">해당 날짜가 되면 소중한 일기를 작성할 수 있어요!</span>
      </template>

      <!-- 2. 과거 또는 오늘인데 일기가 없는 경우 -->
      <template v-else>
        <div class="empty-icon">🌿</div>
        <p>이 날은 작성된 일기가 없어요.</p>
        <span class="empty-subtext">우측 상단 '등록' 버튼을 눌러 소중한 하루를 기록해 보세요!</span>
      </template>
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #edf2f7;
}

.diary-date-title h3 {
  margin: 0;
}

/* 🌟 할 일 컴포넌트와 동일한 깔끔한 삭제 버튼 스타일 */
.btn-delete {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  border-radius: 4px;
  transition:
    background-color 0.2s,
    transform 0.1s;
}

.btn-delete:hover {
  background-color: #fee2e2; /* 붉은 마우스 오버 효과 */
  transform: scale(1.1);
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
