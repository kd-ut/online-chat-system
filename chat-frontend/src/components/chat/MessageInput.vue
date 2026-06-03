<template>
  <div class="message-area">
    <CommunicationBar ref="commBarRef" :current-chat-user-id="currentChatUserId" @send-image="(url) => $emit('sendImage', url)"
      @send-voice="(url, duration) => $emit('sendVoice', url, duration)" @send-emoji="(url) => $emit('sendEmoji', url)"
      @start-voice-call="(id) => $emit('startVoiceCall', id)" @start-video-call="(id) => $emit('startVideoCall', id)" />

    <div class="input-row">
      <div class="left-btns">
        <button class="icon-btn emoji-btn" title="表情" @click="openEmoji">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
        </button>
        <button class="icon-btn toggle-btn" :class="{ active: commExpanded }" title="更多" @click="toggleBar">
          <svg viewBox="0 0 24 24" width="22" height="22" :class="{ rotated: commExpanded }" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
        </button>
      </div>

      <div class="textarea-wrap">
        <textarea
          v-model="content"
          class="chat-textarea"
          rows="1"
          placeholder="请输入消息..."
          @keydown="handleKeydown"
          @input="autoResize"
          ref="textareaRef"
        ></textarea>
      </div>

      <button
        class="icon-btn voice-btn"
        :class="{ recording: isRecording }"
        title="按住说话"
        @mousedown="startVoice"
        @mouseup="stopVoice"
        @mouseleave="cancelVoice"
        @touchstart.prevent="startVoice"
        @touchend.prevent="stopVoice"
        @touchcancel.prevent="cancelVoice"
      >
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
      </button>

      <button class="send-btn" @click="handleSend">发送</button>
    </div>
    <div v-if="isRecording" class="recording-bar">松开发送 · 滑动取消</div>
  </div>
</template>

<script setup lang="ts">
/** 消息输入组件，WeChat 风格布局：左侧表情/更多按钮 + 中间输入框 + 右侧发送按钮 @component */
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
/** 通信栏组件引用 */
const commBarRef = ref<InstanceType<typeof CommunicationBar>>()
/** 通信栏展开状态 */
const commExpanded = ref(false)
/** 是否正在录音 */
const isRecording = ref(false)
/** 文本域元素引用 */
const textareaRef = ref<HTMLTextAreaElement>()

/** 键盘事件处理：Enter 发送，Shift+Enter 换行 */
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

/** 自动调整文本域高度 */
const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

/** 发送文本消息 */
const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', content.value)
  content.value = ''
  const el = textareaRef.value
  if (el) { el.style.height = 'auto' }
}

/** 打开表情选择器 */
const openEmoji = () => {
  commBarRef.value?.openEmojiPicker()
}

/** 切换更多功能栏 */
const toggleBar = () => {
  commBarRef.value?.toggleExpand()
  commExpanded.value = !commExpanded.value
}

/** 开始录音 */
const startVoice = () => {
  commBarRef.value?.startRecord()
  isRecording.value = true
}

/** 停止录音并发送 */
const stopVoice = () => {
  if (!isRecording.value) return
  commBarRef.value?.stopRecord()
  isRecording.value = false
}

/** 取消录音 */
const cancelVoice = () => {
  if (!isRecording.value) return
  commBarRef.value?.cancelRecord()
  isRecording.value = false
}
</script>

<style scoped>
.message-area {
  border-top: 1px solid var(--border-color);
  background: var(--bg-color-white);
  flex-shrink: 0;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 14px;
}

.left-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  padding-bottom: 4px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-regular);
  transition: all 0.15s;
}

.icon-btn:hover {
  background: var(--bg-color);
  color: var(--color-primary);
}

.icon-btn.active {
  color: var(--color-primary);
}

.icon-btn svg.rotated {
  transform: rotate(45deg);
  transition: transform 0.2s;
}

.textarea-wrap {
  flex: 1;
  min-width: 0;
}

.chat-textarea {
  width: 100%;
  border: none;
  outline: none;
  background: var(--bg-color);
  border-radius: 6px;
  padding: 10px 12px;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  font-family: inherit;
  transition: background 0.15s;
  max-height: 120px;
}

.chat-textarea:focus {
  background: #eef0ff;
}

.chat-textarea::placeholder {
  color: var(--text-placeholder);
}

.send-btn {
  flex-shrink: 0;
  padding: 8px 22px;
  background: #07c160;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  margin-bottom: 1px;
}

.send-btn:hover {
  background: #06ad56;
}

.send-btn:active {
  transform: scale(0.96);
}

.voice-btn.recording {
  color: var(--color-danger);
  background: #fef2f2;
}

.recording-bar {
  text-align: center;
  padding: 4px 0 10px;
  font-size: 11px;
  color: var(--color-danger);
  background: #fef2f2;
}
</style>
