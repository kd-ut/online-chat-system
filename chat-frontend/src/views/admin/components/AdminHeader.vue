<template>
  <div class="admin-header">
    <div class="header-left">
      <el-button :icon="ArrowLeft" class="back-btn" @click="goBack">返回聊天</el-button>
      <h2 class="header-title">{{ title }}</h2>
    </div>
    <div class="header-right">
      <ThemeToggle />
      <span class="header-time">{{ currentTime }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 管理后台头部组件 @component */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import ThemeToggle from '@/components/common/ThemeToggle.vue'

defineProps<{ title: string }>()

const router = useRouter()

/** 当前时间 */
const currentTime = ref('')
let timer: ReturnType<typeof setInterval> | null = null

const goBack = () => {
  router.push('/')
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  })
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 60000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: var(--header-height);
  background: var(--bg-color-white);
  border-bottom: 1px solid var(--border-color-light);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.back-btn {
  border-radius: 10px !important;
  font-weight: 600 !important;
  height: 38px;
  font-size: 14px !important;
  padding: 0 20px !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-time {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}
</style>
