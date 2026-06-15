<template>
  <div class="group-chat-window">
    <GroupChatHeader :group="group" :can-edit-notice="canEditNotice" :is-owner="isOwner"
      @command="handleGroupCommand" />

    <GroupNoticeBar :notice="group?.notice ?? null" @click="openNotice" />

    <GroupMessageList ref="messageListRef" :messages="messages" :current-user-id="currentUserId" :loading="loading"
      @load-more="loadMore" @reply="replyToMessage = $event" />

    <GroupMessageInput :muted="isMuted" :reply-to-message="replyToMessage"
      @send="sendMessage" @cancel-reply="replyToMessage = null" />

    <GroupNoticeDialog v-model="showNoticeDialog" :notice="group?.notice ?? null" :can-edit="canEditNotice"
      @save="handleUpdateNotice" />

    <GroupMembersDialog v-model="showMembers" :members="memberList" />

    <GroupManagementDialog v-model="showManage" :group-id="props.group?.id || 0" :members="memberList"
      :current-user-id="currentUserId!" :is-owner="isOwner" :can-manage="canEditNotice"
      @refresh="loadMembers" />

    <InviteFriendDialog v-model="showInvite" :friends="friendList" @invite="handleInvite" />
    <ConfirmDialog v-model="showQuitDialog" title="退出群聊" message="确定要退出该群聊吗？"
      type="warning" confirm-text="退出" cancel-text="取消" @confirm="confirmQuit" />
    <ConfirmDialog v-model="showDisbandDialog" title="解散群聊" message="确定要解散该群聊吗？此操作不可恢复"
      type="danger" confirm-text="解散" cancel-text="取消" @confirm="confirmDisband" />
  </div>
</template>

<script setup lang="ts">
/** 群聊聊天窗口组件，管理群聊消息/成员/公告/管理等完整功能 @component */
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getGroupHistoryApi,
  quitGroupApi,
  disbandGroupApi,
  inviteMemberApi,
  getGroupMembersApi,
  clearGroupUnreadApi,
  updateGroupNoticeApi,
  type GroupVO,
  type GroupMessageVO,
  type GroupMemberVO
} from '@/api/group'
import { getFriendListApi, type FriendVO } from '@/api/friend'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import GroupChatHeader from './GroupChatHeader.vue'
import GroupNoticeBar from './GroupNoticeBar.vue'
import GroupMessageList from './GroupMessageList.vue'
import GroupMessageInput from './GroupMessageInput.vue'
import GroupNoticeDialog from './GroupNoticeDialog.vue'
import GroupMembersDialog from './GroupMembersDialog.vue'
import GroupManagementDialog from './GroupManagementDialog.vue'
import InviteFriendDialog from './InviteFriendDialog.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

/** 组件属性：群聊信息 */
const props = defineProps<{
  group: GroupVO | null
}>()

/** 组件事件：更新群列表、更新未读计数 */
const emit = defineEmits(['update:list', 'updateUnreadCount'])

const userStore = useUserStore()
/** 当前登录用户 ID */
const currentUserId = userStore.userInfo?.id
/** 是否为群主 */
const isOwner = computed(() => props.group?.ownerId === currentUserId)

/** 当前用户是否可编辑公告（群主或管理员） */
const canEditNotice = computed(() => {
  const member = memberList.value.find(m => m.userId === currentUserId)
  return isOwner.value || member?.role === 1
})

/** 当前用户是否被禁言 */
const isMuted = computed(() => {
  const member = memberList.value.find(m => m.userId === currentUserId)
  return member?.muted === true
})

/** 消息列表 */
const messages = ref<GroupMessageVO[]>([])
/** 加载状态 */
const loading = ref(false)
/** 分页页码 */
const page = ref(1)
/** 是否还有更多历史消息 */
const hasMore = ref(true)
/** 消息列表组件引用 */
const messageListRef = ref()

/** 弹窗状态 */
const showNoticeDialog = ref(false)
const showMembers = ref(false)
const showManage = ref(false)
const showInvite = ref(false)
const showQuitDialog = ref(false)
const showDisbandDialog = ref(false)
/** 成员列表 */
const memberList = ref<GroupMemberVO[]>([])
/** 好友列表（用于邀请） */
const friendList = ref<FriendVO[]>([])
/** 当前引用的消息（回复功能） */
const replyToMessage = ref<any>(null)

/** 加载历史消息 @param reset 是否重置页码 @returns Promise<void> */
const loadHistory = async (reset = true) => {
  if (!props.group?.id) return
  if (reset) {
    page.value = 1
    hasMore.value = true
    messages.value = []
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getGroupHistoryApi(props.group.id, page.value, 20)
    const newMessages = res.records.reverse()
    if (reset) {
      messages.value = newMessages
    } else {
      messages.value = [...newMessages, ...messages.value]
    }
    hasMore.value = newMessages.length > 0
    page.value++
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

/** 加载更多历史消息 @returns void */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadHistory(false)
  }
}

/** 清除群聊未读计数 @returns Promise<void> */
const clearUnreadCount = async () => {
  if (!props.group?.id) return
  try {
    await clearGroupUnreadApi(props.group.id)
    emit('updateUnreadCount', props.group.id)
  } catch (error) {
    console.error('清除未读失败', error)
  }
}

