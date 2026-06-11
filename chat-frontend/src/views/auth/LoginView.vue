<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="login-left">
      <div class="left-content">
        <div class="brand">
          <svg class="brand-logo" viewBox="0 0 48 48" fill="none">
            <circle cx="24" cy="24" r="22" stroke="rgba(255,255,255,0.3)" stroke-width="1.5" />
            <circle cx="24" cy="24" r="15" stroke="rgba(255,255,255,0.15)" stroke-width="1" stroke-dasharray="6 4" />
            <path d="M16 22 Q24 36 32 22" stroke="white" stroke-width="2.2" stroke-linecap="round" fill="none" opacity="0.9" />
            <circle cx="20" cy="20" r="2.5" fill="white" opacity="0.8" />
            <circle cx="28" cy="20" r="2.5" fill="white" opacity="0.8" />
          </svg>
          <h2>闪聊 FlashChat</h2>
          <p class="brand-desc">安全 · 快速 · 实时沟通</p>
        </div>

        <div class="features">
          <div class="feature-item">
            <div class="feature-icon"><el-icon><ChatDotRound /></el-icon></div>
            <div class="feature-text">
              <strong>即时消息</strong>
              <span>文字、图片、语音、表情，多种方式畅快交流</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><UserFilled /></el-icon></div>
            <div class="feature-text">
              <strong>群组聊天</strong>
              <span>创建群聊，多人协作沟通更高效</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><PhoneFilled /></el-icon></div>
            <div class="feature-text">
              <strong>语音视频通话</strong>
              <span>端到端加密，清晰流畅的实时通话体验</span>
            </div>
          </div>
        </div>

        <!-- 几何装饰 -->
        <div class="geo-deco">
          <div class="geo geo-1" />
          <div class="geo geo-2" />
          <div class="geo geo-3" />
          <div class="geo-dot dot-1" />
          <div class="geo-dot dot-2" />
          <div class="geo-dot dot-3" />
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h3>欢迎回来</h3>
          <p>登录你的账号，开始畅聊吧</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large"
          @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" v-ripple />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码"
              :prefix-icon="Lock" show-password v-ripple />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading"
              @click="handleLogin" class="login-btn" v-ripple>
              登 录
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
          </div>
        </el-form>
      </div>

      <div class="theme-switch-wrap">
        <ThemeToggle />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ChatDotRound, UserFilled, PhoneFilled } from '@element-plus/icons-vue'
import { loginApi } from '@/api/user'
import { useUserStore } from '@/stores/userStore'
import ThemeToggle from '@/components/common/ThemeToggle.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  loading.value = true
  try {
    const res = await loginApi(form)
    userStore.setToken(res.token)
    userStore.setUserInfo(res.user)
    ElMessage.success('登录成功')
    await router.push('/')
  } catch (error: any) {
    ElMessage.error(error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => router.push('/register')
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.login-left {
  width: 46%;
  background: linear-gradient(160deg, #4a3fd8 0%, #6c5ce7 40%, #8b74f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.left-content {
  position: relative;
  z-index: 2;
  color: white;
  padding: 60px;
  max-width: 460px;
}

.brand {
  margin-bottom: 48px;
}

.brand-logo {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
}

.brand h2 {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 1px;
}

.brand-desc {
  margin: 0;
  font-size: 14px;
  opacity: 0.7;
  letter-spacing: 2px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.feature-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-text strong {
  font-size: 15px;
  font-weight: 600;
}

.feature-text span {
  font-size: 12px;
  opacity: 0.7;
  line-height: 1.5;
}

/* 几何装饰 */
.geo-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.geo {
  position: absolute;
  border-radius: 50%;
}

.geo-1 {
  width: 300px; height: 300px;
  background: rgba(255,255,255,0.03);
  top: -80px; right: -80px;
  animation: geoFloat 12s ease-in-out infinite;
}

.geo-2 {
  width: 200px; height: 200px;
  border: 2px solid rgba(255,255,255,0.08);
  bottom: 10%; left: -40px;
  animation: geoFloat 10s ease-in-out infinite reverse;
}

.geo-3 {
  width: 140px; height: 140px;
  background: rgba(255,255,255,0.04);
  bottom: 30%; right: 15%;
  animation: geoFloat 14s ease-in-out infinite 3s;
}

@keyframes geoFloat {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(20px, -15px); }
  66% { transform: translate(-10px, 10px); }
}

.geo-dot {
  position: absolute;
  width: 6px; height: 6px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  animation: dotPulse 3s ease-in-out infinite;
}

.dot-1 { top: 20%; right: 20%; }
.dot-2 { bottom: 35%; left: 15%; animation-delay: 1s; }
.dot-3 { top: 55%; right: 30%; animation-delay: 2s; }

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.5); }
}

/* ===== 右侧表单区 ===== */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color);
  position: relative;
}

.form-wrapper {
  width: 400px;
  animation: formSlideIn 0.5s ease;
}

@keyframes formSlideIn {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

.form-header {
  margin-bottom: 32px;
}

.form-header h3 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.form-header p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.form-wrapper :deep(.el-form-item) {
  margin-bottom: 22px;
}

.form-wrapper :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
  padding: 6px 14px;
  transition: all 0.2s;
}

.form-wrapper :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary-light-5) inset;
}

.form-wrapper :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-primary-light-5) inset !important;
}

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 12px !important;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark)) !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 2px !important;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(108, 92, 231, 0.3) !important;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--text-regular);
}

.theme-switch-wrap {
  position: absolute;
  top: 24px;
  right: 24px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .login-left {
    display: none;
  }
  .form-wrapper {
    width: 90%;
    max-width: 400px;
  }
}
</style>
