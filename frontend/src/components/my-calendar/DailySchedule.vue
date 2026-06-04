<template>
  <div class="daily-schedule">
    <h3>{{ formattedDate }} 일정</h3>
    <p v-if="events.length === 0" class="no-events">{{ formattedDate }}에는 일정이 없습니다.</p>
    <ul v-else>
      <li v-for="event in events" :key="event.id">
        <strong>{{ event.title }}</strong>
        <span v-if="event.time"> ({{ event.time }})</span>
        <p v-if="event.description">{{ event.description }}</p>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { format } from 'date-fns' // 날짜 포맷팅을 위한 라이브러리 사용 (선택 사항)
import { ko } from 'date-fns/locale' // 한국어 로케일

// --- Props 정의 (부모로부터 받을 데이터) ---
// 이 컴포넌트는 부모로부터 'selectedDate'와 'events'를 받습니다.
const props = defineProps({
  selectedDate: {
    type: Date, // Date 객체 타입을 기대합니다.
    required: true, // 이 props는 필수입니다.
  },
  events: {
    type: Array, // 배열 타입을 기대합니다.
    default: () => [], // 기본값으로 빈 배열을 가집니다 (prop이 전달되지 않을 경우).
  },
})

// --- 계산된 속성 (Computed Property) ---
// selectedDate prop을 받아서 보기 좋은 형태로 포맷팅합니다.
const formattedDate = computed(() => {
  // 'yyyy년 M월 d일 (EEE)' 형태로 포맷 (예: 2025년 6월 26일 (목))
  return format(props.selectedDate, 'yyyy년 M월 d일 (EEE)', { locale: ko })
})
</script>

<style scoped>
/* --- 스타일 (Scoped CSS) --- */
.daily-schedule {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #f9f9f9;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

h3 {
  color: #333;
  margin-bottom: 15px;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  background-color: #fff;
  margin-bottom: 10px;
  padding: 12px 15px;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

li strong {
  color: #007bff;
  font-size: 1.1em;
}

li span {
  color: #555;
  font-size: 0.9em;
  margin-left: 5px;
}

li p {
  margin-top: 5px;
  color: #666;
  font-size: 0.9em;
  line-height: 1.4;
}

.no-events {
  color: #888;
  text-align: center;
  margin-top: 20px;
}
</style>
