<script setup>
import '@/assets/styles/modal.css'
import { ref, reactive } from 'vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'

const props = defineProps({
  initialDate: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['saved', 'close'])

const { createDiary } = useCalendarApi()

// 일기 폼 데이터 상태
const diaryForm = reactive({
  date: props.initialDate,
  content: '',
  imageFile: null,
})

// 이미지 미리보기 URL 상태
const imagePreview = ref(null)

// 파일 선택 시 처리 및 미리보기 생성
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    diaryForm.imageFile = file
    imagePreview.value = URL.createObjectURL(file)
  }
}

// 일기 저장 핸들러
const saveDiary = async () => {
  if (!diaryForm.date) {
    return alert('날짜를 선택해주세요')
  }
  if (!diaryForm.imageFile) {
    return alert('사진은 반드시 첨부해야 합니다.📸')
  }
  if (!diaryForm.content.trim()) {
    return alert('한 줄 일기 내용을 입력해주세요.')
  }

  const diaryPayload = {
    content: diaryForm.content,
    diaryDate: diaryForm.date,
    image: diaryForm.imageFile,
  }

  const isSuccess = await createDiary(diaryPayload)

  if (isSuccess) {
    alert('한 줄 일기가 성공적으로 등록되었습니다. 🌿')
    emit('saved', diaryForm.date)
    emit('close')
  } else {
    alert('한 줄 일기 등록에 실패했습니다. 다시 시도해주세요.')
  }
}
</script>

<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <h3>새 한 줄 일기 등록 📖</h3>

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
        <button class="modal-button primary" @click="saveDiary">등록</button>
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
