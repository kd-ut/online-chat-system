<template>
  <BaseDialog v-model="visible" :title="callTitle" :width="callType === 'video' ? '800px' : '400px'"
    :close-on-click-modal="false" :show-close="false" destroy-on-close @close="hangupCall">
    <div class="call-container" :class="{ 'video-call': callType === 'video' }">
      <template v-if="callType === 'video'">
        <RemoteVideo :target-user="targetUser" :is-connected="isConnected" :status-text="callStatusText" :stream="remoteStream" />
        <LocalVideo :stream="localStream" />
      </template>
      <template v-else>
        <VoiceCallUI :target-user="targetUser" :is-connected="isConnected" :status-text="callStatusText" :duration="callDuration" :stream="remoteStream" />
      </template>
    </div>
    <template #footer>
      <CallActions :is-connected="isConnected" :is-caller="isCaller" @accept="acceptCall" @hangup="hangupCall" />
    </template>
  </BaseDialog>
</template>

<script setup lang="ts">
/** 通话对话框组件（语音/视频），管理 WebRTC 通话生命周期 @component */
import { ref, computed, watch, onUnmounted } from 'vue'
import BaseDialog from '@/components/common/BaseDialog.vue'
import { notify } from '@/utils/notify'
import { websocketService } from '@/utils/websocket'
import { useWebRTC } from '@/composables/useWebRTC'
import RemoteVideo from './RemoteVideo.vue'
import LocalVideo from './LocalVideo.vue'
import VoiceCallUI from './VoiceCallUI.vue'
import CallActions from './CallActions.vue'

/** 组件属性：对话框显示状态、目标用户、通话类型、是否为呼叫方及初始 Offer */
const props = defineProps<{
  modelValue: boolean
  targetUser: any
  callType: 'voice' | 'video'
  isCaller: boolean
  initialOffer?: any
}>()

/** 组件事件：更新对话框状态、结束通话、通话被接受 */
const emit = defineEmits(['update:modelValue', 'endCall', 'callAccepted', 'callRecord'])

/** 对话框可见性状态 */
const visible = ref(false)
/** 远程媒体流 */
const remoteStream = ref<MediaStream | null>(null)
/** 目标用户 ID 计算属性 */
const targetUserId = computed(() => props.targetUser?.userId || props.targetUser?.id)
/** 待处理的 ICE 候选者队列（在远程流就绪前缓存） */
const pendingCandidates: any[] = []
/** 是否已发送挂断信号（防止重复发送） */
let hangupSent = false

/** 使用 WebRTC composable 获取连接状态、通话时长、本地流及相关操作方法 */
const { isConnected, callDuration, localStream, createOffer, handleOffer, handleAnswer, addIceCandidate, hangup } = useWebRTC(props.callType)

/** 通话标题（语音/视频 + 对方昵称） */
const callTitle = computed(() => `${props.callType === 'voice' ? '语音' : '视频'}通话 - ${props.targetUser?.nickname}`)
/** 通话状态文本 */
const callStatusText = computed(() => isConnected.value ? '通话中' : (props.isCaller ? '正在呼叫...' : '来电'))

/** 设置远程媒体流 @param s 远程媒体流 */
const onRemoteStream = (s: MediaStream) => { remoteStream.value = s }

/** 刷新 ICE 候选者队列，将缓存的候选者全部加入到连接中 @returns Promise<void> */
const flushCandidates = async () => {
  for (const c of pendingCandidates.splice(0)) {
    await addIceCandidate(c.candidate, c.sdpMid, c.sdpMLineIndex)
  }
}

/** 执行挂断逻辑 @param showMessage 挂断后显示的消息文本 */
const doHangup = (showMessage: string | null) => {
  if (hangupSent) return
  hangupSent = true
  hangup()
  remoteStream.value = null
  if (showMessage) notify.info(showMessage)
  if (visible.value) {
    visible.value = false
    emit('update:modelValue', false)
    emit('endCall')
  }
}

/** 处理 WebSocket 信令消息 @param data 信令数据 */
const handleSignal = async (data: any) => {
  if (data.fromUserId !== targetUserId.value) return
  if (data.action === 'answer') {
    await handleAnswer(data.sdp)
    flushCandidates()
  } else if (data.action === 'ice-candidate' && data.candidate) {
    if (remoteStream.value) {
      await addIceCandidate(data.candidate, data.sdpMid, data.sdpMLineIndex)
    } else {
      pendingCandidates.push(data)
    }
  } else if (data.action === 'hangup') {
    doHangup('对方已挂断')
  }
}

/** 发起通话（呼叫方） @returns Promise<void> */
const startCall = async () => {
  if (!targetUserId.value) { doHangup('通话对象信息错误'); return }
  try {
    await createOffer(onRemoteStream, targetUserId.value)
  } catch (err) {
    console.error('发起通话失败:', err)
    hangup()
  }
}

/** 接听通话（被呼叫方） @returns Promise<void> */
const acceptCall = async () => {
  if (!targetUserId.value) return
  try {
    if (props.initialOffer?.sdp) {
      await handleOffer(props.initialOffer.sdp, onRemoteStream, targetUserId.value)
      flushCandidates()
      emit('callAccepted')
    } else {
      doHangup('未收到通话请求')
    }
  } catch (err) {
    console.error('接听通话失败:', err)
    hangup()
  }
}

/** 挂断通话处理 @returns Promise<void> */
const hangupCall = () => {
  if (hangupSent) return
  if (targetUserId.value) {
    websocketService.sendCallSignal({ action: 'hangup', toUserId: targetUserId.value, callType: props.callType })
    // 发送通话记录
    emit('callRecord', { callType: props.callType, duration: callDuration.value })
  }
  doHangup(null)
}


/** 是否已注册信令回调 */
let registered = false
/** 监听对话框显示状态，自动发起或清理通话 */
watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    hangupSent = false
    if (!registered) {
      websocketService.onCallSignal(handleSignal)
      registered = true
    }
    if (props.isCaller) await startCall()
  } else {
    hangup()
  }
}, { immediate: true })

/** 组件卸载时确保挂断通话 */
onUnmounted(() => {
  if (!hangupSent) {
    hangup()
  }
})
</script>

<style scoped>
.call-container { text-align: center; padding: 20px; position: relative; }
.video-call { height: 400px; position: relative; }
</style>
