<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <div v-if="loading && messages.length === 0" class="loading">
      <el-skeleton :rows="2" animated />
    </div>

    <MessageBubble v-for="msg in messages" :key="msg.id" :message="msg" :is-own="msg.fromUserId === currentUserId"
      :show-info="true" @reply="(m) => $emit('reply', m)" />

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
/** 聊天消息列表组件，支持滚动加载和历史消息展示 @component */
import { ref, watch, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import MessageBubble from './MessageBubble.vue'

/** 组件属性：消息列表、当前用户 ID、加载状态 */
const props = defineProps<{
  messages: any[]
  currentUserId: number | undefined
  loading: boolean
}>()

/** 组件事件：加载更多消息、回复消息 */
const emit = defineEmits(['loadMore', 'reply'])

/** 列表容器引用 */
const listRef = ref<HTMLElement>()
/** 底部锚点引用，用于滚动到底部 */
const scrollBottomRef = ref<HTMLElement>()

/** 滚动到列表底部 @returns void */
const scrollToBottom = () => {
  nextTick(() => {
    scrollBottomRef.value?.scrollIntoView({ behavior: 'auto' })
  })
}

/** 滚动事件处理，检测是否触底以加载更多 @returns void */
const handleScroll = () => {
  if (!listRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = listRef.value
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    emit('loadMore')
  }
}

/** 监听消息数量变化，自动滚动到底部 */
watch(
  () => props.messages.length,
  () => {
    scrollToBottom()
  },
  { immediate: true }
)

defineExpose({ scrollToBottom })
</script>

<style scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
  background: var(--bg-color);
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
