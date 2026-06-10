<template>
  <div v-show="isExpanded" class="toolbar">
    <button
      v-for="tool in clickTools"
      :key="tool.event"
      class="tool-button"
      type="button"
      @click="emit(tool.event)"
    >
      <el-icon :size="22">
        <component :is="tool.icon" />
      </el-icon>
      <span>{{ tool.label }}</span>
    </button>

    <button
      class="tool-button"
      type="button"
      @mousedown="emit('startRecord')"
      @mouseup="emit('stopRecord')"
      @mouseleave="emit('cancelRecord')"
      @touchstart.prevent="emit('startRecord')"
      @touchend.prevent="emit('stopRecord')"
      @touchcancel.prevent="emit('cancelRecord')"
    >
      <el-icon :size="22">
        <Microphone />
      </el-icon>
      <span>语音</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import {
  ChatDotRound,
  Microphone,
  Phone,
  Picture,
  VideoCamera
} from '@element-plus/icons-vue'

type ClickEvent = 'openImageUpload' | 'startVoiceCall' | 'startVideoCall' | 'openEmojiPicker'

defineProps<{
  isExpanded: boolean
}>()

const emit = defineEmits<{
  (e: 'openImageUpload'): void
  (e: 'startRecord'): void
  (e: 'stopRecord'): void
  (e: 'cancelRecord'): void
  (e: 'startVoiceCall'): void
  (e: 'startVideoCall'): void
  (e: 'openEmojiPicker'): void
}>()

const clickTools: Array<{ label: string; icon: unknown; event: ClickEvent }> = [
  { label: '图片', icon: Picture, event: 'openImageUpload' },
  { label: '语音通话', icon: Phone, event: 'startVoiceCall' },
  { label: '视频通话', icon: VideoCamera, event: 'startVideoCall' },
  { label: '表情', icon: ChatDotRound, event: 'openEmojiPicker' }
]
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 4px;
  padding: 8px 14px;
  border-top: 1px solid var(--border-color);
}

.tool-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  min-width: 60px;
  padding: 8px 14px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--text-regular);
  font-size: 11px;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s, transform 0.15s;
}

.tool-button:hover {
  color: var(--color-primary);
  background: #f5f6ff;
}

.tool-button:active {
  transform: scale(0.95);
}
</style>
