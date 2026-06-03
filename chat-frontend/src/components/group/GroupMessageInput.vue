<template>
  <div class="message-input">
    <el-input v-model="content" type="textarea" :rows="3"
      :disabled="muted"
      :placeholder="muted ? '你已被禁言' : '请输入群消息...'"
      @keydown="handleKeydown" />
    <div class="input-actions">
      <el-tag v-if="muted" type="danger" effect="dark" size="small">你已被禁言</el-tag>
      <el-button v-else type="primary" @click="handleSend">发送 (Enter)</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 群聊消息输入组件，支持禁言状态 @component */
import { ref } from 'vue'

/** 组件属性：是否被禁言 */
defineProps<{
  muted?: boolean
}>()

/** 组件事件：发送消息 */
const emit = defineEmits<{
  (e: 'send', content: string): void
}>()

/** 输入框内容 */
const content = ref('')

/** 键盘事件处理：Enter 发送，Shift+Enter 换行 @param e 键盘事件 */
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

/** 发送消息 @returns void */
const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', content.value)
  content.value = ''
}
</script>

<style scoped>
.message-input {
  padding: 14px 16px;
  border-top: 1px solid var(--border-color-lighter);
  background: var(--bg-color-white);
}

.message-input :deep(.el-textarea__inner) {
  border: 2px solid transparent;
  outline: none;
  background-color: #f5f5f5;
  border-radius: 14px !important;
  transition: all 0.4s;
  padding: 12px 16px;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
}

.message-input :deep(.el-textarea__inner:hover) {
  border: 2px solid var(--color-primary-light);
  background-color: white;
}

.message-input :deep(.el-textarea__inner:focus) {
  border: 2px solid var(--color-primary);
  box-shadow: 0 0 0 4px rgba(108, 92, 231, 0.15);
  background-color: white;
}

.input-actions {
  margin-top: 10px;
  text-align: right;
}

.input-actions .el-button {
  border-radius: 12px !important;
  padding: 8px 24px !important;
  font-weight: 600 !important;
}
</style>
