<script setup>
import { ref, computed } from 'vue'
import MyCalendar from '@/components/my-calendar/MyCalendar.vue'
import DailySchedule from '@/components/my-calendar/DailySchedule.vue'
import MyDiary from '@/components/my-calendar/MyDiary.vue'
import TodoFormModal from '@/components/modal/TodoFormModal.vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'
import DiaryFormModal from '@/components/modal/DiaryFormModal.vue'

// 컴포저블 함수에서 상태(allEvents, allDiaries)와 API 호출 함수 가져오기
const { allEvents, allDiaries, fetchTodoList, fetchDiaryList, deleteTodo, deleteDiary } =
  useCalendarApi()

// 오른쪽 탭에 보여줄 기준 날짜 (기본값: 오늘)
const selectedDate = ref(new Date())
const isTodoRegisterModalOpen = ref(false) // 할 일 등록 모달 열림 상태 관리
const isTodoEditModalOpen = ref(false) // 할 일 수정 모달 열림 상태 추가
const selectedTodoData = ref(null) // 수정 팝업에 채워넣을 데이터 바구니

// 한 줄 일기 등록 모달 열림 상태 관리
const isDiaryRegisterModalOpen = ref(false)

// 한 줄 일기 수정 모달 관련 상태
const isDiaryEditModalOpen = ref(false)
const selectedDiaryData = ref(null)

// 달력 조회 시 썼던 날짜 범위를 기억해두기 위한 변수 (새로고침할 때 재사용!)
const currentPeriod = ref({ startDate: '', endDate: '' })

// 우측 상단 탭 상태 ('todo' 또는 'diary')
const activeTab = ref('todo')

// Date 객체를 '2026-06-25' 형태의 문자열로 안전하게 변환
const formatDateStr = (dateObj) => {
  const offset = dateObj.getTimezoneOffset() * 60000
  return new Date(dateObj.getTime() - offset).toISOString().split('T')[0]
}

// 선택된 날짜 하루치의 일정만 필터링
const filteredEvents = computed(() => {
  const selectedDateStr = formatDateStr(selectedDate.value)
  if (!allEvents.value) return []

  // 백엔드 DTO의 날짜 변수명이 start이므로 event.start로 매핑해서 비교!
  return allEvents.value.filter((event) => event.start === selectedDateStr)
})

// 선택한 날짜 하루치의 일기 데이터만 필터링
const selectedDiary = computed(() => {
  const selectedDateStr = formatDateStr(selectedDate.value)
  if (!allDiaries.value) return []

  console.log('받아온 전체 일기 리스트:', allDiaries.value)

  // 백엔드 DTO의 일기 날짜 변수명에 맞춰 비교 (보통 diaryDate 등 사용)
  return allDiaries.value.find((diary) => diary.diaryDate === selectedDateStr) || null
})

// 날짜를 선택했을 때
const handleDateSelected = (dateStr) => {
  selectedDate.value = new Date(dateStr)
}

// 달력 월 변경 또는 최초 로드 시 기간별 데이터 조회 (할 일 + 일기 동시 조회)
const handleEventsLoaded = async ({ startDate, endDate }) => {
  currentPeriod.value = { startDate, endDate }

  // 병렬로 할 일 목록과 일기 목록을 동시에 가져오기
  await Promise.all([fetchTodoList({ startDate, endDate }), fetchDiaryList({ startDate, endDate })])
}

const processSavedDate = (savedDateStr) => {
  if (!savedDateStr) return true // 날짜 정보가 없으면 그대로 진행

  const newDateObj = new Date(savedDateStr)

  // [체크] 등록된 날짜가 현재 보고 있는 달력의 월과 같은지 비교
  const isSameMonth =
    selectedDate.value.getFullYear() === newDateObj.getFullYear() &&
    selectedDate.value.getMonth() === newDateObj.getMonth()

  // 1. 일단 선택된 하루(레이저 포인터)를 등록된 새 날짜로 변경!
  selectedDate.value = newDateObj

  // 2. 같은 달인지 여부를 반환 (true면 데이터 새로고침 진행, false면 멈춤)
  return isSameMonth
}

// 할 일 등록 및 수정 완료 시 공통 저장 핸들러
const handleTodoSaved = async (savedDateStr) => {
  // 수정 모달이 열려있었다면 닫아주기
  if (isTodoEditModalOpen.value) isTodoEditModalOpen.value = false

  // 다른 달이면 헬퍼가 false를 주므로 바로 리턴!
  if (!processSavedDate(savedDateStr)) return

  await refreshAllLists()
}

