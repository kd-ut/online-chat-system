<template>
  <div class="communication-bar" :class="{ expanded: isExpanded }">
    <Toolbar :is-expanded="isExpanded" @open-image-upload="openImageUpload" @start-record="startRecord"
      @stop-record="stopRecord" @cancel-record="cancelRecord" @start-voice-call="startVoiceCall"
      @start-video-call="startVideoCall" @open-emoji-picker="openEmojiPicker" />

    <RecordingTip :is-recording="isRecording" :record-duration="recordDuration" />

    <EmojiPicker v-model="showEmojiPicker" :system-emojis="systemEmojis" :user-emojis="userEmojis" @select="sendEmoji"
      @upload="uploadCustomEmoji" @delete="deleteEmoji" />

    <input type="file" ref="imageInput" accept="image/*" style="display: none" @change="handleImageUpload" />
    <input type="file" ref="emojiInput" accept="image/*" style="display: none" @change="handleEmojiUpload" />
    <PromptDialog v-model="showEmojiNamePrompt" title="上传表情" message="请输入表情名称"
      confirm-text="确定" cancel-text="取消"
      :input-pattern="/^[\u4e00-\u9fa5a-zA-Z0-9]{1,20}$/" input-error-message="请输入1-20个字符"
      @confirm="onEmojiNameConfirm" />
  </div>
</template>

<script setup lang="ts">
/** 通信工具栏组件，提供图片/语音/表情/通话等功能的集成面板 @component */
import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImageApi, uploadVoiceApi } from '@/api/message'
import { getSystemEmojisApi, getUserEmojisApi, uploadEmojiApi, deleteEmojiApi, type EmojiVO } from '@/api/emoji'
import Toolbar from '../communication/Toolbar.vue'
import RecordingTip from '../communication/RecordingTip.vue'
import PromptDialog from '@/components/common/PromptDialog.vue'
import EmojiPicker from '../communication/EmojiPicker.vue'

/** 组件属性：当前聊天用户 ID */
const props = defineProps<{ currentChatUserId?: number }>()
/** 组件事件：发送图片/语音/表情、发起通话 */
const emit = defineEmits(['sendImage', 'sendVoice', 'sendEmoji', 'startVoiceCall', 'startVideoCall'])

/** 工具栏展开状态 */
const isExpanded = ref(false)
/** 是否正在录音 */
const isRecording = ref(false)
/** 录音时长（秒） */
const recordDuration = ref(0)
/** 表情选择器是否显示 */
const showEmojiPicker = ref(false)

/** 系统表情列表 */
const systemEmojis = ref<EmojiVO[]>([])
/** 用户自定义表情列表 */
const userEmojis = ref<EmojiVO[]>([])

/** 图片选择输入引用 */
const imageInput = ref<HTMLInputElement>()
/** 表情图片上传输入引用 */
const emojiInput = ref<HTMLInputElement>()

/** 录音器实例 */
let mediaRecorder: MediaRecorder | null = null
/** 录音数据块数组 */
let audioChunks: Blob[] = []
/** 录音定时器 ID */
let recordingTimer: number | null = null
/** 录音开始时间戳 */
let startTime: number = 0
/** 麦克风流 */
let mediaStream: MediaStream | null = null

/** 切换工具栏展开/收起 @returns void */
const toggleExpand = () => { isExpanded.value = !isExpanded.value }

/** 打开图片选择器 @returns void */
const openImageUpload = () => { imageInput.value?.click() }
/** 处理图片选择上传 @param event 文件选择事件 @returns Promise<void> */
const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) return ElMessage.error('请选择图片文件')
  try {
    const url = await uploadImageApi(file)
    emit('sendImage', url)
  } catch { /* error already shown by response interceptor */ }
  input.value = ''
}

/** 获取浏览器支持的音频 MIME 类型 @returns 支持的 MIME 类型字符串 */
const getSupportedMimeType = () => {
  const types = ['audio/webm;codecs=opus', 'audio/webm', 'audio/ogg;codecs=opus', 'audio/mp4', 'audio/mpeg']
  return types.find(t => MediaRecorder.isTypeSupported(t)) || ''
}

