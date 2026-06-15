<template>
  <div class="stats-cards">
    <div v-for="item in statItems" :key="item.key" class="stat-card" :style="{ '--card-color': item.color }">
      <div class="card-icon">
        <el-icon :size="22"><component :is="item.icon" /></el-icon>
      </div>
      <div class="card-content">
        <div class="stat-value">{{ item.animated ? animatedValues[item.key] : item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </div>
      <div class="card-decoration" />
    </div>
  </div>
</template>

<script setup lang="ts">
/** 数据统计卡片组件 — 带数字滚动动画和趋势指示 @component */
import { ref, computed, onMounted, watch } from 'vue'
import { User, DataLine, ChatLineSquare, Monitor } from '@element-plus/icons-vue'
import type { StatisticsVO } from '@/api/admin'

const props = defineProps<{ stats: StatisticsVO }>()

/** 卡片的动画值 (从 0 滚动到目标值) */
const animatedValues = ref<Record<string, number>>({ totalUsers: 0, todayActiveUsers: 0, todayMessages: 0, onlineUsers: 0 })

/** 卡片定义 */
const statItems = computed(() => [
  { key: 'totalUsers', label: '总用户数', value: props.stats.totalUsers, icon: User, color: '#6c5ce7', animated: true },
  { key: 'todayActiveUsers', label: '今日活跃', value: props.stats.todayActiveUsers, icon: DataLine, color: '#13deb9', animated: true },
  { key: 'todayMessages', label: '今日消息', value: props.stats.todayMessages, icon: ChatLineSquare, color: '#ffae1f', animated: true },
  { key: 'onlineUsers', label: '在线人数', value: props.stats.onlineUsers, icon: Monitor, color: '#fa896b', animated: true }
])

/** 数字滚动动画 */
function animateValue(key: string, target: number, duration = 800) {
  const start = animatedValues.value[key] || 0
  const range = target - start
  if (range === 0) return
  const startTime = performance.now()

  function step(now: number) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    // easeOutCubic
    const eased = 1 - Math.pow(1 - progress, 3)
    animatedValues.value[key] = Math.round(start + range * eased)

    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }

  requestAnimationFrame(step)
}

/** 监听 stats 变化时触发动画 */
watch(() => props.stats, (newStats) => {
  if (!newStats) return
  animateValue('totalUsers', newStats.totalUsers)
  animateValue('todayActiveUsers', newStats.todayActiveUsers)
  animateValue('todayMessages', newStats.todayMessages)
  animateValue('onlineUsers', newStats.onlineUsers)
}, { immediate: true })

onMounted(() => {
  if (props.stats) {
    animateValue('totalUsers', props.stats.totalUsers)
    animateValue('todayActiveUsers', props.stats.todayActiveUsers)
    animateValue('todayMessages', props.stats.todayMessages)
    animateValue('onlineUsers', props.stats.onlineUsers)
  }
})
</script>

<style scoped>
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  position: relative;
  background: var(--bg-color-white);
  border-radius: var(--border-radius-large);
  padding: 24px;
  border: 1px solid var(--border-color-light);
  display: flex;
  align-items: center;
  gap: 16px;
  overflow: hidden;
  transition: all 0.3s var(--transition-timing);
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(108, 92, 231, 0.12);
  border-color: var(--card-color);
}

.card-decoration {
  position: absolute;
  top: -24px;
  right: -24px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: var(--card-color);
  opacity: 0.06;
  transition: all 0.3s var(--transition-timing);
}

.stat-card:hover .card-decoration {
  opacity: 0.1;
  transform: scale(1.3);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--card-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
  font-weight: 500;
}
</style>
