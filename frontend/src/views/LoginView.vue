<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'

const router = useRouter()
const route = useRoute()
const { login, state, clearError } = useAuth()

const username = ref('')
const password = ref('')
const submitting = ref(false)

const redirect = route.query.redirect || '/dashboard'

async function handleSubmit() {
  if (!username.value.trim() || !password.value.trim()) return
  submitting.value = true
  clearError()
  try {
    await login(username.value.trim(), password.value)
    router.replace(redirect)
  } catch {
    // error is set in state.error by useAuth
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="auth-brand">
        <div class="auth-brand-content">
          <div class="auth-logo">🍽️</div>
          <h2 class="auth-app-name">暖光食刻</h2>
          <p class="auth-tagline">温暖每一餐</p>
        </div>
      </div>

      <div class="auth-form-area">
        <div class="auth-form-card">
          <h3 class="auth-title">管理员登录</h3>

          <form class="auth-form" @submit.prevent="handleSubmit">
            <div class="auth-field">
              <label for="login-username">用户名</label>
              <input
                id="login-username"
                v-model="username"
                type="text"
                autocomplete="username"
                placeholder="请输入用户名"
                :disabled="submitting"
              />
            </div>

            <div class="auth-field">
              <label for="login-password">密码</label>
              <input
                id="login-password"
                v-model="password"
                type="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                :disabled="submitting"
              />
            </div>

            <p v-if="state.error" class="auth-error">{{ state.error }}</p>

            <button
              type="submit"
              class="auth-submit"
              :disabled="submitting || !username.trim() || !password.trim()"
            >
              {{ submitting ? '登录中...' : '登录' }}
            </button>
          </form>

          <p class="auth-switch">
            还没有账号？<router-link to="/register" class="auth-link">立即注册</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(223,243,239,0.65), transparent 32%),
              linear-gradient(180deg, #fbfcfc 0%, #f6f8f7 100%);
}

.auth-panel {
  display: flex;
  width: 780px;
  min-height: 460px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 16px 38px rgba(28,44,52,0.08);
}

.auth-brand {
  flex: 1;
  background: linear-gradient(160deg, #168f7f 0%, #0d6b5e 60%, #0a4a3f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-brand-content {
  text-align: center;
  color: #fff;
}

.auth-logo {
  font-size: 48px;
  margin-bottom: 12px;
}

.auth-app-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
}

.auth-tagline {
  font-size: 13px;
  opacity: 0.75;
  margin: 0;
}

.auth-form-area {
  flex: 1.2;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}

.auth-form-card {
  width: 100%;
  max-width: 320px;
}

.auth-title {
  font-size: 18px;
  font-weight: 600;
  color: #202832;
  margin: 0 0 28px 0;
}

.auth-field {
  margin-bottom: 16px;
}

.auth-field label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #202832;
  margin-bottom: 6px;
}

.auth-field input {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #dde5e2;
  border-radius: 8px;
  background: #f9fbfa;
  color: #202832;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
  box-sizing: border-box;
}

.auth-field input:focus {
  border-color: #168f7f;
  box-shadow: 0 0 0 3px rgba(22,143,127,0.12);
}

.auth-field input:disabled {
  opacity: 0.6;
}

.auth-error {
  color: #d95858;
  font-size: 13px;
  margin: 0 0 16px 0;
}

.auth-submit {
  width: 100%;
  padding: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: #168f7f;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.18s ease, box-shadow 0.18s ease;
  box-shadow: 0 12px 26px rgba(27,138,127,0.22);
}

.auth-submit:hover:not(:disabled) {
  background: #147a6e;
}

.auth-submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.auth-switch {
  text-align: center;
  font-size: 13px;
  color: #6d7a7a;
  margin: 18px 0 0 0;
}

.auth-link {
  color: #168f7f;
  text-decoration: none;
  font-weight: 500;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
