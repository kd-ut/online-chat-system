<template>
  <div class="toolbar" v-show="isExpanded">
    <div class="tool-item" @click="$emit('openImageUpload')">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
      <span>图片</span>
    </div>
    <div class="tool-item" @mousedown="$emit('startRecord')" @mouseup="$emit('stopRecord')"
      @mouseleave="$emit('cancelRecord')">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
      <span>语音</span>
    </div>
    <div class="tool-item" @click="$emit('startVoiceCall')">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
      <span>语音通话</span>
    </div>
    <div class="tool-item" @click="$emit('startVideoCall')">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
      <span>视频通话</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 工具栏组件，展示图片/语音/通话功能入口 @component */

/** 组件属性：是否展开 */
defineProps<{
  isExpanded: boolean
}>()

/** 组件事件：各功能按钮的点击回调 */
defineEmits<{
  (e: 'openImageUpload'): void
  (e: 'startRecord'): void
  (e: 'stopRecord'): void
  (e: 'cancelRecord'): void
  (e: 'startVoiceCall'): void
  (e: 'startVideoCall'): void
  (e: 'openEmojiPicker'): void
}>()
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 4px;
  padding: 8px 14px;
  border-top: 1px solid var(--border-color);
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  color: var(--text-regular);
  padding: 8px 14px;
  border-radius: 8px;
  transition: all 0.15s;
  font-size: 11px;
  min-width: 60px;
}

.tool-item:hover {
  color: var(--color-primary);
  background: #f5f6ff;
}

.tool-item:active {
  transform: scale(0.95);
}
</style>
