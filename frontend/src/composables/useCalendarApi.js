import { ref } from 'vue'
import api from '@/utils/api.js'

export function useCalendarApi() {
  const allEvents = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchCalendarList = async ({startDate, endDate}) => {
    loading.value = true
    error.value = null
    try {
      const res = await api.get('/api/calendar/todo', {
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

  return {
    allEvents,
    loading,
    error,
    fetchCalendarList,
  }
}