/** 开始录音 @returns Promise<void> */
const startRecord = async () => {
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    const mimeType = getSupportedMimeType()
    const options: any = mimeType ? { mimeType } : {}
    mediaRecorder = new MediaRecorder(mediaStream, options)
    audioChunks = []
    startTime = Date.now()
    recordDuration.value = 0

    recordingTimer = setInterval(() => {
      recordDuration.value = Math.floor((Date.now() - startTime) / 1000)
      if (recordDuration.value >= 60) stopRecord()
    }, 200) as unknown as number

    mediaRecorder.ondataavailable = (e) => { audioChunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      const duration = Math.floor((Date.now() - startTime) / 1000)
      if (duration < 1) {
        ElMessage.warning('录音时间太短')
        mediaStream?.getTracks().forEach(t => t.stop())
        mediaStream = null
        return
      }
      const mimeType = getSupportedMimeType() || 'audio/webm'
      const ext = mimeType.includes('mp4') ? 'mp4' : mimeType.includes('ogg') ? 'ogg' : 'webm'
      const blob = new Blob(audioChunks, { type: mimeType })
      const file = new File([blob], `voice_${Date.now()}.${ext}`, { type: mimeType })
      try {
        const url = await uploadVoiceApi(file)
        emit('sendVoice', url, duration)
      } catch { /* error already shown by response interceptor */ }
      mediaStream?.getTracks().forEach(t => t.stop())
      mediaStream = null
    }
    mediaRecorder.start(100)
    isRecording.value = true
  } catch { ElMessage.error('无法获取麦克风权限') }
}

/** 停止录音 @returns void */
const stopRecord = () => {
  if (mediaRecorder && isRecording.value && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) clearInterval(recordingTimer)
  }
}

/** 取消录音 @returns void */
const cancelRecord = () => {
  if (mediaRecorder && isRecording.value && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) clearInterval(recordingTimer)
    ElMessage.info('已取消录音')
    mediaStream?.getTracks().forEach(t => t.stop())
    mediaStream = null
  }
}

/** 发起语音通话 @returns void */
const startVoiceCall = () => {
  if (props.currentChatUserId) emit('startVoiceCall', props.currentChatUserId)
  else ElMessage.warning('请先选择聊天对象')
}
/** 发起视频通话 @returns void */
const startVideoCall = () => {
  if (props.currentChatUserId) emit('startVideoCall', props.currentChatUserId)
  else ElMessage.warning('请先选择聊天对象')
}

/** 打开表情选择器 @returns Promise<void> */
const openEmojiPicker = async () => {
  await loadEmojis()
  showEmojiPicker.value = true
}
/** 加载表情数据 @returns Promise<void> */
const loadEmojis = async () => {
  try {
    systemEmojis.value = await getSystemEmojisApi()
    userEmojis.value = await getUserEmojisApi()
  } catch { console.error('加载表情失败') }
}

/** 发送表情 @param emoji 表情对象 @returns void */
const sendEmoji = (emoji: EmojiVO) => {
  console.log('发送表情:', emoji)
  emit('sendEmoji', emoji.url)
  ElMessage.success('表情已发送')
}

/** 表情名称输入对话框显示状态 */
const showEmojiNamePrompt = ref(false)
/** 待上传的表情文件 */
let pendingEmojiFile: File | null = null

/** 触发上传自定义表情 @returns void */
const uploadCustomEmoji = () => emojiInput.value?.click()
/** 处理表情文件选择 @param event 文件选择事件 @returns void */
const handleEmojiUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) { ElMessage.error('请选择图片文件'); return }
  pendingEmojiFile = file
  showEmojiNamePrompt.value = true
}
/** 确认上传表情名称 @param name 表情名称 @returns Promise<void> */
const onEmojiNameConfirm = async (name: string) => {
  const file = pendingEmojiFile
  pendingEmojiFile = null
  if (!file || !name) return
  try {
    const newEmoji = await uploadEmojiApi(file, name)
    userEmojis.value.unshift(newEmoji)
    ElMessage.success('表情上传成功')
  } catch { ElMessage.error('上传失败') }
}
/** 删除自定义表情 @param emojiId 表情 ID @returns Promise<void> */
const deleteEmoji = async (emojiId: number) => {
  try {
    await deleteEmojiApi(emojiId)
    userEmojis.value = await getUserEmojisApi()
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('删除失败')
  }
}

/** 暴露方法供父组件调用 */
defineExpose({ toggleExpand, openEmojiPicker })

/** 组件卸载时清理录音资源 */
onUnmounted(() => {
  if (mediaRecorder && isRecording.value) mediaRecorder.stop()
  if (mediaStream) mediaStream.getTracks().forEach(t => t.stop())
})
</script>

<style scoped>
.communication-bar {
  background: var(--bg-color-white);
}
</style>
