<template>
  <div class="home-container">
    <div v-if="isLoggedIn">
      <CalendarView />
    </div>
    <div v-else>
      <Introduce />
    </div>
  </div>
</template>

<script>
import Introduce from '@/components/Introduce.vue'
import { useAuthStore } from '@/stores/authStore.js'
import CalendarView from '@/views/CalendarView.vue'
export default {
  name: 'HomeView',
  created() {
    const authStore = useAuthStore()
    if (authStore.isLoggedIn) {
      this.$router.replace('/calendar')
    }
  },
  data() {
    return {}
  },
  computed: {
    isLoggedIn() {
      const authStore = useAuthStore()
      return authStore.isLoggedIn
    },
  },
  components: {
    CalendarView,
    Introduce,
  },
}
</script>

<style scoped>
:global(html, body) {
  margin: 0;
  padding: 0;
  overflow: hidden; /* 스크롤 강제 차단 */
}

.home-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex; /* flex로 내부 요소 높이 강제 조정 */
  flex-direction: column;
}

.home-container > div {
  flex: 1;
}
</style>
