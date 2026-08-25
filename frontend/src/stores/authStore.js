import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useCalendarApi } from '@/composables/useCalendarApi.js'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)
  const isLoggedIn = ref(!!localStorage.getItem('accessToken'))
  const memberId = ref(localStorage.getItem('memberId') || null)
  const profileImageUrl = ref(localStorage.getItem('profileImageUrl') || null)
  const nickname = ref(localStorage.getItem('nickname') || null)
  const role = ref(localStorage.getItem('role') || null)

  function setProfileImageUrl(newUrl) {
    profileImageUrl.value = newUrl
    localStorage.setItem('profileImageUrl', newUrl)
  }

  function setUser(user) {
    accessToken.value = user.accessToken
    refreshToken.value = user.refreshToken
    isLoggedIn.value = true
    memberId.value = user.memberId
    profileImageUrl.value = user.profileImageUrl
    nickname.value = user.nickname
    role.value = user.role

    localStorage.setItem('accessToken', user.accessToken)
    localStorage.setItem('refreshToken', user.refreshToken)
    localStorage.setItem('memberId', user.memberId)
    localStorage.setItem('profileImageUrl', user.profileImageUrl)
    localStorage.setItem('nickname', user.nickname)
    localStorage.setItem('role', user.role)
  }

  function logout() {

    const { resetCalendarState } = useCalendarApi()

    accessToken.value = null
    refreshToken.value = null
    isLoggedIn.value = false
    memberId.value = null
    profileImageUrl.value = null
    nickname.value = null
    role.value = null

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('memberId')
    localStorage.removeItem('profileImageUrl')
    localStorage.removeItem('nickname')
    localStorage.removeItem('role')

    resetCalendarState()
  }

  return {
    accessToken,
    refreshToken,
    isLoggedIn,
    memberId,
    profileImageUrl,
    nickname,
    role,
    setUser,
    logout,
    setProfileImageUrl,
  }
})
