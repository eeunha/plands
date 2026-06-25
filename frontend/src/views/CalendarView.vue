<script setup>
import { ref, computed, onMounted } from 'vue'
import MyCalendar from '@/components/my-calendar/MyCalendar.vue'
import DailySchedule from '@/components/my-calendar/DailySchedule.vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'

// 컴포저블 함수에서 상태(allEvents)와 API 호출 함수(fetchCalendarList) 가져오기
const { allEvents, fetchCalendarList } = useCalendarApi()

// 오른쪽 탭에 보여줄 기준 날짜 (기본값: 오늘)
const selectedDate = ref(new Date())

// 1. 백엔드에서 받은 한 달치(allEvents) 중, 선택된 날짜 하루치만 칼같이 필터링
const filteredEvents = computed(() => {
  // 자바스크립트 Date 객체를 '2026-06-25' 형태의 문자열로 안전하게 변환하는 로직
  const offset = selectedDate.value.getTimezoneOffset() * 60000 // 영국과 한국의 시간차(ms)
  const selectedDateStr = new Date(selectedDate.value.getTime() - offset)
    .toISOString()
    .split('T')[0]

  // 백엔드 DTO의 날짜 변수명이 start이므로 event.start로 매핑해서 비교해야 함!
  // 혹시 몰라 만약의 에러를 방지하기 위해 allEvents가 비어있을 때의 예외 처리 추가
  if (!allEvents.value) return []
  return allEvents.value.filter((event) => event.start === selectedDateStr)
})

// 2. 달력에서 날짜를 클릭했을 때 실행될 함수
const handleDateSelected = (dateStr) => {
  // 자식(MyCalendar)이 '2026-06-25' 문자열을 쏴주면, 그걸 받아 Date 객체로 변환해 저장
  selectedDate.value = new Date(dateStr)
  console.log('CalendarView: 유저가 클릭한 날짜 ->', dateStr)
}

// 3. 자식(MyCalendar) 달력이 켜지거나 월을 바꿨을 때, 기간을 받아와서 백엔드 찌르는 함수
const handleEventsLoaded = async ({ startDate, endDate }) => {
  console.log(`CalendarView: 달력이 감지한 기간으로 백엔드 조회 요청! [${startDate} ~ ${endDate}]`)
  // 컴포저블 API 호출 (객체 구조로 전달)
  await fetchCalendarList({ startDate, endDate })
}

onMounted(() => {
  console.log('CalendarView가 마운트되었습니다.')
})
</script>

<template>
  <div class="calendar-view-container">
    <h2>나의 일정</h2>
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
        <DailySchedule :selected-date="selectedDate" :events="filteredEvents" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.calendar-view-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
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
