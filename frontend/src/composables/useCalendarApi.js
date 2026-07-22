import { ref } from 'vue'
import api from '@/utils/api.js'

const cachedTodoTypes = ref(null)
const cachedMemberPlants = ref(null)

export function useCalendarApi() {
  const allEvents = ref([])
  const loading = ref(false)
  const error = ref(null)

  // 캐시와 일정 목록 비우기
  const resetCalendarState = () => {
    cachedTodoTypes.value = null
    cachedMemberPlants.value = null
    allEvents.value = []
  }

  // 캘린더 전체 할 일 조회 함수 (GET)
  const fetchCalendarList = async ({ startDate, endDate }) => {
    loading.value = true
    error.value = null
    try {
      const res = await api.get('/api/todo', {
        params: { startDate, endDate },
      })
      allEvents.value = res.data
    } catch (err) {
      error.value = err
      console.error('달력 데이터 가져오기 실패:', err)
    } finally {
      loading.value = false
    }
  }

  // 💡 모달 폼용: 할 일 카테고리 타입 리스트 조회 (GET)
  const getTodoTypes = async () => {
    // 1. 이미 기억해둔 데이터가 있다면? 백엔드 안 찌르고 바로 반환!
    if (cachedTodoTypes.value) {
      return cachedTodoTypes.value
    }

    // 2. 기억해둔 게 없으면 백엔드 찌르기
    error.value = null
    try {
      const res = await api.get('/api/todo/type')
      cachedTodoTypes.value = res.data
      return res.data
    } catch (err) {
      error.value = err
      console.error('할 일 카테고리 로드 실패:', err)
      throw err // 오류가 발생하면 컴포넌트 catch문으로 던짐
    }
  }

  // 💡 모달 폼용: 회원의 반려 식물 리스트 조회 (GET)
  const getMemberPlants = async () => {
    // 1. 이미 기억해둔 데이터가 있다면? 바로 반환!
    if (cachedMemberPlants.value) {
      return cachedMemberPlants.value
    }

    // 2. 캐시된 정보 없을 때
    error.value = null
    try {
      const res = await api.get('/api/calendar/member-plant')
      cachedMemberPlants.value = res.data
      return res.data
    } catch (err) {
      error.value = err
      console.error('회원 식물 목록 로드 실패:', err)
      throw err
    }
  }

  // 새 할 일 등록 함수 (POST)
  const createTodo = async (todoData) => {
    loading.value = true
    error.value = null
    try {
      const res = await api.post('/api/todo', todoData)
      return true // 성공하면 컴포넌트(모달)단에 성공 시그널을 보냄!
    } catch (err) {
      error.value = err
      console.error('할 일 등록 실패:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 할 일 삭제 함수 (DELETE) - soft delete도 delete. 행위의 목적!
  const deleteTodo = async (todoId) => {
    loading.value = true
    error.value = null
    try {
      const res = await api.delete(`/api/todo/${todoId}`)
      return true
    } catch (err) {
      error.value = err
      console.error('할 일 삭제 실패:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 할 일 수정 함수 (PUT) - 리소스가 무겁지 않고 백엔드 구현이 편하며 객체 통째로 던져주는 구조가 편해 PATCH 대신 PUT 사용
  const updateTodo = async (todoId, todoData) => {
    loading.value = true
    error.value = null
    try {
      const res = await api.put(`/api/todo/${todoId}`, todoData)
      return true
    } catch (err) {
      error.value = err
      console.error('할 일 수정 실패:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // --- 일기(Diary) 관련 API ---
  // 새 한 줄 일기 등록 함수 (POST)
  const createDiary = async (diaryData) => {
    loading.value = true
    error.value = null
    try {
      const formData = new FormData()
      formData.append('content', diaryData.content)
      formData.append('diaryDate', diaryData.diaryDate)

      if (diaryData.image) {
        formData.append('image', diaryData.image)
      }

      await api.post('/api/diary', formData, {
        headers: {
          'Content-Type': undefined, // 👈 전역 설정된 application/json을 무시하고 브라우저가 boundary를 잡게 함!
        },
      })
      return true
    } catch (err) {
      error.value = err
      console.error('한 줄 일기 등록 실패:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    allEvents,
    loading,
    error,
    resetCalendarState,
    fetchCalendarList,
    getTodoTypes,
    getMemberPlants,
    createTodo,
    deleteTodo,
    updateTodo,
    createDiary,
  }
}