// 할 일 수정 모달 오픈 핸들러
const handleTodoEdit = (eventObj) => {
  selectedTodoData.value = eventObj // 클릭한 할 일 정보를 바구니에 저장
  isTodoEditModalOpen.value = true // 수정 모달 열기!
}

// 할 일 삭제 핸들러
const handleTodoDelete = async (todoId) => {
  const isSuccess = await deleteTodo(todoId)

  if (isSuccess) {
    alert('할 일이 성공적으로 삭제되었습니다. 🌿')
    await refreshAllLists()
  } else {
    alert('할 일 삭제에 실패했습니다.')
  }
}

// 한 줄 일기 등록 완료 시 실행될 저장 핸들러
const handleDiarySaved = async (savedDateStr) => {
  isDiaryRegisterModalOpen.value = false

  if (!processSavedDate(savedDateStr)) return

  await refreshAllLists()
}

// 한 줄 일기 수정 모달 오픈 핸들러 (MyDiary 컴포넌트에서 이벤트로 올려받을 함수)
const handleDiaryEdit = (diaryObj) => {
  selectedDiaryData.value = diaryObj // 수정할 기존 일기 데이터 담기
  isDiaryEditModalOpen.value = true // 한 줄 일기 수정 모달 열기
}

// 한 줄 일기 수정 완료 시 실행될 저장 핸들러
const handleDiaryUpdated = async (savedDateStr) => {
  isDiaryEditModalOpen.value = false

  if (!processSavedDate(savedDateStr)) return

  await refreshAllLists()
}

// 한 줄 일기 삭제 핸들러
const handleDiaryDelete = async (diaryId) => {
  const result = await deleteDiary(diaryId)

  if (result.success) {
    alert('일기가 성공적으로 삭제되었습니다. 🌿')
    await refreshAllLists()
  } else {
    alert(result.message)
  }
}

// 💡 오늘 날짜 YYYY-MM-DD 구하기
const getTodayStr = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 💡 미래 날짜 판별 함수
const isFutureDate = (dateStr) => {
  if (!dateStr) return false
  return dateStr > getTodayStr()
}

// 탭에 따라 등록 버튼을 눌렀을 때 실행될 통합 핸들러
const handleRegisterClick = () => {
  if (activeTab.value === 'todo') {
    isTodoRegisterModalOpen.value = true // 할 일 등록 모달 오픈
  } else if (activeTab.value === 'diary') {
    // 현재 선택된 날짜에 이미 작성된 일기가 있는지 검사
    const targetDateStr = formatDateStr(selectedDate.value)
    const isExist = allDiaries.value?.some((diary) => diary.diaryDate === targetDateStr)

    // 1. 미래 날짜 먼저 차단
    if (isFutureDate(targetDateStr)) {
      alert('미래 날짜에는 일기를 등록할 수 없습니다. 📅')
      return
    }

    // 2. 이미 작성된 일기 체크
    if (isExist) {
      alert('선택하신 날짜에는 이미 작성된 일기가 있습니다! 🌿\n기존 일기를 확인해 주세요.')
      return // 👈 일기 등록 모달을 열지 않고 함수 종료!
    }

    isDiaryRegisterModalOpen.value = true // 일기가 없는 날짜일 때만 모달 오픈
  }
}

// 전체 목록(할 일 + 한 줄 일기) 새로고침 공통 로직
const refreshAllLists = async () => {
  const { startDate, endDate } = currentPeriod.value

  if (startDate && endDate) {
    await Promise.all([
      fetchTodoList({ startDate, endDate }),
      fetchDiaryList({ startDate, endDate }),
    ])
  } else {
    console.warn('CalendarView: 현재 기억된 달력 조회 기간이 없어 새로고침을 스킵합니다.')
  }
}

// 💡 모달창에 '2026-06-25' 형태로 날짜를 넘겨주기 위한 computed
const selectedDateString = computed(() => formatDateStr(selectedDate.value))
</script>

