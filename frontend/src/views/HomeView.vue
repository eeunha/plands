<template>
  <div class="home-container">
    <Introduce />
  </div>
</template>

<script>
import Introduce from '@/components/Introduce.vue'
export default {
  name: 'HomeView',
  data() {
    return {}
  },
  components: {
    Introduce,
  },
}
</script>

<style scoped>
.home-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>
