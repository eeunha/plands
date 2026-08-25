import axios from 'axios';
import { useAuthStore } from '@/stores/authStore.js'

// axios 인스턴스 생성
const api = axios.create({
  baseURL: 'http://localhost:8081',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false,
});

// 요청 인터셉터: accessToken과 refreshToken을 헤더에 추가
api.interceptors.request.use(
  (config) => {
    const publicEndpoints = ['/api/auth/signup', '/api/auth/login'];
    const isPublicEndpoint = publicEndpoints.some(endpoint => config.url === endpoint);

    if (!isPublicEndpoint) {
      const accessToken = localStorage.getItem('accessToken');
      const refreshToken = localStorage.getItem('refreshToken');

      if (accessToken) {
        config.headers['Authorization'] = `Bearer ${accessToken}`;
        console.log('🚀 요청에 AccessToken 추가');
      }

      if (refreshToken) {
        config.headers['Refresh-Token'] = refreshToken;
        console.log('🚀 요청에 RefreshToken 추가');
      }
    }

    console.log('📤 요청 URL:', config.url);
    console.log('📤 요청 헤더:', config.headers);

    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터: 필요한 경우에만 토큰 저장 및 401 처리
api.interceptors.response.use(
  (response) => {
    const url = response.config.url;

    // 로그인 또는 토큰 재발급 요청에만 토큰 저장
    const isAuthEndpoint = url.includes('/api/auth/login') || url.includes('/api/auth/refresh-token');

    if (isAuthEndpoint) {
      if (response.data?.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken);
        console.log('✅ AccessToken 저장됨 (로그인 또는 재발급)');
      }
      if (response.data?.refreshToken) {
        localStorage.setItem('refreshToken', response.data.refreshToken);
        console.log('🔄 RefreshToken 저장됨 (로그인 또는 재발급)');
      }
    }

    // 응답 헤더에 Authorization이 있다면 AccessToken 업데이트
    const authHeader = response.headers['authorization'] || response.headers['Authorization'];
    if (authHeader?.startsWith('Bearer ')) {
      const token = authHeader.replace('Bearer ', '');
      localStorage.setItem('accessToken', token);
      console.log('✅ 새로운 AccessToken 저장됨 (응답 헤더)');
    }

    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    console.log('❌ 에러 발생:', error.response?.status, error.response?.data);

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      console.log('🔄 토큰 재발급 시도...');

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        console.error('❌ RefreshToken 없음. 로그아웃 처리');
        handleLogout();
        return Promise.reject(error);
      }

      try {
        const refreshResponse = await axios.post(
          'http://localhost:8081/api/auth/refresh-token',
          {},
          {
            headers: {
              'Content-Type': 'application/json',
              'Refresh-Token': refreshToken,
            },
          }
        );

        console.log('🔄 토큰 재발급 응답:', refreshResponse.data);

        if (refreshResponse.data.accessToken) {
          localStorage.setItem('accessToken', refreshResponse.data.accessToken);
          console.log('🔑 새로운 AccessToken 저장됨');
        }
        if (refreshResponse.data.refreshToken) {
          localStorage.setItem('refreshToken', refreshResponse.data.refreshToken);
          console.log('🔑 새로운 RefreshToken 저장됨');
        }

        originalRequest.headers['Authorization'] = `Bearer ${refreshResponse.data.accessToken}`;
        originalRequest.headers['Refresh-Token'] = refreshResponse.data.refreshToken;

        console.log('🔄 원본 요청 재시도');
        return api(originalRequest);
      } catch (refreshError) {
        console.error('❌ RefreshToken으로 갱신 실패:', refreshError);
        handleLogout();
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// 로그아웃 처리
function handleLogout() {
  const authStore = useAuthStore()
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  authStore.logout() // Pinia 등에서 사용자 상태를 false로 변경
  console.log('🚪 로그아웃 완료')
}

export default api;
