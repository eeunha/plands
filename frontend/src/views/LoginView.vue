<template>
  <div class="login-container">
    <div class="login-main">
      <div class="login-form">
        <form @submit.prevent="login">
        <h2>Login</h2>
          <div class="input-group">
            <input type="email" placeholder="이메일" class="input-field" v-model="email"/>
          </div>
          <div class="input-group">
            <input type="password" placeholder="비밀번호" class="input-field" v-model="password"/>
          </div>
        <button type="submit" class="btn-gr-lg">Login</button>
        <p class="signup-link">
          Don't have an account? <router-link to="/signup">Sign Up</router-link>
        </p>
        </form>
        <div class="social-login">
          <p class="social-login text">Or continue with</p>
          <a
            v-for="provider in providers"
            :key="provider.name"
            class="social-icon"
            :class="provider.name"
            :href="getOAuthUrl(provider.name)"
          >
            {{ provider.label }}
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import '@/assets/styles/common.css'
import '@/assets/styles/login.css'
import api from "@/utils/api.js";
import { useAuthStore } from '@/stores/authStore.js';

export default {
  name: 'LoginPage',
  data() {
    return {
      email: '',
      password: '',
      providers: [
        { name: 'google', label: 'Google' },
        { name: 'naver', label: 'Naver' },
        { name: 'kakao', label: 'Kakao' },
      ],
    }
  },
  methods: {
    async login() {
      try {
        const authStore = useAuthStore();
        const response = await api.post('/api/auth/login', {
          email: this.email,
          password: this.password
        });
        const data = response.data;
        const userForSetUser = {
          memberId: data.memberId,
          nickname: data.nickname,
          role: data.role,
          profileImageUrl: data.profileImageUrl,
          accessToken: data.jwtToken.accessToken,
          refreshToken: data.jwtToken.refreshToken,
        };
        authStore.setUser(userForSetUser);
        this.$router.push('/calendar');
      } catch (error) {
        console.error('로그인 실패:', error);
        alert('이메일 또는 비밀번호가 올바르지 않습니다.');
      }
    },
    getOAuthUrl(provider) {
      const redirectUri = 'http://localhost:8082/oauth2/redirect';
      const backendBaseUrl = 'http://localhost:8081';
      return `${backendBaseUrl}/oauth2/authorize/${provider}?redirect_uri=${redirectUri}`;
    }
  }
};
</script>
