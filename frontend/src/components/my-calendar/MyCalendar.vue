<script setup>
import { ref, watch, computed } from 'vue'
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
  diaries: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  selectedDate: {
    type: Date,
    required: true,
  },
})

// FullCalendar 태그에 접근하기 위한 DOM 참조 변수
const fullCalendarRef = ref(null)

// 날짜 비교를 위한 유틸 함수 (UTC/KST 이슈 깔끔하게 정리)
const formatDateToString = (date) => {
  const offset = date.getTimezoneOffset() * 60000
  return new Date(date.getTime() - offset).toISOString().split('T')[0]
}

// 빠른 조회를 위해 diaries를 Set 자료구조로 변환 (O(1) 시간 복잡도로 성능 최적화)
const diaryDateSet = computed(() => {
  return new Set(props.diaries?.map((diary) => diary.diaryDate) || [])
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  weekends: true,
  locale: 'ko',

  // 달력 높이를 내부 콘텐츠에 맞게 자동 조절 ➡️ 내부 스크롤바 원천 차단!
  height: 'auto',

  // 5줄만 필요한 달은 5줄만, 6줄이 필요한 달은 6줄로 상황에 맞게 가변 처리됨
  fixedWeekCount: false,

  // 하루에 보여줄 최대 이벤트(할 일) 개수 제한!
  // 딱 3개까지만 화면에 노출하고, 초과되면 아래에 자동으로 "+N개 더" 표시를 띄워줌
  dayMaxEvents: 3,

  events: props.events, // computed 내부에서 props 직접 참조

  // 상단 헤더 버튼 커스텀 (today 버튼 클릭 가로채기)
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
        if (calendarApi) {
          calendarApi.today()
          emit('date-selected', formatDateToString(new Date()))
        }
      },
    },
  },

  // 달력 칸이 그려질 때마다 실행되면서 특정 클래스를 붙여주는 함수
  dayCellClassNames: (arg) => {
    // 1) 현재 그리고 있는 칸의 날짜 시차 교정 (UTC -> KST)
    const cellDateStr = formatDateToString(arg.date)

    // 2) 부모가 선택해서 들고 있는 날짜 시차 교정 (UTC -> KST)
    const selectedDateStr = formatDateToString(props.selectedDate)

    return cellDateStr === selectedDateStr ? ['selected-date'] : []
  },

  // 달력 날짜 셀 내부의 콘텐츠를 커스텀하는 훅
  dayCellContent: (arg) => {
    const cellDateStr = formatDateToString(arg.date)

    // 해당 날짜에 일기가 있는지 확인
    const hasDiary = diaryDateSet.value.has(cellDateStr)

    // 기본으로 표시되는 날짜 숫자 (예: "25" -> "25일" 등에서 숫자만 추출)
    const dayNumber = arg.dayNumberText

    const wrapper = document.createElement('div')
    wrapper.className = 'day-cell-content-wrapper'

    if (hasDiary) {
      const icon = document.createElement('span')
      icon.className = 'diary-marker-icon'
      icon.textContent = '🌿'
      wrapper.appendChild(icon)
    }

    const numSpan = document.createElement('span')
    numSpan.className = 'day-num'
    numSpan.textContent = dayNumber
    wrapper.appendChild(numSpan)

    return { domNodes: [wrapper] }
  },

  // 달력이 처음 켜지거나, 유저가 [이전달]/[다음달] 버튼을 누를 때마다 자동 실행!
  datesSet: (info) => {
    // FullCalendar가 계산한 화면상의 전체 시작일과 종료일 (ex: 5월 31일 ~ 7월 12일)
    // 화면 범위 계산 및 백엔드 데이터 조회 요청 (기존 동일)
    const startDate = info.startStr.split('T')[0]
    const endDate = info.endStr.split('T')[0]

    // 부모(CalendarView)에게 "이 기간만큼 백엔드에서 데이터 조회해와!" 하고 신호 보냄
    emit('events-loaded', { startDate, endDate })

    // 달력이 보여주는 '진짜 현재 월의 1일' 정보
    // info.view.currentStart는 FullCalendar가 인지하는 '진짜 현재 월의 1일' (Date 객체)
    const currentMonthFirstDay = info.view.currentStart

    // 진짜 실제 오늘(Today)의 날짜 정보 가져오기
    const realToday = new Date()

    // 1) 현재 달력에 보이는 월과 부모가 선택한 월이 같은지 비교
    const isSameYearMonth =
      props.selectedDate.getFullYear() === currentMonthFirstDay.getFullYear() &&
      props.selectedDate.getMonth() === currentMonthFirstDay.getMonth()

    // 2) 만약 다른 달로 넘어갔다면? (월 이동 감지)
    if (!isSameYearMonth) {
      // 이동한 달이 '진짜 오늘'이 속한 년/월과 같다면 -> 오늘 날짜 선택!
      if (
        realToday.getFullYear() === currentMonthFirstDay.getFullYear() &&
        realToday.getMonth() === currentMonthFirstDay.getMonth()
      ) {
        // 이동한 달이 이번 달(6월)이라면? 1일이 아니라 '진짜 오늘 날짜'를 선택!
        emit('date-selected', formatDateToString(realToday))
      } else {
        // 아니면 해당 월의 1일 선택!
        emit('date-selected', formatDateToString(currentMonthFirstDay))
      }
    }
  },

  // 달력에서 특정 날짜 칸을 마우스로 클릭했을 때 실행
  dateClick: (info) => {
    // 부모에게 클릭한 날짜 문자열(ex: '2026-06-25')을 쏴서 오른쪽 탭을 바꾸게 만듦
    emit('date-selected', info.dateStr)
  },

  // 할 일 스티커(이벤트)를 클릭했을 때도 똑같이 작동하게 만들기!
  eventClick: (info) => {
    emit('date-selected', info.event.startStr)
  },
}))

