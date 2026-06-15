<template>
  <div class="message-item" :class="{ own: isOwn }">
    <el-avatar :size="40" :src="message.fromUserAvatar || ''">
      {{ message.fromUserNickname?.charAt(0) || 'U' }}
    </el-avatar>
    <div class="message-content">
      <div class="message-info" v-if="showInfo">
        <span class="name">{{ message.fromUserNickname }}</span>
        <span class="time">{{ formatRelativeTime(message.sendTime) }}</span>
      </div>
      <div class="message-bubble" :class="{ recalled: message.isRecalled }"
        @mouseenter="onBubbleEnter" @mouseleave="onBubbleLeave">
        <!-- 被引用消息卡片 -->
        <div v-if="message.repliedMessage && !message.isRecalled" class="replied-card" @click.stop>
          <div class="replied-name">{{ message.repliedMessage.fromUserNickname }}</div>
          <div class="replied-content">{{ message.repliedMessage.content || '消息已撤回' }}</div>
        </div>

        <span v-if="message.messageType === 1 && !message.isRecalled">{{ message.content }}</span>

        <div v-else-if="message.messageType === 2 && !message.isRecalled" class="image-message">
          <el-image :src="message.content" :preview-src-list="[message.content]" fit="cover" class="message-image" />
        </div>

        <div v-else-if="message.messageType === 3 && !message.isRecalled" class="emoji-message">
          <el-image :src="message.content" fit="contain" class="emoji-image" />
        </div>

        <VoiceMessage v-else-if="message.messageType === 4 && !message.isRecalled" :url="message.content"
          :duration="message.duration" />

        <CallRecord v-else-if="(message.messageType === 5 || message.messageType === 6) && !message.isRecalled"
          :call-type="message.messageType === 5 ? 'voice' : 'video'" :duration="message.content" />

        <span v-else-if="message.isRecalled" class="recalled">
          {{ isOwn ? '你撤回了一条消息' : '对方撤回了一条消息' }}
        </span>

        <span v-else>{{ message.content }}</span>

        <!-- 操作按钮组：JS 定时器控制显隐，上下排列 -->
        <div v-show="showActions" class="action-btns" @mouseenter="onActionsEnter" @mouseleave="onActionsLeave">
          <el-button v-if="isOwn && !message.isRecalled && canRecall" class="action-btn recall-btn" text size="small"
            @click.stop="handleRecall">撤回</el-button>
          <el-button v-if="!message.isRecalled" class="action-btn reply-btn" text size="small"
            @click.stop="$emit('reply', message)">回复</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 聊天消息气泡组件，支持文字/图片/语音/撤回消息展示 @component */
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatRelativeTime } from '@/utils/date'
import { recallMessageApi } from '@/api/message'
import CallRecord from '../messageBubble/CallRecord.vue'
import VoiceMessage from './VoiceMessage.vue'

/** 组件属性：消息对象、是否为本人发送、是否显示信息 */
const props = defineProps<{
  message: any
  isOwn: boolean
  showInfo?: boolean
}>()

/** 组件事件：回复消息 */
defineEmits<{
  (e: 'reply', message: any): void
}>()

/** 撤回时限：2 分钟（毫秒） */
const RECALL_LIMIT = 2 * 60 * 1000

/** 是否可撤回：发送时间在 2 分钟内 */
const canRecall = computed(() => {
  if (!props.message.sendTime) return false
  const now = Date.now()
  const sendTime = new Date(props.message.sendTime).getTime()
  return now - sendTime <= RECALL_LIMIT
})

/** 撤回消息处理 @returns Promise<void> */
const handleRecall = async () => {
  if (!canRecall.value) {
    ElMessage.warning('消息发送超过2分钟，无法撤回')
    return
  }
  try {
    await recallMessageApi(props.message.id)
    ElMessage.success('已撤回')
    props.message.isRecalled = true
  } catch {
    ElMessage.error('撤回失败')
  }
}

/** JS 定时器控制按钮显隐：鼠标离开气泡后 500ms 延迟隐藏，避免误触消失 */
const showActions = ref(false)
let hideTimer: ReturnType<typeof setTimeout> | null = null

const clearHideTimer = () => {
  if (hideTimer) { clearTimeout(hideTimer); hideTimer = null }
}

const onBubbleEnter = () => {
  clearHideTimer()
  showActions.value = true
}

const onBubbleLeave = () => {
  hideTimer = setTimeout(() => { showActions.value = false }, 200)
}

const onActionsEnter = () => {
  clearHideTimer()
  showActions.value = true
}

const onActionsLeave = () => {
  hideTimer = setTimeout(() => { showActions.value = false }, 200)
}
</script>

<style scoped>
.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  padding: 0 16px;
  animation: messageIn 0.25s ease;
}

@keyframes messageIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-item.own {
  flex-direction: row-reverse;
}

.message-item.own .message-content {
  align-items: flex-end;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 68%;
}

.message-info {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.message-bubble {
  background: var(--bg-color-white);
  color: var(--text-primary);
  padding: 10px 16px;
  border-radius: 14px;
  word-wrap: break-word;
  line-height: 1.55;
  font-size: 14px;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.message-item:not(.own) .message-bubble {
  border-top-left-radius: 4px;
}

.message-item.own .message-bubble {
  background: linear-gradient(135deg, #a8e6cf, #95ec69);
  color: #1f2937;
  box-shadow: 0 2px 6px rgba(149, 236, 105, 0.25);
  border-top-right-radius: 4px;
}

.image-message {
  cursor: pointer;
}

.message-image {
  max-width: 240px;
  border-radius: 4px;
}

.emoji-message {
  line-height: 0;
}

.emoji-image {
  width: 80px;
  height: 80px;
  border-radius: 2px;
}

/* 操作按钮组：上下排列，绝对定位在气泡上方 */
.action-btns {
  position: absolute;
  top: -52px;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 3px;
  z-index: 5;
}

.action-btn {
  font-size: 11px;
  height: 22px;
  width: 48px;
  padding: 0;
  border-radius: 4px;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 对方消息气泡上的按钮 */
.message-item:not(.own) .action-btn {
  color: var(--text-secondary);
  background: var(--bg-color-elevated);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}

.message-item:not(.own) .action-btn:hover {
  color: var(--color-primary);
  background: var(--color-primary-light-1);
}

/* 自己的消息气泡上的按钮 */
.message-item.own .action-btn {
  color: rgba(31, 41, 55, 0.75);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.message-item.own .action-btn:hover {
  color: #1f2937;
  background: rgba(255, 255, 255, 0.95);
}

/* 被引用消息卡片 */
.replied-card {
  background: rgba(0, 0, 0, 0.05);
  border-left: 3px solid var(--color-primary);
  border-radius: 4px;
  padding: 4px 10px;
  margin-bottom: 6px;
  font-size: 12px;
  cursor: default;
}

.replied-name {
  color: var(--color-primary);
  font-weight: 600;
  margin-bottom: 2px;
  font-size: 11px;
}

.replied-content {
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 240px;
}

.message-item.own .replied-card {
  background: rgba(255, 255, 255, 0.2);
  border-left-color: rgba(255, 255, 255, 0.6);
}

.message-item.own .replied-name {
  color: rgba(255, 255, 255, 0.9);
}

.message-item.own .replied-content {
  color: rgba(255, 255, 255, 0.7);
}

.recalled {
  color: var(--text-secondary);
  font-style: italic;
  font-size: 13px;
}
</style>
