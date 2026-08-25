import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore.js'

import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import SignUpView from '@/views/SignUpView.vue'
import OAuth2RedirectHandler from '@/views/OAuth2RedirectHandler.vue'
import MyPageView from '@/views/MyPageView.vue'
import FaqView from '@/views/FaqView.vue'
import NoticeListView from '@/views/NoticeListView.vue'
import NoticeDetailView from '@/views/NoticeDetailView.vue'
import AdminDashboard from '@/views/admin/AdminDashboard.vue'
import CalendarView from '@/views/CalendarView.vue'
import PlantView from '@/views/PlantView.vue'
import CommunityView from '@/views/CommunityView.vue'

const publicRoutes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { layout: 'default' },
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { layout: 'default' },
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignUpView,
    meta: { layout: 'default' },
  },
  {
    path: '/oauth2/redirect',
    name: 'oauth2Redirect',
    component: OAuth2RedirectHandler,
    meta: { layout: 'default' },
  },
  {
    path: '/mypage',
    name: 'mypage',
    component: MyPageView,
    meta: { layout: 'default', requiresAuth: true },
  },
  {
    path: '/faq',
    name: 'faq',
    component: FaqView,
    meta: { layout: 'default' },
  },
  {
    path: '/notice',
    name: 'noticeList',
    component: NoticeListView,
    meta: { layout: 'default' },
  },
  {
    path: '/notice/:id',
    name: 'noticeDetail',
    component: NoticeDetailView,
    meta: { layout: 'default' },
  },
  {
    path: '/calendar',
    name: 'calendar',
    component: CalendarView,
    meta: { layout: 'default', requiresAuth: true },
  },
  {
    path: '/community',
    name: 'community',
    component: CommunityView,
    meta: { layout: 'default' },
  },
  {
    path: '/plant',
    name: 'plant',
    component: PlantView,
    meta: { layout: 'default' },
  },
]

const adminRoutes = {
  path: '/admin',
  meta: { layout: 'admin' },
  beforeEnter: (to, from, next) => {
    const authStore = useAuthStore()
    if (authStore && authStore.role === 'ROLE_ADMIN') {
      next()
    } else {
      next('/')
    }
  },
  children: [
    {
      path: '',
      name: 'adminDashboard',
      component: AdminDashboard,
      meta: { layout: 'admin' }
    }
  ]
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...publicRoutes,
    adminRoutes
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 1. 보호받아야 할 경로인가?
  const requiresAuth = to.meta.requiresAuth

  // 2. 로그인이 필요한데 토큰이 없는가?
  if (requiresAuth && !authStore.isLoggedIn) {
    alert('로그인이 필요한 서비스입니다.')
    next({ name: 'login' }) // 여기서만 로그인으로 보냄
  } else {
    next()
  }
})

export default router
