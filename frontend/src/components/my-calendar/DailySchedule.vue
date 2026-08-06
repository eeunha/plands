<script setup>
import { computed } from 'vue'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'

const props = defineProps({
  selectedDate: {
    type: Date,
    required: true,
  },
  events: {
    type: Array,
    default: () => [],
  },
})

// 💡 부모에게 신호를 보내기 위한 emit 정의
const emit = defineEmits(['open-register', 'edit-todo', 'delete-todo'])

const formattedDate = computed(() => {
  return format(props.selectedDate, 'yyyy년 M월 d일 (EEE)', { locale: ko })
})

// 수정 버튼 클릭 시 작동 함수
const clickEditTodo = (eventObj) => {
  emit('edit-todo', eventObj)
}

// 🌟 삭제 버튼을 클릭했을 때 작동하는 함수
const clickDeletedTodo = (todoId) => {
  if (confirm('이 할 일을 정말 삭제하시겠습니까?')) {
    emit('delete-todo', todoId)
  }
}
</script>

<template>
  <div class="daily-schedule">
    <div class="schedule-header">
      <h3>{{ formattedDate }} 할 일</h3>
    </div>

    <p v-if="events.length === 0" class="no-events">{{ formattedDate }}에는 할 일이 없습니다.</p>

    <ul v-else>
      <li v-for="event in events" :key="event.id" class="schedule-item">
        <div class="item-main-wrapper">
          <div class="item-header">
            <span class="color-badge" :style="{ backgroundColor: event.color }"></span>
            <strong class="event-title">{{ event.title }}</strong>
          </div>

          <div class="button-group">
            <button class="btn-edit" @click="clickEditTodo(event)" title="할 일 수정">✏️</button>
            <button class="btn-delete" @click="clickDeletedTodo(event.id)" title="할 일 삭제">
              🗑️
            </button>
          </div>
        </div>

        <div v-if="event.plants && event.plants.length > 0" class="plant-list">
          <span v-for="(plant, index) in event.plants" :key="index" class="plant-badge">
            🌿 {{ plant.plantName }}
          </span>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.daily-schedule {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #f9f9f9;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  min-height: 458px;
  overflow-y: auto;

  /* 자식 요소들을 세로로 나열하는 flex box로 변환 */
  display: flex;
  flex-direction: column;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 2px solid #10b981;
  padding-bottom: 10px;
}

h3 {
  color: #333;
  margin: 0;
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

/* 💡 타이틀과 쓰레기통을 양끝으로 밀어버리기 위한 가로 정렬용 */
.item-main-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-header {
  display: flex;
  align-items: center;
}

/* 색상 칩 동그라미 스타일 예쁘게 잡기 */
.color-badge {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.event-title {
  color: #10b981;
  font-size: 1.1em;
}

/* style scoped 맨 아래에 예쁜 초록색 마우스오버 효과 추가 */
.button-group {
  display: flex;
  gap: 4px;
}

.btn-edit {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 4px;
  border-radius: 4px;
  transition:
    background-color 0.2s,
    transform 0.1s;
}

.btn-edit:hover {
  background-color: #e6f4ea; /* 에메랄드 한 스푼 얹은 마우스오버 */
  transform: scale(1.1);
}

/* 🌟 깔끔한 미니멀 쓰레기통 버튼 스타일 */
.btn-delete {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 4px;
  border-radius: 4px;
  transition:
    background-color 0.2s,
    transform 0.1s;
}

.btn-delete:hover {
  background-color: #fee2e2; /* 살짝 붉은 마우스오버 효과 */
  transform: scale(1.1);
}

.no-events {
  color: #888;
  text-align: center;

  /* 마진 탑을 없애고, flex 안에서 남은 세로 공간을 100% 꽉 채우게 만듦 */
  margin: 0;
  flex-grow: 1;

  /* 텍스트 자체를 세로/가로 정중앙으로 완벽하게 밀어 넣는 마법의 3줄 */
  display: flex;
  justify-content: center;
  align-items: center;
}

.plant-list {
  margin-top: 8px;
}

.plant-badge {
  background-color: #ecfdf5;
  color: #065f46 !important;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.85em !important;
  margin-right: 5px;
  display: inline-block;
}
</style>
