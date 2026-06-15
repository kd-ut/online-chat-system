<template>
  <div class="chat-window">
    <ChatHeader :friend="friend" @download="showDownloadDialog = true" />

    <MessageList ref="messageListRef" :messages="messages" :current-user-id="currentUserId" :loading="loading"
      @load-more="loadMore" @reply="replyToMessage = $event" />

    <MessageInput :current-chat-user-id="friend?.userId" :reply-to-message="replyToMessage"
      @send="sendMessage" @send-image="sendImage"
      @send-voice="sendVoice" @send-emoji="sendEmoji" @start-voice-call="startVoiceCall"
      @start-video-call="startVideoCall" @cancel-reply="replyToMessage = null" />

    <CallDialog v-model="voiceCallVisible" :target-user="friend" call-type="voice" :is-caller="true"
      @end-call="endVoiceCall" @call-record="onCallRecord" />
    <CallDialog v-model="incomingCallVisible" :target-user="incomingCaller" :call-type="incomingCallType"
      :is-caller="false" :initial-offer="pendingOffer" @end-call="endIncomingCall"
      @call-accepted="stopRingtone" @call-record="onCallRecord" />

    <DownloadDialog v-model="showDownloadDialog" :friend-id="friend?.userId" :friend-name="friend?.nickname"
      :total-messages="totalMessageCount" :max-limit="maxDownloadLimit" @download="handleDownload" />
  </div>
</template>

<script setup lang="ts">
/** 单聊聊天窗口组件，管理消息/通话/下载等功能 @component */
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatHistoryApi, downloadChatHistoryApi, markAsReadApi } from '@/api/message'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'
import { useFriendStore } from '@/stores/friendStore'
import { useRtcStore } from '@/stores/rtcStore'
import ChatHeader from '../chat/ChatHeader.vue'
import MessageList from '../chat/MessageList.vue'
import MessageInput from '../chat/MessageInput.vue'
import CallDialog from '../call/CallDialog.vue'
import DownloadDialog from '../common/DownloadDialog.vue'

/** 组件属性：好友对象 */
const props = defineProps<{ friend: any }>()
const userStore = useUserStore()
const messageStore = useMessageStore()
const friendStore = useFriendStore()
const rtcStore = useRtcStore()
/** 当前登录用户 ID */
const currentUserId = userStore.userInfo?.id

/** 消息列表 */
const messages = ref<any[]>([])
/** 加载状态 */
const loading = ref(false)
/** 分页页码 */
const page = ref(1)
/** 是否还有更多 */
const hasMore = ref(true)
/** 总消息数 */
const totalMessageCount = ref(0)
/** 下载对话框显示状态 */
const showDownloadDialog = ref(false)
/** 当前引用的消息（回复功能） */
const replyToMessage = ref<any>(null)

/** 语音通话对话框可见 */
const voiceCallVisible = ref(false)
/** 来电对话框可见 */
const incomingCallVisible = ref(false)
/** 来电者信息 */
const incomingCaller = ref<any>(null)
/** 来电类型 */
const incomingCallType = ref<'voice' | 'video'>('voice')
/** 待处理的 Offer */
const pendingOffer = ref<any>(null)

/** 铃声 URL（缓存） */
let _ringUrl: string | null = null
/** 获取铃声 URL @returns 铃声地址 */
function getRingUrl(): string {
  if (_ringUrl === null) {
    try { _ringUrl = new URL('../../assets/audio/ring.MP3', import.meta.url).href }
    catch { _ringUrl = '' }
  }
  return _ringUrl
}
/** 铃声音频对象 */
let _ringAudio: HTMLAudioElement | null = null
/** 播放来电铃声 @returns void */
function startRingtone() {
  const url = getRingUrl()
  if (!url || _ringAudio) return
  try {
    _ringAudio = new Audio(url)
    _ringAudio.loop = true
    _ringAudio.volume = 0.5
    _ringAudio.preload = 'auto'
    _ringAudio.load()
    _ringAudio.addEventListener('canplaythrough', () => {
      _ringAudio?.play().catch(() => {})
    }, { once: true })
  } catch { /* ignore */ }
}
/** 停止来电铃声 @returns void */
function stopRingtone() {
  if (_ringAudio) { _ringAudio.pause(); _ringAudio.loop = false; _ringAudio.currentTime = 0; _ringAudio = null }
}

/** 最大下载条数 */
const maxDownloadLimit = 500
/** 消息列表组件引用 */
const messageListRef = ref()

