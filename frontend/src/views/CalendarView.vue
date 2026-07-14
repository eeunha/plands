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

// Date 객체를 '2026-06-25' 형태의 문자열로 안전하게 바꾸는 헬퍼 함수
const formatDateStr = (dateObj) => {
  const offset = dateObj.getTimezoneOffset() * 60000
  return new Date(dateObj.getTime() - offset).toISOString().split('T')[0]
}

// 백엔드에서 받은 한 달치(allEvents) 중, 선택된 날짜 하루치만 칼같이 필터링
const filteredEvents = computed(() => {
  const selectedDateStr = formatDateStr(selectedDate.value)
  if (!allEvents.value) return []

  // 백엔드 DTO의 날짜 변수명이 start이므로 event.start로 매핑해서 비교!
  return allEvents.value.filter((event) => event.start === selectedDateStr)
})

// 달력에서 날짜를 클릭했을 때 실행될 함수
const handleDateSelected = (dateStr) => {
  selectedDate.value = new Date(dateStr)
}

// 자식(MyCalendar) 달력이 켜지거나 월을 바꿨을 때, 기간을 받아와서 백엔드 찌르는 함수
const handleEventsLoaded = async ({ startDate, endDate }) => {
  currentPeriod.value = { startDate, endDate }
  await fetchCalendarList({ startDate, endDate })
}

// 💡 [통합 리팩토링] 등록 및 수정 완료 시 공통으로 실행되는 저장 핸들러!
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

// DailySchedule이 보낸 수정 신호를 처리하는 핸들러 함수
const handleTodoEdit = (eventObj) => {
  selectedTodoData.value = eventObj // 클릭한 할 일 정보를 바구니에 저장
  isEditModalOpen.value = true // 수정 모달 열기!
}

// 삭제 신호를 받아서 처리하는 핸들러 함수 추가
const handleTodoDelete = async (todoId) => {
  const isSuccess = await deleteTodo(todoId)

  if (isSuccess) {
    alert('할 일이 성공적으로 삭제되었습니다. 🌿')
    await refreshCalendarList()
  } else {
    alert('할 일 삭제에 실패했습니다.')
  }
}

// 💡 등록/수정/삭제 후 달력 목록을 새로고침하는 공통 로직 분리
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
      <div class="calendar-section">
        <MyCalendar
          :events="allEvents"
          :selected-date="selectedDate"
          @date-selected="handleDateSelected"
          @events-loaded="handleEventsLoaded"
        />
      </div>
      <div class="schedule-section">
        <DailySchedule
          :selected-date="selectedDate"
          :events="filteredEvents"
          @open-register="isRegisterModalOpen = true"
          @delete-todo="handleTodoDelete"
          @edit-todo="handleTodoEdit"
        />
      </div>
    </div>

    <TodoRegisterModal
      v-if="isRegisterModalOpen"
      :initial-date="selectedDateString"
      @close="isRegisterModalOpen = false"
      @saved="handleTodoSaved"
    />

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
</style>
