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

const publicRoutes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { layout: 'default' }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { layout: 'default' }
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignUpView,
    meta: { layout: 'default' }
  },
  {
    path: '/oauth2/redirect',
    name: 'oauth2Redirect',
    component: OAuth2RedirectHandler,
    meta: { layout: 'default' }
  },
  {
    path: '/mypage',
    name: 'mypage',
    component: MyPageView,
    meta: { layout: 'default' }
  },
  {
    path: '/faq',
    name: 'faq',
    component: FaqView,
    meta: { layout: 'default' }
  },
  {
    path: '/notice',
    name: 'noticeList',
    component: NoticeListView,
    meta: { layout: 'default' }
  },
  {
    path: '/notice/:id',
    name: 'noticeDetail',
    component: NoticeDetailView,
    meta: { layout: 'default' }
  },
  {
    path: '/calendar',
    name: 'calendar',
    component: CalendarView,
    meta: { layout: 'default' }
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

export default router