/** 加载历史消息 @param reset 是否重置 @returns Promise<void> */
const loadHistory = async (reset = true) => {
  if (!props.friend?.userId) return
  if (reset) {
    // 优先使用缓存，避免重复请求
    const cached = messageStore.getCachedMessages(props.friend.userId)
    if (cached) {
      messages.value = cached.messages
      page.value = cached.page
      hasMore.value = cached.hasMore
      totalMessageCount.value = cached.total
      loading.value = false
      await nextTick()
      messageListRef.value?.scrollToBottom()
      return
    }
    page.value = 1
    hasMore.value = true
    messages.value = []
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getChatHistoryApi(props.friend.userId, page.value, 20)
    if (page.value === 1) totalMessageCount.value = res.total

    const newMessages = res.records.reverse()

    if (reset) {
      messages.value = newMessages
    } else {
      messages.value = [...newMessages, ...messages.value]
    }

    hasMore.value = newMessages.length > 0
    page.value++

    // 首页加载后写入缓存
    if (reset) {
      messageStore.setCachedMessages(props.friend.userId, {
        messages: messages.value,
        page: page.value,
        hasMore: hasMore.value,
        total: totalMessageCount.value,
        timestamp: Date.now()
      })
      await nextTick()
      messageListRef.value?.scrollToBottom()
    } else if (newMessages.length > 0) {
      // loadMore 后也更新缓存
      messageStore.setCachedMessages(props.friend.userId, {
        messages: messages.value,
        page: page.value,
        hasMore: hasMore.value,
        total: totalMessageCount.value,
        timestamp: Date.now()
      })
    }
  } catch (error) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

/** 加载更多消息 @returns void */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadHistory(false)
  }
}

/** 添加本地消息（发送后立即显示） @param content 内容 @param messageType 消息类型 @param duration 语音时长 @param replyToId 引用消息ID @returns void */
const addLocalMessage = (content: string, messageType: number, duration?: number, replyToId?: number) => {
  // 查找被引用消息信息
  let repliedMessage: any = undefined
  if (replyToId) {
    const found = messages.value.find(m => m.id === replyToId)
    if (found) {
      repliedMessage = {
        messageId: replyToId,
        content: found.isRecalled ? '' : found.content,
        fromUserNickname: found.fromUserNickname,
        messageType: found.messageType
      }
    }
  }
  const msg = {
    id: Date.now() + Math.random(),
    fromUserId: currentUserId,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar,
    content,
    messageType,
    duration,
    sendTime: new Date().toISOString(),
    isRecalled: false,
    replyToId,
    repliedMessage
  }
  messages.value.push(msg)
  messageStore.appendToCache(props.friend.userId, msg)
  messageListRef.value?.scrollToBottom()
}

/** 发送文本消息 @param content 文本内容 @param replyToId 引用消息ID @returns void */
const sendMessage = (content: string, replyToId?: number) => {
  if (!props.friend?.userId) return
  addLocalMessage(content, 1, undefined, replyToId)
  websocketService.sendMessage(props.friend.userId, content, 1, undefined, replyToId)
  replyToMessage.value = null
}

/** 发送图片 @param url 图片地址 @returns void */
const sendImage = (url: string) => {
  if (!props.friend?.userId) return
  addLocalMessage(url, 2)
  websocketService.sendMessage(props.friend.userId, url, 2)
}

/** 发送语音 @param url 语音地址 @param duration 时长 @returns void */
const sendVoice = (url: string, duration: number) => {
  if (!props.friend?.userId) return
  addLocalMessage(url, 4, duration)
  websocketService.sendMessage(props.friend.userId, url, 4, duration)
}

/** 发送表情 @param url 表情地址 @returns void */
const sendEmoji = (url: string) => {
  if (!props.friend?.userId) return
  addLocalMessage(url, 3)
  websocketService.sendMessage(props.friend.userId, url, 3)
}

/** 发起语音通话 @param toUserId 目标用户 ID @returns void */
const startVoiceCall = (toUserId: number) => {
  if (!toUserId) return ElMessage.warning('请先选择聊天对象')
  if (toUserId === currentUserId) return ElMessage.error('不能给自己打电话')
  voiceCallVisible.value = true
}

/** 发起视频通话 @param toUserId 目标用户 ID @returns void */
const startVideoCall = (toUserId: number) => {
  if (!toUserId) return ElMessage.warning('请先选择聊天对象')
  if (toUserId === currentUserId) return ElMessage.error('不能给自己打电话')
  void rtcStore.startDirectVideoCall(props.friend)
}

const endVoiceCall = () => { voiceCallVisible.value = false }
const endIncomingCall = () => {
  incomingCallVisible.value = false; incomingCaller.value = null; pendingOffer.value = null
  stopRingtone()
}

/** 处理通话记录（语音/视频通话结束后持久化到聊天记录） */
const onCallRecord = (data: { callType: 'voice' | 'video'; duration: number }) => {
  const messageType = data.callType === 'voice' ? 5 : 6
  const durationSec = data.duration || 0
  const content = String(durationSec)
  addLocalMessage(content, messageType)
  if (props.friend?.userId) {
    websocketService.sendMessage(props.friend.userId, content, messageType)
  }
}

