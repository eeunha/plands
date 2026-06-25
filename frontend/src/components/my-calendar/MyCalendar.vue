<template>
  <div class="calendar-container">
    <div v-if="loading" class="loading-text">일정 불러오는 중...</div>
    <FullCalendar ref="fullCalendarRef" :options="calendarOptions" />
  </div>
</template>

<script setup>
import { reactive, watch, defineEmits, ref } from 'vue' // defineProps, watch 제거
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'

// 부모(CalendarView)에게 데이터와 신호를 보내기 위한 에밋 선언
const emit = defineEmits(['date-selected', 'events-loaded'])

// 부모가 백엔드에서 받아온 한 달치 실시간 데이터(events)를 Props로 전달받음
const props = defineProps({
  events: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  // [★ 추가] 부모가 기억하고 있는 현재 선택된 날짜 객체와 동기화하기 위해 props 추가!
  selectedDate: {
    type: Date,
    required: true,
  },
})

// FullCalendar 태그에 접근하기 위한 DOM 참조 변수
const fullCalendarRef = ref(null)

const calendarOptions = reactive({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  weekends: true,
  locale: 'ko',

  // 달력 화면에 그려질 실시간 스티커 리스트 (초기값은 빈 배열)
  events: [],

  // [★ 2번 요구사항 해결] 상단 헤더 버튼 커스텀 (today 버튼 클릭 가로채기)
  headerToolbar: {
    left: 'title',
    center: '',
    right: 'customToday prev,next', // 원래 'today' 대신 우리가 만든 'customToday'를 배치!
  },
  customButtons: {
    customToday: {
      text: 'today',
      click: () => {
        // 1) 달력 라이브러리 자체를 오늘 날짜 화면으로 이동시킴
        const calendarApi = fullCalendarRef.value.getApi()
        calendarApi.today()

        // 2) 오늘 날짜를 '2026-06-25' 형태로 가공해서 부모 관제탑에 전달 ➡️ 오른쪽 탭도 오늘로 동기화!
        const todayStr = new Date().toISOString().split('T')[0]
        emit('date-selected', todayStr)
      },
    },
  },

  // 달력 칸이 그려질 때마다 실행되면서 특정 클래스를 붙여주는 함수
  dayCellClassNames: (arg) => {
    // 1) 현재 그리고 있는 칸의 날짜 시차 교정 (UTC -> KST)
    const cellOffset = arg.date.getTimezoneOffset() * 60000
    const cellDateStr = new Date(arg.date.getTime() - cellOffset).toISOString().split('T')[0]

    // 2) 부모가 선택해서 들고 있는 날짜 시차 교정 (UTC -> KST)
    const selectedOffset = props.selectedDate.getTimezoneOffset() * 60000
    const selectedDateStr = new Date(props.selectedDate.getTime() - selectedOffset)
      .toISOString()
      .split('T')[0]

    // 이제 두 문자열이 똑같이 한국 시간 기준으로 비교돼!
    if (cellDateStr === selectedDateStr) {
      return ['selected-date']
    }
    return []
  },

  // 달력이 처음 켜지거나, 유저가 [이전달]/[다음달] 버튼을 누를 때마다 자동 실행!
  datesSet: (info) => {
    // 1) FullCalendar가 계산한 화면상의 전체 시작일과 종료일 (ex: 5월 31일 ~ 7월 12일)
    // 화면 범위 계산 및 백엔드 데이터 조회 요청 (기존 동일)
    const startDate = info.startStr.split('T')[0]
    const endDate = info.endStr.split('T')[0]

    console.log('MyCalendar: 현재 화면의 날짜 범위 감지 완료 ->', startDate, '~', endDate)

    // 부모(CalendarView)에게 "이 기간만큼 백엔드에서 데이터 조회해와!" 하고 신호 보냄
    emit('events-loaded', { startDate, endDate })

    // 2) 달력이 보여주는 '진짜 현재 월의 1일' 정보
    // info.view.currentStart는 FullCalendar가 인지하는 '진짜 현재 월의 1일' (Date 객체)이야.
    const currentMonthFirstDay = info.view.currentStart

    // 3) 진짜 실제 오늘(Today)의 날짜 정보 가져오기
    const realToday = new Date()

    // 부모가 들고 있는 날짜의 '월'과 달력이 새로 보여주는 '월'이 다를 때만 작동 (월 이동 감지)
    if (props.selectedDate.getMonth() !== currentMonthFirstDay.getMonth()) {
      // [★ 핵심 예외 처리] 이동한 달이 '진짜 오늘'과 같은 년도, 같은 월인지 비교!
      if (
        realToday.getFullYear() === currentMonthFirstDay.getFullYear() &&
        realToday.getMonth() === currentMonthFirstDay.getMonth()
      ) {
        // 이동한 달이 이번 달(6월)이라면? 1일이 아니라 '진짜 오늘 날짜'를 선택!
        const offset = realToday.getTimezoneOffset() * 60000
        const todayStr = new Date(realToday.getTime() - offset).toISOString().split('T')[0]

        console.log(`MyCalendar: 이번 달로 복귀 감지! 오늘 날짜(${todayStr})로 자동 선택합니다.`)
        emit('date-selected', todayStr)
      } else {
        // 이동한 달이 완전히 다른 달(5월, 7월 등)이라면? 은하 기획대로 '그 달의 1일'을 선택!
        const offset = currentMonthFirstDay.getTimezoneOffset() * 60000
        const firstDayStr = new Date(currentMonthFirstDay.getTime() - offset)
          .toISOString()
          .split('T')[0]

        console.log(
          `MyCalendar: 다른 달로 이동 감지! 해당 월의 1일(${firstDayStr})로 자동 선택합니다.`,
        )
        emit('date-selected', firstDayStr)
      }
    }
  },

  // 달력에서 특정 날짜 칸을 마우스로 클릭했을 때 실행
  dateClick: (info) => {
    // 부모에게 클릭한 날짜 문자열(ex: '2026-06-25')을 쏴서 오른쪽 탭을 바꾸게 만듦
    emit('date-selected', info.dateStr)
    console.log('MyCalendar: 유저가 클릭한 날짜 ->', info.dateStr)
  },
})

