<template>
  <div class="message-bubble-wrapper" :class="{ own: isOwn }">
    <div class="avatar">
      <el-avatar :size="40" :src="avatarUrl">
        {{ message.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
    </div>

    <div class="message-container">
      <div class="message-info" v-if="showInfo">
        <span class="nickname">{{ message.fromUserNickname }}</span>
        <span class="time">{{ formatTime(message.sendTime) }}</span>
      </div>

      <div class="message-bubble" :class="{ recalled: message.isRecalled }">
        <RecalledMessage v-if="message.isRecalled" :is-own="isOwn" />

        <TextMessage v-else-if="message.messageType === 1" :content="message.content" />

        <ImageMessage v-else-if="message.messageType === 2" :src="message.content" />

        <VoiceMessage v-else-if="message.messageType === 4" :url="message.content" :duration="message.duration"
          :message-id="message.id" />

        <CallRecord v-else-if="message.messageType === 5 || message.messageType === 6"
          :call-type="message.messageType === 5 ? 'voice' : 'video'" :duration="message.content" />

        <span v-else>{{ message.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 消息气泡组件（新版），按消息类型分发到不同子组件 @component */
import { computed } from 'vue'
import defaultAvatar from '@/assets/images/default-avatar.png'
import TextMessage from '../messageBubble/TextMessage.vue'
import ImageMessage from '../messageBubble/ImageMessage.vue'
import VoiceMessage from '../messageBubble/VoiceMessage.vue'
import CallRecord from '../messageBubble/CallRecord.vue'
import RecalledMessage from '../messageBubble/RecalledMessage.vue'

/** 组件属性：消息对象、是否本人发送、是否显示信息 */
const props = defineProps<{
  message: {
    id: number
    fromUserId: number
    fromUserNickname: string
    fromUserAvatar?: string | null
    messageType: number
    content: string
    duration?: number
    sendTime: string
    isRecalled: boolean
  }
  isOwn: boolean
  showInfo?: boolean
}>()

/** 头像 URL @returns 头像地址或默认头像 */
const avatarUrl = computed(() => {
  return props.message.fromUserAvatar || defaultAvatar
})

/** 格式化时间 @param time ISO 时间字符串 @returns HH:mm 格式 */
const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.message-bubble-wrapper {
  display: flex;
  gap: 14px;
  margin-bottom: 18px;
  align-items: flex-start;
}

.message-bubble-wrapper.own {
  flex-direction: row-reverse;
}

.message-bubble-wrapper.own .message-container {
  align-items: flex-end;
}

.message-container {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-info {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-bubble {
  background: #fff;
  padding: 12px 20px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  word-break: break-word;
  font-size: 15px;
  line-height: 1.6;
}

.message-bubble-wrapper.own .message-bubble {
  background: linear-gradient(135deg, #7c6df0, #a08aff);
  color: white;
  border-bottom-right-radius: 6px;
}

.message-bubble-wrapper:not(.own) .message-bubble {
  background: #fff;
  border-bottom-left-radius: 6px;
}

.message-bubble.recalled {
  background: #f5f5f5;
  color: #909399;
  font-style: italic;
}
</style>
