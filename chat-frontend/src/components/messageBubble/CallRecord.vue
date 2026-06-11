<template>
  <div class="call-record">
    <el-icon class="call-icon" :size="16">
      <Phone v-if="callType === 'voice'" />
      <VideoCamera v-else />
    </el-icon>
    <span class="call-text">{{ label }}</span>
    <span class="call-duration" v-if="formattedDuration">{{ formattedDuration }}</span>
  </div>
</template>

<script setup lang="ts">
/** 通话记录组件（语音/视频），显示通话图标和时长 @component */
import { computed } from 'vue'
import { Phone, VideoCamera } from '@element-plus/icons-vue'

const props = defineProps<{
  callType: 'voice' | 'video'
  duration: string
}>()

const label = computed(() => props.callType === 'voice' ? '语音通话' : '视频通话')

const formattedDuration = computed(() => {
  const sec = parseInt(props.duration, 10)
  if (isNaN(sec) || sec <= 0) return ''
  const minutes = Math.floor(sec / 60)
  const seconds = sec % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})
</script>

<style scoped>
.call-record {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.call-icon {
  opacity: 0.7;
}

.call-text {
  opacity: 0.85;
}

.call-duration {
  opacity: 0.6;
  font-size: 12px;
  margin-left: 2px;
}
</style>
