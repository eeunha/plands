<script setup>
import '@/assets/styles/modal.css'
import { ref, reactive, onMounted } from 'vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'

const props = defineProps({
  initialDate: {
    type: String,
    default: '',
  },
  isEditMode: {
    type: Boolean,
    default: false,
  },
  diaryData: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['saved', 'close'])

const { createDiary, updateDiary } = useCalendarApi()

// 알림 메시지 상단 상수 분리 (TodoFormModal과 일치하는 패턴)
const MODAL_TEXTS = {
  edit: {
    success: '한 줄 일기가 성공적으로 수정되었습니다. 🌱',
    fail: '한 줄 일기 수정에 실패했습니다.',
  },
  create: {
    success: '한 줄 일기가 성공적으로 등록되었습니다. 🌱',
    fail: '한 줄 일기 등록에 실패했습니다.',
  },
}

// 일기 폼 데이터 상태
const diaryForm = reactive({
  date: props.initialDate,
  content: '',
  imageFile: null,
})

// 이미지 미리보기 URL 상태 (기존 서버 이미지 또는 새로 업로드한 파일 미리보기)
const imagePreview = ref(null)

// 백엔드 서버 주소 (서버에 이미 저장된 기존 이미지 경로 조합용)
const BACKEND_URL = 'http://localhost:8081'

// 파일 선택 시 처리 및 미리보기 생성
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    diaryForm.imageFile = file
    imagePreview.value = URL.createObjectURL(file)
  }
}

// 한국 시간(로컬) 기준 오늘 날짜를 'YYYY-MM-DD' 문자열로 구하는 함수
const getTodayStr = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}` // 예: '2026-07-23'
}

// 미래 날짜 판별 함수 (문자열 대소 비교로 시차 버그 100% 방지)
const isFutureDate = (dateStr) => {
  if (!dateStr) return false

  const todayStr = getTodayStr()

  // 예: '2026-07-24' > '2026-07-23' 👉 true (미래 날짜)
  return dateStr > todayStr
}

// 일기 저장 함수 (등록/수정 분기 처리)
const saveDiary = async () => {
  // 1. 유효성 검사
  if (!diaryForm.date) {
    return alert('날짜를 선택해주세요')
  }

  if (isFutureDate(diaryForm.date)) {
    return alert('미래 날짜에는 일기를 작성할 수 없습니다! 📅')
  }

  // 💡 등록 모드일 때만 이미지 필수 체크 (수정 모드에서는 기존 사진 유지 가능)
  if (!props.isEditMode && !diaryForm.imageFile) {
    return alert('사진은 반드시 첨부해야 합니다.📸')
  }

  if (!diaryForm.content.trim()) {
    return alert('한 줄 일기 내용을 입력해주세요.')
  }

  const diaryPayload = {
    content: diaryForm.content,
    diaryDate: diaryForm.date,
    image: diaryForm.imageFile, // 새 파일이 없으면 null이 들어가고 백엔드에서 기존 이미지 유지 처리
  }

  let result = { success: false, message: '' }
  const modeKey = props.isEditMode ? 'edit' : 'create'

  // ✏️ 모드에 따라 API 분기 태우기
  if (props.isEditMode) {
    const diaryId = props.diaryData?.diaryId
    result = await updateDiary(diaryId, diaryPayload)
  } else {
    result = await createDiary(diaryPayload)
  }

  if (result.success) {
    alert(MODAL_TEXTS[modeKey].success)
    emit('saved', diaryForm.date)
    emit('close')
  } else {
    alert(result.message || MODAL_TEXTS[modeKey].fail)
  }
}

// 💡 수정 모드일 때 기존 데이터 바인딩
onMounted(() => {
  if (props.isEditMode && props.diaryData) {
    const data = props.diaryData

    diaryForm.date = data.diaryDate || props.initialDate
    diaryForm.content = data.content || ''

    // 기존에 등록된 이미지가 있다면 미리보기 창에 띄워주기
    if (data.imagePath) {
      imagePreview.value = data.imagePath.startsWith('http')
        ? data.imagePath
        : `${BACKEND_URL}${data.imagePath}`
    }
  }
})
</script>

<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <h3>{{ isEditMode ? '한 줄 일기 수정하기' : '새 한 줄 일기 등록 📖' }}</h3>

      <!-- 날짜 선택 섹션 -->
      <div class="form-group">
        <label>작성 날짜</label>
        <input type="date" v-model="diaryForm.date" class="modal-input" />
      </div>

      <!-- 사진 업로드 섹션 -->
      <div class="form-group">
        <label>사진 첨부</label>
        <input
          type="file"
          accept="image/*"
          @change="handleFileChange"
          class="modal-input file-input"
        />

        <!-- 사진 미리보기 영역 -->
        <div v-if="imagePreview" class="image-preview-container">
          <img :src="imagePreview" alt="미리보기" class="preview-img" />
        </div>
      </div>

      <!-- 한 줄 일기 내용 입력 섹션 -->
      <div class="form-group">
        <label>한 줄 일기</label>
        <div class="textarea-wrapper">
          <textarea
            v-model="diaryForm.content"
            maxLength="255"
            rows="3"
            placeholder="오늘 식물들과 함께 한 소중한 순간을 남겨주세요. (255자 이내)"
            class="modal-textarea"
          ></textarea>
          <div class="char-counter">
            <span>{{ diaryForm.content.length }}</span> / 255자
          </div>
        </div>
      </div>

      <!-- 모달 버튼 -->
      <div class="modal-buttons">
        <button class="modal-button primary" @click="saveDiary">{{ isEditMode ? '수정완료' : '등록'}}</button>
        <button class="modal-button" @click="$emit('close')">취소</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-group {
  margin-bottom: 20px;
  text-align: left;
}
.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}
.modal-input,
.modal-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 14px;
}
.modal-textarea {
  resize: none;
}
.file-input {
  padding: 8px;
  background: #fff;
}
.image-preview-container {
  margin-top: 10px;
  text-align: center;
  border: 1px dashed #cbd5e0;
  border-radius: 4px;
  padding: 10px;
  background: #f7fafc;
}
.preview-img {
  max-width: 100%;
  max-height: 150px;
  border-radius: 4px;
  object-fit: cover;
}
.textarea-wrapper {
  position: relative;
}
.char-counter {
  width: 100%;
  text-align: right;
  font-size: 12px;
  color: #888;
  margin-top: 6px;
}
</style>
