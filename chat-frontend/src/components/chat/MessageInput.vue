<template>
  <div class="message-area">
    <CommunicationBar ref="commBarRef" :current-chat-user-id="currentChatUserId" @send-image="(url) => $emit('sendImage', url)"
      @send-voice="(url, duration) => $emit('sendVoice', url, duration)" @send-emoji="(url) => $emit('sendEmoji', url)"
      @start-voice-call="(id) => $emit('startVoiceCall', id)" @start-video-call="(id) => $emit('startVideoCall', id)" />

    <div
      ref="cancelZoneRef"
      v-if="isRecording"
      class="cancel-zone"
      :class="{ active: isInCancelZone }"
      @mousemove="onCancelZoneMove"
      @mouseleave="isInCancelZone = false"
      @mouseup="handleCancelRelease"
      @touchend.prevent="handleCancelRelease"
    >松开取消</div>

    <div class="input-row" @mouseup="onRowMouseUp">
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
        :class="{ recording: isRecording, pressing: isLongPressing }"
        title="按住说话"
        @mousedown="startVoice"
        @mouseup="handleVoiceRelease"
        @touchstart.prevent="startVoice"
        @touchend.prevent="handleVoiceRelease"
        @touchmove.prevent="onTouchMove"
        @touchcancel.prevent="handleVoiceRelease"
      >
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
      </button>

      <button class="send-btn" @click="handleSend">发送</button>
    </div>
    <div v-if="isRecording && !isInCancelZone" class="recording-bar" @mouseup="finishVoice">松开发送</div>
  </div>
</template>

<script setup lang="ts">
/** 消息输入组件，WeChat 风格布局 @component */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import CommunicationBar from '@/components/common/CommunicationBar.vue'

defineProps<{ currentChatUserId?: number }>()

const emit = defineEmits<{
  (e: 'send', content: string): void
  (e: 'sendImage', url: string): void
  (e: 'sendVoice', url: string, duration: number): void
  (e: 'sendEmoji', url: string): void
  (e: 'startVoiceCall', toUserId: number): void
  (e: 'startVideoCall', toUserId: number): void
}>()

const content = ref('')
const commBarRef = ref<InstanceType<typeof CommunicationBar>>()
const commExpanded = ref(false)
const isRecording = ref(false)
const isLongPressing = ref(false)
const longPressTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const LONG_PRESS_THRESHOLD = 300
const textareaRef = ref<HTMLTextAreaElement>()
const cancelZoneRef = ref<HTMLDivElement>()

/** 手指/鼠标是否在取消区域内 */
const isInCancelZone = ref(false)

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault(); handleSend()
  }
}

const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', content.value)
  content.value = ''
  const el = textareaRef.value
  if (el) el.style.height = 'auto'
}

const openEmoji = () => commBarRef.value?.openEmojiPicker()

const toggleBar = () => {
  commBarRef.value?.toggleExpand()
  commExpanded.value = !commExpanded.value
}

/** 长按开始：300ms 后真正开始录音 */
const startVoice = () => {
  if (longPressTimer.value) { clearTimeout(longPressTimer.value); longPressTimer.value = null }
  isLongPressing.value = true
  isInCancelZone.value = false
  longPressTimer.value = setTimeout(() => {
    longPressTimer.value = null
    isLongPressing.value = false
    commBarRef.value?.startRecord()
    isRecording.value = true
  }, LONG_PRESS_THRESHOLD)
}

/** 发送或取消：不在取消区域 → 发送，在取消区域 → 取消 */
const finishVoice = () => {
  if (!isRecording.value) return
  if (isInCancelZone.value) {
    commBarRef.value?.cancelRecord()
  } else {
    commBarRef.value?.stopRecord()
  }
  isRecording.value = false
  isInCancelZone.value = false
}

/** 语音按钮松开 */
const handleVoiceRelease = () => {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
    isLongPressing.value = false
    ElMessage.info('按住说话')
    return
  }
  finishVoice()
}

/** 在取消区域松开 → 取消 */
const handleCancelRelease = () => {
  finishVoice()
}

/** 在输入行/录音条松开（但不在取消区域） → 发送 */
const onRowMouseUp = () => {
  if (!isRecording.value) return
  finishVoice()
}

/** 鼠标在取消区域移动：用坐标判断位置，避免 mouseenter/mouseleave 边界闪烁 */
const onCancelZoneMove = (e: MouseEvent) => {
  if (!isRecording.value || !cancelZoneRef.value) return
  const rect = cancelZoneRef.value.getBoundingClientRect()
  isInCancelZone.value = e.clientX >= rect.left &&
    e.clientX <= rect.right &&
    e.clientY >= rect.top &&
    e.clientY <= rect.bottom
}

/** 触摸移动：检测手指是否进入了取消区域 */
const onTouchMove = (e: TouchEvent) => {
  if (!isRecording.value || !cancelZoneRef.value) return
  const touch = e.touches[0]
  if (!touch) return
  const rect = cancelZoneRef.value.getBoundingClientRect()
  isInCancelZone.value = touch.clientX >= rect.left &&
    touch.clientX <= rect.right &&
    touch.clientY >= rect.top &&
    touch.clientY <= rect.bottom
}
</script>

<style scoped>
.message-area {
  border-top: 1px solid var(--border-color);
  background: var(--bg-color-white);
  flex-shrink: 0;
}

.cancel-zone {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  background: var(--bg-color);
  border-bottom: 1px solid var(--border-color);
  user-select: none;
  transition: all 0.15s;
}

.cancel-zone.active {
  color: #fff;
  background: #e87461;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 14px;
}

.left-btns {
  display: flex; align-items: center; gap: 2px; flex-shrink: 0; padding-bottom: 4px;
}

.icon-btn {
  width: 36px; height: 36px; border: none; background: transparent; border-radius: 6px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  color: var(--text-regular); transition: all 0.15s;
}
.icon-btn:hover { background: var(--bg-color); color: var(--color-primary); }
.icon-btn.active { color: var(--color-primary); }
.icon-btn svg.rotated { transform: rotate(45deg); transition: transform 0.2s; }

.textarea-wrap { flex: 1; min-width: 0; }

.chat-textarea {
  width: 100%; border: none; outline: none; background: var(--bg-color); border-radius: 6px;
  padding: 10px 12px; resize: none; font-size: 14px; line-height: 1.5; font-family: inherit;
  transition: background 0.15s; max-height: 120px;
}
.chat-textarea:focus { background: #eef0ff; }
.chat-textarea::placeholder { color: var(--text-placeholder); }

.send-btn {
  flex-shrink: 0; padding: 8px 22px; background: #07c160; color: white; border: none;
  border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer;
  transition: all 0.15s; margin-bottom: 1px;
}
.send-btn:hover { background: #06ad56; }
.send-btn:active { transform: scale(0.96); }

.voice-btn.recording { color: var(--color-danger); background: #fef2f2; }
.voice-btn.pressing { color: var(--color-primary); background: #eef0ff; }

.recording-bar {
  text-align: center; padding: 4px 0 10px; font-size: 11px;
  color: var(--text-secondary); background: var(--bg-color-white);
}
</style>
