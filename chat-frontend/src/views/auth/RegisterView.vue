<template>
  <div class="register-page">
    <!-- 左侧品牌区（与登录页一致） -->
    <div class="register-left">
      <div class="left-content">
        <div class="brand">
          <h2>加入闪聊</h2>
          <p class="brand-desc">创建账号，开启即时通讯之旅</p>
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
        </div>

        <div class="geo-deco">
          <div class="geo geo-1" />
          <div class="geo geo-2" />
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="register-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h3>注册账号</h3>
          <p>填写信息，开始你的聊天之旅</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large"
          @keyup.enter="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名（4-20位）" :prefix-icon="User" v-ripple />
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称" :prefix-icon="UserFilled" v-ripple />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（6-20位）"
              :prefix-icon="Lock" show-password v-ripple />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码"
              :prefix-icon="Lock" show-password v-ripple />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading"
              @click="handleRegister" class="register-btn" v-ripple>
              注 册
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span>已有账号？</span>
            <el-link type="primary" @click="goToLogin">立即登录</el-link>
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
import { User, UserFilled, Lock, ChatDotRound } from '@element-plus/icons-vue'
import { registerApi } from '@/api/user'
import ThemeToggle from '@/components/common/ThemeToggle.vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '', nickname: '', password: '', confirmPassword: ''
})

const validateConfirm = (_rule: any, value: string, callback: any) => {
  callback(value !== form.password ? new Error('两次输入的密码不一致') : undefined)
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在4-20个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  loading.value = true
  try {
    await registerApi({ username: form.username, password: form.password, nickname: form.nickname })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goToLogin = () => router.push('/login')
</script>

<style scoped>
.register-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.register-left {
  width: 42%;
  background: linear-gradient(160deg, #4a3fd8 0%, #6c5ce7 40%, #8b74f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.left-content { position: relative; z-index: 2; color: white; padding: 60px; max-width: 460px; }
.brand { margin-bottom: 40px; }
.brand h2 { margin: 0 0 8px; font-size: 32px; font-weight: 800; letter-spacing: 1px; }
.brand-desc { margin: 0; font-size: 14px; opacity: 0.7; letter-spacing: 2px; }

.features { display: flex; flex-direction: column; gap: 20px; }
.feature-item { display: flex; align-items: flex-start; gap: 14px; }
.feature-icon {
  width: 38px; height: 38px; border-radius: 10px;
  background: rgba(255, 255, 255, 0.15); display: flex;
  align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0;
}
.feature-text { display: flex; flex-direction: column; gap: 2px; }
.feature-text strong { font-size: 15px; font-weight: 600; }
.feature-text span { font-size: 12px; opacity: 0.7; line-height: 1.5; }

.geo-deco { position: absolute; inset: 0; pointer-events: none; }
.geo { position: absolute; border-radius: 50%; }
.geo-1 {
  width: 250px; height: 250px; background: rgba(255,255,255,0.03);
  top: -60px; right: -60px;
  animation: geoFloat 10s ease-in-out infinite;
}
.geo-2 {
  width: 160px; height: 160px; border: 2px solid rgba(255,255,255,0.08);
  bottom: 15%; left: -30px;
  animation: geoFloat 8s ease-in-out infinite reverse;
}
@keyframes geoFloat {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(20px, -15px); }
  66% { transform: translate(-10px, 10px); }
}

.register-right {
  flex: 1; display: flex; align-items: center; justify-content: center;
  background: var(--bg-color); position: relative;
}

.form-wrapper { width: 400px; animation: formSlideIn 0.5s ease; }
@keyframes formSlideIn {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

.form-header { margin-bottom: 32px; }
.form-header h3 { margin: 0 0 6px; font-size: 24px; font-weight: 700; color: var(--text-primary); }
.form-header p { margin: 0; font-size: 14px; color: var(--text-secondary); }

.form-wrapper :deep(.el-form-item) { margin-bottom: 20px; }
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

.register-btn {
  width: 100%; height: 46px; border-radius: 12px !important;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark)) !important;
  font-size: 16px !important; font-weight: 600 !important; letter-spacing: 2px !important;
}
.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(108, 92, 231, 0.3) !important;
}

.form-footer { text-align: center; margin-top: 16px; font-size: 14px; color: var(--text-regular); }
.theme-switch-wrap { position: absolute; top: 24px; right: 24px; }

@media (max-width: 768px) {
  .register-left { display: none; }
  .form-wrapper { width: 90%; max-width: 400px; }
}
</style>