// diaries(일기) 목록이 바뀌면 달력 화면을 강제로 다시 그려서 🌿 마커를 즉시 반영함!
watch(
  () => props.diaries,
  () => {
    const calendarApi = fullCalendarRef.value?.getApi()
    if (calendarApi) {
      calendarApi.render()
    }
  },
  { deep: true },
)

// 선택된 날짜 변경 시그널 처리 (메서드로 분리하여 가독성 향상)
watch(
  () => props.selectedDate,
  (newDate) => {
    const calendarApi = fullCalendarRef.value?.getApi()
    if (!calendarApi) return

    const currentStart = calendarApi.view.currentStart
    if (
      newDate.getFullYear() !== currentStart.getFullYear() ||
      newDate.getMonth() !== currentStart.getMonth()
    ) {
      calendarApi.gotoDate(newDate)
    } else {
      calendarApi.render()
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="calendar-container">
    <div v-if="loading" class="loading-text">할 일 불러오는 중...</div>
    <FullCalendar ref="fullCalendarRef" :options="calendarOptions" />
  </div>
</template>

<style scoped>
.calendar-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  overflow: hidden; /* [★ 추가] 혹시라도 달력이 컨테이너를 삐져나가 스크롤바를 만드는 현상 방지 */
}

.loading-text {
  text-align: center;
  color: #10b981; /* 은하가 좋아하는 에메랄드 포인트 색상 */
  font-weight: bold;
  margin-bottom: 10px;
}

:deep(.fc-daygrid-day.selected-date) {
  background-color: #e6f4ea !important;
  transition: background-color 0.2s ease;
}

/* 오늘 날짜 기본 테두리나 배경 튜닝 */
:deep(.fc-day-today) {
  background-color: #f8fafc !important;
}

/* 달력의 각 날짜 칸이 할 일이 없어도 최소 이만큼의 높이를 유지하도록 설정 */
:deep(.fc-daygrid-day-frame) {
  min-height: 85px;
}

/* 날짜 셀 내부 래퍼 스타일 (가로 정렬로 변경) */
:deep(.day-cell-content-wrapper) {
  display: flex;
  flex-direction: row; /* 👈 세로에서 가로 방향으로 변경 */
  justify-content: space-between; /* 👈 좌우 끝으로 밀어주기 (왼쪽: 아이콘, 오른쪽: 숫자) */
  align-items: center;
  width: 100%;
  padding: 2px 4px; /* 살짝의 여백 추가 */
}

/* 아이콘 스타일 */
:deep(.diary-marker-icon) {
  font-size: 14px;
  line-height: 1;
  margin-right: 12px;
}

/* 날짜 숫자 스타일 */
:deep(.day-num) {
  font-size: 15px;
}
</style>
