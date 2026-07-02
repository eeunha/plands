<script setup>
import '@/assets/styles/modal.css'
import { ref, reactive, onMounted } from 'vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'
import { useAuthStore } from '@/stores/authStore.js'
import api from '@/utils/api.js'

// 💡 부모가 넘겨주는 날짜 prop 정의
const props = defineProps({
  initialDate: {
    type: String,
    default: '',
  },
  isEditMode: {
    type: Boolean,
    default: false,
  },
  todoData: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['saved', 'close'])

const authStore = useAuthStore()
const { createTodo, updateTodo } = useCalendarApi()

// 💡 드롭다운들에 뿌려줄 목록 데이터들을 담을 변수
const todoTypes = ref([])
const memberPlants = ref([])

// 💡 커스텀 드롭다운의 열림 상태와 선택된 텍스트를 관리할 변수
const isTypeDropdownOpen = ref(false)
const selectedTypeName = ref('할 일을 선택해주세요.')
const selectedColorCode = ref('')

// 2. 폼 데이터를 하나의 객체로 이쁘게 관리 (할 일 중심 네이밍!)
const todoForm = reactive({
  todoTypeId: null,
  dueDate: props.initialDate, // 부모 달력에서 선택했던 날짜
  memberPlantIds: [],
})

// 할 일 종류를 클릭했을 때 실행할 함수
const selectTodoType = (type) => {
  todoForm.todoTypeId = type.todoTypeId
  selectedTypeName.value = type.typeName
  selectedColorCode.value = type.colorCode
  isTypeDropdownOpen.value = false // 메뉴 닫기
}

// 커스텀 드롭다운 토글 함수
const toggleTypeDropdown = () => {
  isTypeDropdownOpen.value = !isTypeDropdownOpen.value
}

// 4. 할 일 저장 함수 (등록과 수정을 여기서 분기 처리해버림!)
const saveTodo = async () => {
  // 간단한 유효성 검사
  if (!todoForm.dueDate) {
    alert('날짜를 선택해주세요.')
    return
  }
  if (!todoForm.todoTypeId) {
    alert('할 일 종류를 선택해주세요.')
    return
  }
  if (todoForm.memberPlantIds.length === 0) {
    alert('적어도 하나의 식물을 선택해주세요.')
    return
  }

  // 백엔드 DTO 가방 구조와 1:1 매칭
  const todoData = {
    todoTypeId: Number(todoForm.todoTypeId),
    dueDate: todoForm.dueDate,
    memberPlantIds: todoForm.memberPlantIds.map(Number),
  }

  let isSuccess = false

  // ✏️ 모드에 따라 전송할 Axios API 함수를 스위칭한다!
  if (props.isEditMode) {
    const todoId = props.todoData.id
    isSuccess = await updateTodo(todoId, todoData)
  } else {
    isSuccess = await createTodo(todoData)
  }

  if (isSuccess) {
    alert(
      props.isEditMode
        ? '할 일이 성공적으로 수정되었습니다. 🌱'
        : '할 일이 성공적으로 등록되었습니다. 🌱',
    )
    emit('saved', todoForm.dueDate)
    emit('close')
  } else {
    alert(props.isEditMode ? '할 일 수정에 실패했습니다.' : '할 일 등록에 실패했습니다.')
  }
}

// 3. 모달이 열릴 때 드롭다운에 뿌릴 식물 목록과 할 일 종류를 서버에서 받아옴
// 만약 '수정 모드'라면 기존 데이터를 폼에 바인딩!
onMounted(async () => {
  try {
    const [typeRes, plantRes] = await Promise.all([
      api.get('/api/calendar/todo-types'),
      api.get(`/api/calendar/member-plants`),
    ])
    todoTypes.value = typeRes.data
    memberPlants.value = plantRes.data

    // 수정 모드 분기. 부모가 준 데이터 있다면 폼에 넣기
    if (props.isEditMode && props.todoData) {
      console.log('부모가 넘겨준 전체 todoData:', props.todoData)
      console.log('그 중 식물 리스트(plants) 구조:', props.todoData.plants)

      // FullCalendar의 event.id = 백엔드 todoId
      todoForm.todoTypeId = props.todoData.todoTypeId || props.todoData.extendedProps?.todoTypeId
      todoForm.dueDate = props.todoData.start // 기존 수행 날짜 고정

      // 기존에 매핑되어 있던 식물 ID 배열 세팅
      if (props.todoData.plants) {
        todoForm.memberPlantIds = props.todoData.plants.map((p) => p.memberPlantId)
      }

      // 텍스트와 컬러칩도 기존에 맞게 연동
      selectedTypeName.value = props.todoData.title
      selectedColorCode.value = props.todoData.color
    }
  } catch (err) {
    console.error('드롭다운 목록 로드 실패:', err)
  }
})
</script>
<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <h3>{{ isEditMode ? '할 일 수정하기 ✏️' : '새 할 일 등록' }}</h3>

      <!-- 날짜 선택 섹션 -->
      <div class="form-group">
        <label>수행 날짜</label>
        <input type="date" v-model="todoForm.dueDate" class="modal-input" />
      </div>

      <!-- 할 일 종류 선택 (드롭다운) -->
      <div class="form-group">
        <label>할 일 종류</label>
        <div class="custom-dropdown">
          <button type="button" class="dropdown-toggle" @click="toggleTypeDropdown">
            <span
              v-if="todoForm.todoTypeId"
              class="color-dot"
              :style="{ backgroundColor: selectedColorCode }"
            ></span>
            <span :class="{ 'placeholder-text': !todoForm.todoTypeId }">
              {{ selectedTypeName }}
            </span>
            <span class="arrow">▼</span>
          </button>

          <ul v-if="isTypeDropdownOpen" class="dropdown-menu-list">
            <li
              v-for="type in todoTypes"
              :key="type.todoTypeId"
              @click="selectTodoType(type)"
              class="dropdown-item"
            >
              <span class="color-dot" :style="{ backgroundColor: type.colorCode }"></span>
              <span class="item-text">{{ type.typeName }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- 식물 다중 선택 섹션 (체크박스 스타일) -->
      <div class="form-group">
        <label>대상 식물 (중복 선택 가능)</label>
        <div class="plant-checkbox-list">
          <label
            v-for="plant in memberPlants"
            :key="plant.memberPlantId"
            class="plant-checkbox-item"
          >
            <input type="checkbox" :value="plant.memberPlantId" v-model="todoForm.memberPlantIds" />
            {{ plant.plantNickname || plant.plantName }}
          </label>
        </div>
        <p v-if="memberPlants.length === 0" class="empty-text">등록된 식물이 없습니다.</p>
      </div>

      <div class="modal-buttons">
        <button
          class="modal-button primary"
          @click="saveTodo"
        >
          {{ isEditMode ? '수정완료' : '등록' }}
        </button>
        <button class="modal-button" @click="$emit('close')">취소</button>
      </div>
    </div>
  </div>
</template>
<style scoped>
/* 💡 할 일 폼에 맞는 간단한 스타일만 추가 (기존 modal.css와 결합됨) */
.form-group {
  margin-bottom: 20px;
  text-align: left;
}
.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}
.modal-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}
/* 🌟 커스텀 드롭다운 디자인 */
.custom-dropdown {
  position: relative;
  width: 100%;
}
.dropdown-toggle {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 14px;
}
.placeholder-text {
  color: #999;
}

.arrow {
  margin-left: auto; /* 화살표를 오른쪽 끝으로 밀기 */
  font-size: 10px;
  color: #aaa;
}

/* 툭 떨어지는 목록 상자 */
.dropdown-menu-list {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0;
  margin: 4px 0 0 0;
  list-style: none;
  max-height: 160px;
  overflow-y: auto;
  z-index: 100;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.dropdown-item:hover {
  background-color: #f3f4f6;
}

/* 🎨 은하가 원했던 완벽한 동그라미 컬러칩 CSS */
.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 10px;
  display: inline-block;
  flex-shrink: 0;
}

.item-text {
  color: #333;
  font-size: 14px;
}

/* 식물 체크박스 리스트 관련 */
.plant-checkbox-list {
  max-height: 120px;
  overflow-y: auto;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 4px;
  background: #fff;
}
.plant-checkbox-item {
  display: block;
  margin-bottom: 6px;
  cursor: pointer;
  font-weight: normal !important;
}
.empty-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
