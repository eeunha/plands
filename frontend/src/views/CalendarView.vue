<script setup>
import { ref, computed, onMounted } from 'vue'
import MyCalendar from '@/components/my-calendar/MyCalendar.vue'
import DailySchedule from '@/components/my-calendar/DailySchedule.vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'

const { allEvents, fetchCalendarList } = useCalendarApi()

const selectedDate = ref(new Date())

// const allEvents = ref([
//   {
//     id: '1',
//     title: '팀 미팅',
//     date: '2026-06-26',
//     time: '10:00 AM',
//     description: '주간 업무 논의',
//   },
//   {
//     id: '2',
//     title: '보고서 제출',
//     date: '2026-06-26',
//     time: '05:00 PM',
//     description: '월간 보고서 마감',
//   },
//   { id: '3', title: '운동하기', date: '2026-06-26', time: '07:00 PM' },
//   {
//     id: '4',
//     title: '친구와 저녁',
//     date: '2026-06-27',
//     time: '07:30 PM',
//     description: '강남역 맛집',
//   },
//   { id: '5', title: '병원 예약', date: '2026-06-28', time: '02:00 PM' },
//   { id: '6', title: 'FullCalendar 학습', date: '2025-06-29' },
//   { id: '7', title: '장보기', date: '2026-06-29' },
// ])

const filteredEvents = computed(() => {
  const offset = selectedDate.value.getTimezoneOffset() * 60000 // 영국과 한국의 시간차(ms)
  const selectedDateStr = new Date(selectedDate.value.getTime() - offset)
    .toISOString()
    .split('T')[0]

  return allEvents.value.filter((event) => event.date === selectedDateStr)
})

const handleDateSelected = (dateInfo) => {
  selectedDate.value = dateInfo.date
  console.log('CalendarView: 선택된 날짜:', selectedDate.value)
}

const refreshPage = () => {
  const currentYear = selectedDate.value.getFullYear()
  const currentMonth = selectedDate.value.getMonth() + 1

  console.log(`CalendarView: 백엔드로 ${currentYear}년 ${currentMonth}월 일정 조회 요청!`)

  fetchCalendarList({ year: currentYear, month: currentMonth })
}

onMounted(() => {
  console.log('CalendarView가 마운트되었습니다.')
  refreshPage()
})
</script>

<template>
  <div class="calendar-view-container">
    <h2>나의 일정</h2>
    <div class="content-wrapper">
      <div class="calendar-section">
        <MyCalendar @date-selected="handleDateSelected" />
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