/** 下载聊天记录 @param limit 下载条数 @returns Promise<void> */
const handleDownload = async (limit: number) => {
  try {
    await downloadChatHistoryApi(props.friend.userId, props.friend.nickname, limit)
    ElMessage.success('开始下载')
  } catch { ElMessage.error('下载失败') }
}

/** 标记消息为已读 @returns Promise<void> */
const markAsRead = async () => {
  if (!props.friend?.userId) return
  try {
    await markAsReadApi(props.friend.userId)
    messageStore.clearUnreadForFriend(props.friend.userId)
    friendStore.clearUnreadForFriend(props.friend.userId)
  } catch (error) { console.error(error) }
}

/** 收到新消息回调 @param data 消息数据 @returns void */
const onNewMessage = (data: any) => {
  // 处理发送方确认：用真实 messageId 更新本地临时消息
  if (data.type === 'message_sent' && data.toUserId === props.friend?.userId) {
    // 找到最近一条自己发送的、内容匹配的临时消息
    const tempMsg = [...messages.value].reverse().find(m =>
      m.fromUserId === currentUserId &&
      m.content === data.content &&
      m.messageType === data.messageType
    )
    if (tempMsg) {
      tempMsg.id = data.messageId
      tempMsg.replyToId = data.replyToId
      if (data.repliedMessage) {
        tempMsg.repliedMessage = data.repliedMessage
      }
    }
    return
  }

  if (props.friend?.userId === data.fromUserId) {
    // 去重：已存在相同 messageId 则不重复添加
    if (data.messageId && messages.value.some(m => m.id === data.messageId)) return
    const msg = {
      id: data.messageId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      fromUserAvatar: data.fromUserAvatar,
      content: data.content,
      messageType: data.messageType || 1,
      duration: data.duration,
      sendTime: data.sendTime,
      isRecalled: false,
      replyToId: data.replyToId,
      repliedMessage: data.repliedMessage
    }
    messages.value.push(msg)
    messageStore.appendToCache(props.friend.userId, msg)
    messageListRef.value?.scrollToBottom()
    friendStore.clearUnreadForFriend(data.fromUserId)
  }
}

/** 收到撤回通知回调 @param data 撤回数据 @returns void */
const onRecall = (data: any) => {
  // 处理自己撤回对方看到的通知，或对方撤回自己看到的通知
  if (props.friend?.userId === data.fromUserId || props.friend?.userId === data.toUserId) {
    const idx = messages.value.findIndex(m => m.id === data.messageId)
    if (idx !== -1) {
      messages.value[idx].isRecalled = true
      // 清除引用信息中的内容（显示"消息已撤回"）
      if (messages.value[idx].repliedMessage) {
        messages.value[idx].repliedMessage.content = ''
      }
      // 更新缓存
      messageStore.invalidateCache(props.friend.userId)
    }
  }
}

/** 通话信令回调（仅处理语音通话信令，视频走 SFU） @param data 信令数据 @returns void */
const onCallSignal = (data: any) => {
  if (data.action === 'offer' && data.fromUserId !== currentUserId) {
    pendingOffer.value = data
    startRingtone()
    incomingCaller.value = {
      id: data.fromUserId,
      userId: data.fromUserId,
      nickname: data.fromUserNickname
    }
    incomingCallType.value = data.callType === 'video' ? 'video' : 'voice'
    incomingCallVisible.value = true
  }
}

/** 监听好友切换，重新加载消息和标记已读 */
watch(() => props.friend, (newFriend) => {
  if (newFriend?.userId) {
    loadHistory(true)
    markAsRead()
  }
}, { immediate: true, deep: true })

/** WebSocket 回调清理函数 */
let unsubMessage: (() => void) | null = null
let unsubCallSignal: (() => void) | null = null
let unsubRecall: (() => void) | null = null

onMounted(() => {
  unsubMessage = websocketService.onMessage(onNewMessage)
  unsubCallSignal = websocketService.onCallSignal(onCallSignal)
  unsubRecall = websocketService.onRecall(onRecall)
  // SFU 视频通话结束后生成聊天记录
  rtcStore.onCallRecord((data) => {
    const durationSec = data.duration || 0
    addLocalMessage(String(durationSec), 6)
  })
})

onUnmounted(() => {
  if (unsubMessage) { unsubMessage(); unsubMessage = null }
  if (unsubCallSignal) { unsubCallSignal(); unsubCallSignal = null }
  if (unsubRecall) { unsubRecall(); unsubRecall = null }
  if (props.friend) markAsRead()
  rtcStore.onCallRecord(null)
})
</script>

<style scoped>
.chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-color-white);
}

</style>
