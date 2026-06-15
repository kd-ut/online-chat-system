<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <div v-if="loading && messages.length === 0" class="loading">
      <el-skeleton :rows="2" animated />
    </div>

    <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ own: msg.fromUserId === currentUserId }">
      <el-avatar :size="40" :src="msg.fromUserAvatar || ''">
        {{ msg.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="message-content">
        <div class="message-info">
          <span class="name">{{ msg.fromUserNickname }}</span>
          <span class="time">{{ formatRelativeTime(msg.sendTime) }}</span>
        </div>
        <div class="message-bubble" :class="{ recalled: msg.isRecalled }"
          @mouseenter="onBubbleEnter(msg.id)" @mouseleave="onBubbleLeave">
          <!-- 被引用消息卡片 -->
          <div v-if="msg.repliedMessage && !msg.isRecalled" class="replied-card" @click.stop>
            <div class="replied-name">{{ msg.repliedMessage.fromUserNickname }}</div>
            <div class="replied-content">{{ msg.repliedMessage.content || '消息已撤回' }}</div>
          </div>

          <span v-if="!msg.isRecalled">{{ msg.content }}</span>
          <span v-else class="recalled-text">
            {{ msg.fromUserId === currentUserId ? '你撤回了一条消息' : '对方撤回了一条消息' }}
          </span>

          <!-- 操作按钮组：JS 定时器控制显隐，上下排列 -->
          <div v-show="hoveredMsgId === msg.id" class="action-btns"
            @mouseenter="onActionsEnter(msg.id)" @mouseleave="onActionsLeave">
            <el-button v-if="msg.fromUserId === currentUserId && !msg.isRecalled && canRecall(msg)"
              class="action-btn recall-btn" text size="small"
              @click.stop="handleGroupRecall(msg.id)">撤回</el-button>
            <el-button v-if="!msg.isRecalled" class="action-btn reply-btn" text size="small"
              @click.stop="$emit('reply', msg)">回复</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading && messages.length > 0" class="loading-more">
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
      <span>加载更多...</span>
    </div>

    <div ref="scrollBottomRef"></div>
  </div>
</template>

<script setup lang="ts">
/** 群聊消息列表组件 @component */
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/date'
import { recallGroupMessageApi } from '@/api/group'

/** 组件属性：消息列表、当前用户 ID、加载状态 */
const props = defineProps<{
  messages: any[]
  currentUserId: number | undefined
  loading: boolean
}>()

/** 组件事件：加载更多、回复消息 */
const emit = defineEmits(['loadMore', 'reply'])

/** 撤回时限：2分钟（毫秒） */
const RECALL_LIMIT = 2 * 60 * 1000

/** JS 定时器控制按钮显隐：当前悬停的消息ID */
const hoveredMsgId = ref<number | null>(null)
let hideTimer: ReturnType<typeof setTimeout> | null = null

const clearHideTimer = () => {
  if (hideTimer) { clearTimeout(hideTimer); hideTimer = null }
}

const onBubbleEnter = (msgId: number) => {
  clearHideTimer()
  hoveredMsgId.value = msgId
}

const onBubbleLeave = () => {
  hideTimer = setTimeout(() => { hoveredMsgId.value = null }, 200)
}

const onActionsEnter = (msgId: number) => {
  clearHideTimer()
  hoveredMsgId.value = msgId
}

const onActionsLeave = () => {
  hideTimer = setTimeout(() => { hoveredMsgId.value = null }, 200)
}

/** 判断消息是否可撤回 @param msg 消息对象 @returns 是否在2分钟内 */
const canRecall = (msg: any) => {
  if (!msg.sendTime) return false
  const now = Date.now()
  const sendTime = new Date(msg.sendTime).getTime()
  return now - sendTime <= RECALL_LIMIT
}

/** 撤回群消息 @param messageId 消息ID */
const handleGroupRecall = async (messageId: number) => {
  try {
    await recallGroupMessageApi(messageId)
    ElMessage.success('已撤回')
    // 本地立即更新状态
    const msg = props.messages.find(m => m.id === messageId)
    if (msg) {
      msg.isRecalled = true
      if (msg.repliedMessage) {
        msg.repliedMessage.content = ''
      }
    }
  } catch {
    ElMessage.error('撤回失败')
  }
}

/** 列表容器引用 */
const listRef = ref<HTMLElement>()
/** 底部锚点引用 */
const scrollBottomRef = ref<HTMLElement>()

/** 滚动到底部 @returns void */
const scrollToBottom = () => {
  nextTick(() => {
    scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

/** 滚动事件处理，触底加载更多 @returns void */
const handleScroll = () => {
  if (!listRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = listRef.value
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    emit('loadMore')
  }
}

/** 监听消息数量变化自动滚动到底部 */
watch(() => props.messages.length, () => {
  scrollToBottom()
}, { immediate: true })

defineExpose({ scrollToBottom })
</script>

<style scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
  background: var(--bg-color);
}

.message-item {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
  padding: 0 12px;
  animation: messageIn 0.3s ease;
}

@keyframes messageIn {
  from { opacity: 0; transform: translateY(10px); }
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
  gap: 10px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.message-info .name {
  font-weight: 600;
  color: var(--color-primary);
}

.message-bubble {
  background: #ffffff;
  color: #1f2937;
  padding: 10px 16px;
  border-radius: 6px;
  word-wrap: break-word;
  line-height: 1.55;
  font-size: 14px;
  border: 1px solid #ececec;
  border-top-left-radius: 2px;
  position: relative;
}

.message-item.own .message-bubble {
  background: linear-gradient(135deg, #a8e6cf, #95ec69);
  color: #1f2937;
  border: none;
  border-top-right-radius: 2px;
  border-top-left-radius: 6px;
  box-shadow: 0 2px 6px rgba(149, 236, 105, 0.25);
}

.message-bubble.recalled {
  background: #f5f5f5;
  color: #909399;
}

.message-item.own .message-bubble.recalled {
  background: rgba(149, 236, 105, 0.2);
  color: #909399;
}

.recalled-text {
  color: #909399;
  font-style: italic;
  font-size: 13px;
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
  border-left-color: rgba(255, 255, 255, 0.5);
}

.message-item.own .replied-name {
  color: rgba(31, 41, 55, 0.8);
}

.message-item.own .replied-content {
  color: rgba(31, 41, 55, 0.6);
}

.loading {
  padding: 24px;
  text-align: center;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

.is-loading {
  animation: rotate 1.2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