// 부모가 백엔드에서 데이터를 새로 받아와서 props.events를 바꿔주면,
// 그걸 실시간으로 감시(watch)해서 달력 화면(calendarOptions.events)에 싹 업데이트 해줌!
watch(
  () => props.events,
  (newEvents) => {
    calendarOptions.events = newEvents
  },
  { deep: true },
)

// [★ 대박 중요] 2. 부모가 넘겨준 선택 날짜가 바뀔 때마다 달력 눈금 강제 새로고침(리렌더링)하기!
watch(
  () => props.selectedDate,
  () => {
    if (fullCalendarRef.value) {
      const calendarApi = fullCalendarRef.value.getApi()
      // FullCalendar 자체 기능을 다시 호출해서 dayCellClassNames를 강제로 재구동시킴!
      calendarApi.render()
    }
  },
)
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

.loading-text {
  text-align: center;
  color: #10b981; /* 은하가 좋아하는 에메랄드 포인트 색상 */
  font-weight: bold;
  margin-bottom: 10px;
}

/* [★ 스타일 추가] 선택된 날짜 칸의 배경색을 연한 에메랄드(화이트 톤)로 지정 */
:deep(.fc-daygrid-day.selected-date) {
  background-color: #e6f4ea !important; /* 은하의 시그니처 연한 에메랄드 */
  transition: background-color 0.2s ease;
}

/* 오늘 날짜 기본 테두리나 배경 튜닝 */
:deep(.fc-day-today) {
  background-color: #f8fafc !important;
}
</style>
