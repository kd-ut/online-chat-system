<template>
  <div class="message-area">
    <div class="input-row">
      <div class="textarea-wrap">
        <textarea
          v-model="content"
          class="chat-textarea"
          rows="1"
          :disabled="muted"
          :placeholder="muted ? '你已被禁言' : '请输入群消息...'"
          @keydown="handleKeydown"
          @input="autoResize"
          ref="textareaRef"
        ></textarea>
      </div>
      <el-tag v-if="muted" type="danger" effect="dark" size="small" class="muted-tag">禁言</el-tag>
      <button v-else class="send-btn" @click="handleSend">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 群聊消息输入组件，WeChat 风格布局，支持禁言状态 @component */
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

/** 发送消息 */
const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', content.value)
  content.value = ''
  const el = textareaRef.value
  if (el) { el.style.height = 'auto' }
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

.chat-textarea:disabled {
  background: #fef0f0;
  color: var(--text-secondary);
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

.muted-tag {
  flex-shrink: 0;
  margin-bottom: 4px;
}
</style>
