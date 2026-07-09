import { ref } from 'vue'
import api from '@/utils/api.js'

export function useCalendarApi() {
  const allEvents = ref([])
  const loading = ref(false)
  const error = ref(null)

  // 캘린더 전체 일정 조회 함수 (GET)
  const fetchCalendarList = async ({ startDate, endDate }) => {
    loading.value = true
    error.value = null
    try {
      const res = await api.get('/api/calendar/todo', {
        params: { startDate, endDate },
      })
      allEvents.value = res.data
      console.log(allEvents.value)
    } catch (err) {
      error.value = err
      console.error('달력 데이터 가져오기 실패:', err)
    } finally {
      loading.value = false
    }
  }

  // 💡 모달 폼용: 할 일 카테고리 타입 리스트 조회 (GET)
  const getTodoTypes = async () => {
    error.value = null
    try {
      const res = await api.get('/api/calendar/todo-types')
      return res.data
    } catch (err) {
      error.value = err
      console.error('할 일 카테고리 로드 실패:', err)
      throw err // 오류가 발생하면 컴포넌트 catch문으로 던짐
    }
  }

  // 💡 모달 폼용: 회원의 반려 식물 리스트 조회 (GET)
  const getMemberPlants = async () => {
    error.value = null
    try {
      const res = await api.get('/api/calendar/member-plants')
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
      const res = await api.post('/api/calendar/todo', todoData)
      console.log('할 일 등록 성공:', res.data)
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
      const res = await api.delete(`/api/calendar/todo/${todoId}`)
      console.log('할 일 삭제 성공:', res.data)
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
      const res = await api.put(`/api/calendar/todo/${todoId}`, todoData)
      console.log('할 일 수정 성공:', res.data)
      return true
    } catch (err) {
      error.value = err
      console.log('할 일 수정 실패:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    allEvents,
    loading,
    error,
    fetchCalendarList,
    getTodoTypes,
    getMemberPlants,
    createTodo,
    deleteTodo,
    updateTodo,
  }
}
