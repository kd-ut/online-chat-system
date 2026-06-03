<template>
  <div class="message-area">
    <div class="message-input">
      <el-input v-model="content" type="textarea" :rows="2" placeholder="请输入消息..." @keydown="handleKeydown" />
      <div class="input-actions">
        <el-button type="primary" @click="handleSend">发送</el-button>
      </div>
    </div>
    <CommunicationBar :current-chat-user-id="currentChatUserId" @send-image="(url) => $emit('sendImage', url)"
      @send-voice="(url, duration) => $emit('sendVoice', url, duration)" @send-emoji="(url) => $emit('sendEmoji', url)"
      @start-voice-call="(id) => $emit('startVoiceCall', id)" @start-video-call="(id) => $emit('startVideoCall', id)" />
  </div>
</template>

<script setup lang="ts">
/** 消息输入组件，提供文本输入和通信工具栏 @component */
import { ref } from 'vue'
import CommunicationBar from '@/components/common/CommunicationBar.vue'

/** 组件属性：当前聊天用户 ID */
defineProps<{
  currentChatUserId?: number
}>()

/** 组件事件：发送文本/图片/语音/表情消息、发起通话 */
const emit = defineEmits<{
  (e: 'send', content: string): void
  (e: 'sendImage', url: string): void
  (e: 'sendVoice', url: string, duration: number): void
  (e: 'sendEmoji', url: string): void
  (e: 'startVoiceCall', toUserId: number): void
  (e: 'startVideoCall', toUserId: number): void
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

/** 发送文本消息 @returns void */
const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', content.value)
  content.value = ''
}
</script>

<style scoped>
.message-area {
  border-top: 1px solid var(--border-color-lighter);
  background: var(--bg-color-white);
  flex-shrink: 0;
}

.message-input {
  padding: 14px 16px;
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
