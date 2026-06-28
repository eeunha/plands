import { ref } from 'vue'
import api from '@/utils/api.js'

export function useCalendarApi() {
  const allEvents = ref([])
  const loading = ref(false)
  const error = ref(null)

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
      return false;
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
      return false;
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
    createTodo,
    deleteTodo,
    updateTodo,
  }
}
