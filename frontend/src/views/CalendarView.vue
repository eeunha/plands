<script setup>
import { ref, computed } from 'vue'
import MyCalendar from '@/components/my-calendar/MyCalendar.vue'
import DailySchedule from '@/components/my-calendar/DailySchedule.vue'
import TodoRegisterModal from '@/components/modal/TodoRegisterModal.vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'

// 컴포저블 함수에서 상태(allEvents)와 API 호출 함수(fetchCalendarList) 가져오기
const { allEvents, fetchCalendarList, deleteTodo } = useCalendarApi()

// 오른쪽 탭에 보여줄 기준 날짜 (기본값: 오늘)
const selectedDate = ref(new Date())
const isRegisterModalOpen = ref(false) // 모달 열림 상태 관리
const isEditModalOpen = ref(false) // 수정 모달 열림 상태 추가
const selectedTodoData = ref(null) // 수정 팝업에 채워넣을 데이터 바구니

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

// 날짜를 선택했을 때
const handleDateSelected = (dateStr) => {
  selectedDate.value = new Date(dateStr)
}

// 달력 월 변경 또는 최초 로드 시 기간별 데이터 조회
const handleEventsLoaded = async ({ startDate, endDate }) => {
  currentPeriod.value = { startDate, endDate }
  await fetchCalendarList({ startDate, endDate })
}

// 등록 및 수정 완료 시 공통 저장 핸들러
const handleTodoSaved = async (savedDateStr) => {
  // 수정 모달이 열려있었다면 닫아주기
  if (isEditModalOpen.value) isEditModalOpen.value = false

  if (savedDateStr) {
    const newDateObj = new Date(savedDateStr)

    // [체크] 등록된 날짜가 현재 보고 있는 달력의 월과 같은지 비교
    const isSameMonth =
      selectedDate.value.getFullYear() === newDateObj.getFullYear() &&
      selectedDate.value.getMonth() === newDateObj.getMonth()

    // 1. 일단 선택된 하루(레이저 포인터)를 등록된 새 날짜로 변경!
    selectedDate.value = newDateObj

    // 2. 만약 다른 달(7월)로 등록한 거라면?
    // 부모가 selectedDate를 바꾸는 순간 자식(MyCalendar)의 watch가 작동해
    // 알아서 달력을 7월로 넘기고 백엔드를 찌를 테니, 부모는 여기서 아무것도 안 해도 됨!
    if (!isSameMonth) {
      return
    }
  }

  // 3. 만약 같은 달(6월) 안에서 등록한 거라면 화면 이동이 없으니 기존처럼 데이터만 새로고침!
  await refreshCalendarList()
}

// 수정 모달 오픈 핸들러
const handleTodoEdit = (eventObj) => {
  selectedTodoData.value = eventObj // 클릭한 할 일 정보를 바구니에 저장
  isEditModalOpen.value = true // 수정 모달 열기!
}

// 삭제 핸들러
const handleTodoDelete = async (todoId) => {
  const isSuccess = await deleteTodo(todoId)

  if (isSuccess) {
    alert('할 일이 성공적으로 삭제되었습니다. 🌿')
    await refreshCalendarList()
  } else {
    alert('할 일 삭제에 실패했습니다.')
  }
}

// 새로고침 공통 로직
const refreshCalendarList = async () => {
  const { startDate, endDate } = currentPeriod.value

  if (startDate && endDate) {
    await fetchCalendarList({ startDate, endDate })
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
          :selected-date="selectedDate"
          @date-selected="handleDateSelected"
          @events-loaded="handleEventsLoaded"
        />
      </div>

      <!-- 오른쪽: 할 일 / 일기 전환 영역 -->
      <div class="schedule-section">
        <!-- 우측 상단 탭 헤더 -->
        <div class="tab-header">
          <button :class="{ active: activeTab === 'todo' }" @click="activeTab = 'todo'">
            할 일
          </button>
          <button :class="{ active: activeTab === 'diary' }" @click="activeTab = 'diary'">
            일기
          </button>
        </div>

        <!-- 탭에 따른 컨텐츠 전환 영역 -->
        <div class="tab-content">
          <!-- '할 일' 탭 내용 -->
          <DailySchedule
            v-if="activeTab === 'todo'"
            :selected-date="selectedDate"
            :events="filteredEvents"
            @open-register="isRegisterModalOpen = true"
            @delete-todo="handleTodoDelete"
            @edit-todo="handleTodoEdit"
          />

          <!-- '일기' 탭 내용 (나중에 Diary 컴포넌트로 분리 예정) -->
          <div v-else class="diary-pane">
            <p class="diary-date-title">{{ selectedDateString }}의 일기</p>
            <!-- 여기에 일기 이미지와 내용이 들어갈 예정입니다. -->
          </div>
        </div>
      </div>
    </div>

    <!-- 등록 모달 -->
    <TodoRegisterModal
      v-if="isRegisterModalOpen"
      :initial-date="selectedDateString"
      @close="isRegisterModalOpen = false"
      @saved="handleTodoSaved"
    />

    <!-- 수정 모달 -->
    <TodoRegisterModal
      v-if="isEditModalOpen"
      :is-edit-mode="true"
      :todo-data="selectedTodoData"
      :initial-date="selectedTodoData ? selectedTodoData.start : selectedDateString"
      @close="isEditModalOpen = false"
      @saved="handleTodoSaved"
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
  gap: 15px;
  margin-bottom: 20px;
  border-bottom: 1px solid #edf2f7;
  padding-bottom: 10px;
}

.tab-header button {
  background: none;
  border: none;
  font-size: 18px;
  font-weight: bold;
  color: #a0aec0;
  cursor: pointer;
  padding: 0;
  transition: color 0.2s ease;
}

.tab-header button.active {
  color: #2d3748;
}

.tab-content {
  flex-grow: 1;
}

.diary-pane {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #718096;
}

.diary-date-title {
  font-weight: bold;
  margin-bottom: 15px;
  color: #2d3748;
}
</style>