/** 发送群消息 @param content 消息内容 @param replyToId 引用消息ID @returns void */
const sendMessage = (content: string, replyToId?: number) => {
  if (!props.group?.id) return
  if (isMuted.value) { ElMessage.warning('你已被禁言'); return }
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
  const tempMsg: GroupMessageVO = {
    id: Date.now(),
    groupId: props.group.id,
    fromUserId: currentUserId!,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar ?? null,
    content: content,
    messageType: 1,
    sendTime: new Date().toISOString(),
    isRecalled: false,
    replyToId,
    repliedMessage
  }
  messages.value.push(tempMsg)
  messageListRef.value?.scrollToBottom()
  websocketService.sendGroupMessage(props.group.id, content, 1, replyToId)
  replyToMessage.value = null
}

/** 接收 WebSocket 群消息 @param data 消息数据 @returns void */
const onGroupMessage = (data: any) => {
  // 处理发送方确认：用真实 messageId 更新本地临时消息
  if (data.type === 'group_message_sent' && props.group && data.groupId === props.group?.id) {
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

  if (props.group && data.groupId === props.group.id) {
    const newMsg: GroupMessageVO = {
      id: data.messageId,
      groupId: data.groupId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      fromUserAvatar: data.fromUserAvatar ?? null,
      content: data.content,
      messageType: data.messageType,
      sendTime: data.sendTime,
      isRecalled: false,
      replyToId: data.replyToId,
      repliedMessage: data.repliedMessage
    }
    messages.value.push(newMsg)
    messageListRef.value?.scrollToBottom()
    clearUnreadCount()
  }
}

/** 接收群消息撤回通知 @param data 撤回数据 @returns void */
const onGroupRecall = (data: any) => {
  if (props.group && data.groupId === props.group?.id) {
    const idx = messages.value.findIndex(m => m.id === data.messageId)
    if (idx !== -1) {
      messages.value[idx].isRecalled = true
      if (messages.value[idx].repliedMessage) {
        messages.value[idx].repliedMessage.content = ''
      }
    }
  }
}

/** 加载群成员列表 @returns Promise<void> */
const loadMembers = async () => {
  if (!props.group?.id) return
  try {
    memberList.value = await getGroupMembersApi(props.group.id)
  } catch (error) {
    console.error('加载群成员失败', error)
  }
}

/** 加载好友列表 @returns Promise<void> */
const loadFriendList = async () => {
  try {
    const res = await getFriendListApi()
    const friends: FriendVO[] = []
    for (const group of res) friends.push(...group.friends)
    friendList.value = friends
  } catch (error) {
    console.error('加载好友列表失败', error)
  }
}

/** 更新群公告 @param notice 公告内容 @returns Promise<void> */
const handleUpdateNotice = async (notice: string) => {
  if (!props.group?.id) return
  try {
    await updateGroupNoticeApi(props.group.id, notice)
    if (props.group) props.group.notice = notice
    ElMessage.success('群公告已更新')
    emit('update:list')
  } catch (error) {
    console.error(error)
    ElMessage.error('更新失败')
    throw error
  }
}

/** 打开公告弹窗 @returns void */
const openNotice = () => {
  showNoticeDialog.value = true
}

/** 处理群聊头部下拉菜单命令 @param command 命令标识 @returns Promise<void> */
const handleGroupCommand = async (command: string) => {
  if (!props.group) return

  if (command === 'notice' || command === 'viewNotice') {
    showNoticeDialog.value = true
  } else if (command === 'members') {
    await loadMembers()
    showMembers.value = true
  } else if (command === 'invite') {
    await loadFriendList()
    showInvite.value = true
  } else if (command === 'manage') {
    await loadMembers()
    showManage.value = true
  } else if (command === 'quit') {
    showQuitDialog.value = true
  } else if (command === 'disband') {
    showDisbandDialog.value = true
  }
}

/** 确认退出群聊 @returns Promise<void> */
const confirmQuit = async () => {
  await quitGroupApi(props.group!.id)
  ElMessage.success('已退出群聊')
  emit('update:list')
}

/** 确认解散群聊 @returns Promise<void> */
const confirmDisband = async () => {
  await disbandGroupApi(props.group!.id)
  ElMessage.success('群聊已解散')
  emit('update:list')
}

/** 邀请好友 @param userId 好友用户 ID @returns Promise<void> */
const handleInvite = async (userId: number) => {
  if (!props.group?.id) return
  try {
    await inviteMemberApi({ groupId: props.group.id, userId })
    ElMessage.success('邀请已发送')
  } catch (error) {
    console.error(error)
    ElMessage.error('邀请失败')
    throw error
  }
}

/** 监听群聊切换，重新加载数据和清除未读 */
watch(() => props.group, (newGroup) => {
  if (newGroup?.id) {
    loadHistory(true)
    clearUnreadCount()
    loadMembers()
  }
}, { immediate: true })

/** WebSocket 群消息回调清理函数 */
let unsubGroupMessage: (() => void) | null = null
let unsubGroupRecall: (() => void) | null = null

/** 注册 WebSocket 群消息回调 */
onMounted(() => {
  unsubGroupMessage = websocketService.onGroupMessage(onGroupMessage)
  unsubGroupRecall = websocketService.onGroupRecall(onGroupRecall)
})

onUnmounted(() => {
  if (unsubGroupMessage) { unsubGroupMessage(); unsubGroupMessage = null }
  if (unsubGroupRecall) { unsubGroupRecall(); unsubGroupRecall = null }
})
</script>

<style scoped>
.group-chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}
</style>
