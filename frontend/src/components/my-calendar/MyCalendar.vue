<template>
  <div class="calendar-container">
    <FullCalendar :options="calendarOptions" />
  </div>
</template>

<script setup>
import { reactive, onMounted, defineEmits } from 'vue'; // defineProps, watch 제거
import FullCalendar from '@fullcalendar/vue3';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';

const emit = defineEmits(['date-selected']);

// selectedDate prop 관련 코드 제거 (defineProps 제거)
// const props = defineProps({
//   selectedDate: {
//     type: Date,
//     required: true,
//   },
// });

const calendarOptions = reactive({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  weekends: true,
  locale: 'ko',
  events: [
    // 필요하다면 이곳에 MyCalendar가 자체적으로 표시할 이벤트를 넣을 수 있습니다.
    { title: '내장 이벤트 1', date: '2026-06-26' },
    { title: '내장 이벤트 2', date: '2026-06-27' },
  ],

  // --- dayCellClassNames 부분 제거 ---
  // dayCellClassNames: (arg) => {
  //   const cellDateStr = arg.date.toISOString().split('T')[0];
  //   const selectedDateStr = props.selectedDate.toISOString().split('T')[0];
  //   if (cellDateStr === selectedDateStr) {
  //     return ['selected-date'];
  //   }
  //   return [];
  // },
  // ------------------------------------

  dateClick: (info) => {
    emit('date-selected', info);
    console.log('MyCalendar: 날짜 클릭됨:', info.dateStr);
  },
});

// --- watch 부분 제거 ---
// watch(() => props.selectedDate, (newDate, oldDate) => {
//   console.log('MyCalendar: selectedDate prop 변경 감지:', oldDate, '->', newDate);
// });

onMounted(() => {
  console.log('MyCalendar가 마운트되었습니다.');
});
</script>

<style scoped>
.calendar-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* --- selected-date 관련 스타일 제거 --- */
/* .fc-daygrid-day.selected-date {
  background-color: #ffe0b2;
  border-radius: 5px;
} */

/* .fc-day-today {
  background-color: #e0f2f7;
} */
</style>