<template>
  <div class="calendar-view-container">
    <div class="content-wrapper">
      <!-- 왼쪽: 캘린더 영역 -->
      <div class="calendar-section">
        <MyCalendar
          :events="allEvents"
          :diaries="allDiaries"
          :selected-date="selectedDate"
          @date-selected="handleDateSelected"
          @events-loaded="handleEventsLoaded"
        />
      </div>

      <!-- 오른쪽: 할 일 / 일기 전환 영역 -->
      <div class="schedule-section">
        <!-- 우측 상단 탭 헤더 -->
        <div class="tab-header">
          <div class="tab-buttons">
            <button :class="{ active: activeTab === 'todo' }" @click="activeTab = 'todo'">
              할 일
            </button>
            <button :class="{ active: activeTab === 'diary' }" @click="activeTab = 'diary'">
              일기
            </button>
          </div>

          <!-- 등록 버튼 -->
          <button class="btn-register" @click="handleRegisterClick">등록</button>
        </div>

        <!-- 탭에 따른 컨텐츠 전환 영역 -->
        <div class="tab-content">
          <!-- '할 일' 탭 내용 -->
          <DailySchedule
            v-if="activeTab === 'todo'"
            :selected-date="selectedDate"
            :events="filteredEvents"
            @open-register="isTodoRegisterModalOpen = true"
            @delete-todo="handleTodoDelete"
            @edit-todo="handleTodoEdit"
          />

          <!-- '일기' 탭 내용 -->
          <MyDiary
            v-else
            :selected-date="selectedDate"
            :diary-data="selectedDiary"
            @edit-diary="handleDiaryEdit"
            @delete-diary="handleDiaryDelete"
          />
        </div>
      </div>
    </div>

    <!-- 할 일 등록 모달 -->
    <TodoFormModal
      v-if="isTodoRegisterModalOpen"
      :initial-date="selectedDateString"
      @close="isTodoRegisterModalOpen = false"
      @saved="handleTodoSaved"
    />

    <!-- 할 일 수정 모달 -->
    <TodoFormModal
      v-if="isTodoEditModalOpen"
      :is-edit-mode="true"
      :todo-data="selectedTodoData"
      :initial-date="selectedTodoData ? selectedTodoData.start : selectedDateString"
      @close="isTodoEditModalOpen = false"
      @saved="handleTodoSaved"
    />

    <!-- 한 줄 일기 등록 모달 -->
    <DiaryFormModal
      v-if="isDiaryRegisterModalOpen"
      :initial-date="selectedDateString"
      @close="isDiaryRegisterModalOpen = false"
      @saved="handleDiarySaved"
    />

    <!-- 한 줄 일기 수정 모달 -->
    <DiaryFormModal
      v-if="isDiaryEditModalOpen"
      :is-edit-mode="true"
      :diary-data="selectedDiaryData"
      :initial-date="selectedDateString"
      @close="isDiaryEditModalOpen = false"
      @saved="handleDiaryUpdated"
    />
  </div>
</template>

<style scoped>
.calendar-view-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.content-wrapper {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.calendar-section {
  flex: 2;
  min-width: 600px;
  flex-grow: 1;
}

.schedule-section {
  flex: 1;
  min-width: 300px;
  flex-grow: 1;
}

.tab-header {
  display: flex;
  justify-content: space-between; /* 좌우 끝으로 정렬 */
  align-items: center;
  padding-bottom: 10px;
}

.tab-buttons {
  display: flex;
  gap: 15px;

  padding-bottom: 10px;
  flex-grow: 1; /* 탭 영역이 남은 공간을 쓰게 해서 버튼과 확실히 분리 */
  margin-right: 20px;
  padding-top: 10px;
}

.tab-header .tab-buttons button {
  background: none;
  border: none;
  font-size: 18px;
  font-weight: bold;
  color: #a0aec0;
  cursor: pointer;
  padding: 0 0 3px 0;
  position: relative;
  transition: color 0.2s ease;
}

.tab-header .tab-buttons button.active {
  color: #2d3748;
}

/* 활성화된 탭 아래에만 예쁜 초록색 포인트 밑줄 주기 (선택사항) */
.tab-header .tab-buttons button.active::after {
  content: '';
  position: absolute;
  //bottom: -12px; /* 밑줄 위치 맞추기 */
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #10b981;
  border-radius: 2px;

  bottom: -4px;
}

.btn-register {
  background-color: #10b981;
  color: white;
  border: none;
  padding: 8px 22px; /* 위아래 패딩과 좌우 패딩을 늘려서 버튼 키우기 */
  font-size: 15px; /* 글씨 크기도 살짝 키우기 */
  border-radius: 24px; /* 더 매끄러운 타원형으로 */
  cursor: pointer;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2); /* 은은한 입체감 추가 */
  transition:
    background-color 0.2s,
    transform 0.1s;
  white-space: nowrap; /* 글자 줄바꿈 방지 */
}

.btn-register:hover {
  background-color: #059669;
}

.tab-content {
  flex-grow: 1;
}
</style>
