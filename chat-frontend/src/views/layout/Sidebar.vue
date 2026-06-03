<template>
  <div class="sidebar">
    <SidebarHeader />

    <SidebarTabs :active-tab="activeTab" :badge-count="friendStore.friendRequests.length"
      :friend-unread-count="friendUnreadCount" :is-admin="userStore.isAdmin()"
      @update:active-tab="activeTab = $event" @go-to-admin="goToAdmin" />

    <div class="sidebar-content">
      <FriendList v-show="activeTab === 'friends'" :current-chat-user-id="currentChatUserId"
        @select-chat="handleSelectChat" @write-impression="handleWriteImpression" />

      <GroupList v-show="activeTab === 'groups'" :groups="groupList" :current-group-id="currentGroupId"
        @select="selectGroup" @create="showCreateGroupDialog = true" />

      <RequestList v-show="activeTab === 'requests'" :requests="friendStore.friendRequests"
        @agree="handleRequest($event, 1)" @reject="handleRequest($event, 2)" />

      <ImpressionBoard v-show="activeTab === 'impressions'" :target-user-id="impressionTargetUserId"
        @clear-target="impressionTargetUserId = null" />
    </div>

    <CreateGroupDialog v-model="showCreateGroupDialog" :friend-list="friendListForGroup" @submit="handleCreateGroup" />
  </div>
</template>

<script setup lang="ts">
/** 侧边栏组件，整合好友/群聊/申请/印象选项卡 @component */
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import { useFriendStore } from '@/stores/friendStore'
import { handleFriendRequestApi } from '@/api/friend'
import { getGroupListApi, createGroupApi, type GroupVO } from '@/api/group'
import { getFriendListApi, type FriendVO } from '@/api/friend'
import { websocketService } from '@/utils/websocket'
import FriendList from '@/components/friend/FriendList.vue'
import ImpressionBoard from '@/components/impression/ImpressionBoard.vue'
import SidebarHeader from './sidebar/SidebarHeader.vue'
import SidebarTabs from './sidebar/SidebarTabs.vue'
import GroupList from './sidebar/GroupList.vue'
import RequestList from './sidebar/RequestList.vue'
import CreateGroupDialog from './sidebar/CreateGroupDialog.vue'

/** 组件事件：选择好友聊天、选择群聊 */
const emit = defineEmits(['selectChat', 'selectGroup'])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const friendStore = useFriendStore()

/** 当前激活的 Tab */
const activeTab = ref('friends')
/** 当前聊天好友 ID */
const currentChatUserId = ref<number | null>(null)
/** 当前群聊 ID */
const currentGroupId = ref<number | null>(null)
/** 群聊列表 */
const groupList = ref<GroupVO[]>([])
/** 创建群聊对话框显示状态 */
const showCreateGroupDialog = ref(false)
/** 用于创建群聊的好友列表 */
const friendListForGroup = ref<FriendVO[]>([])
const impressionTargetUserId = ref<number | null>(null)

/** 好友未读消息总数（用于好友 Tab 角标） */
const friendUnreadCount = computed(() => {
  let total = 0
  for (const group of friendStore.friendList) {
    for (const friend of group.friends) {
      total += friend.unreadCount || 0
    }
  }
  return total
})

/** 跳转到管理后台 @returns void */
const goToAdmin = () => {
  router.push('/admin')
}

/** 加载群聊列表 @returns Promise<void> */
const loadGroupList = async () => {
  try {
    const res = await getGroupListApi()
    groupList.value = res
  } catch (error) {
    console.error('加载群聊列表失败', error)
  }
}

/** 选择群聊 @param group 群聊对象 @returns void */
const selectGroup = (group: GroupVO) => {
  currentGroupId.value = group.id
  group.unreadCount = 0
  emit('selectGroup', group)
}

/** 创建群聊 @param data 群聊信息 @returns Promise<void> */
const handleCreateGroup = async (data: { name: string; notice?: string; memberIds: number[] }) => {
  try {
    const newGroup = await createGroupApi(data)
    groupList.value.unshift(newGroup)
    ElMessage.success('群聊创建成功')
    selectGroup(newGroup)
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
    throw error
  }
}

/** 加载好友列表（用于创建群聊时的邀请） @returns Promise<void> */
const loadFriendListForGroup = async () => {
  const res = await getFriendListApi()
  const friends: FriendVO[] = []
  for (const group of res) {
    friends.push(...group.friends)
  }
  friendListForGroup.value = friends
}

/** 处理好友申请 @param requestId 申请 ID @param status 处理状态(1:同意 2:拒绝) @returns Promise<void> */
const handleRequest = async (requestId: number, status: number) => {
  try {
    await handleFriendRequestApi(requestId, status)
    ElMessage.success(status === 1 ? '已同意' : '已拒绝')
    friendStore.loadFriendRequests()
    friendStore.loadFriendList()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

/** 选择聊天好友 @param friend 好友对象 @returns void */
const handleWriteImpression = (userId: number) => {
  impressionTargetUserId.value = userId
  activeTab.value = 'impressions'
}

const handleSelectChat = (friend: any) => {
  const userId = friend?.userId || friend?.id
  if (userId) {
    currentChatUserId.value = userId
    emit('selectChat', {
      id: userId,
      userId: userId,
      nickname: friend?.nickname || friend?.name || '好友',
      avatar: friend?.avatar,
      isOnline: friend?.isOnline,
      remark: friend?.remark
    })
  }
}

/** 收到群消息时更新未读计数 @param data 消息数据 @returns void */
const onGroupMessage = (data: any) => {
  const group = groupList.value.find(g => g.id === data.groupId)
  if (group && currentGroupId.value !== data.groupId) {
    group.unreadCount = (group.unreadCount || 0) + 1
  }
}

/** 收到私聊消息时更新好友未读计数（红点），当前聊天窗口内不增加 @param data 消息数据 */
const onPrivateMessage = (data: any) => {
  if (currentChatUserId.value !== data.fromUserId) {
    friendStore.incrementUnreadForFriend(data.fromUserId)
  }
}

watch(() => route.query, () => {
  loadGroupList()
  friendStore.loadFriendRequests()
}, { deep: true })

/** 同步路由参数到当前聊天状态，并清除对应未读红点 */
watch(() => route.query.friendId, (friendId) => {
  const id = friendId ? Number(friendId) : null
  currentChatUserId.value = id
  if (id) friendStore.clearUnreadForFriend(id)
}, { immediate: true })

watch(() => route.query.groupId, (groupId) => {
  const id = groupId ? Number(groupId) : null
  currentGroupId.value = id
  if (id) {
    const group = groupList.value.find(g => g.id === id)
    if (group) group.unreadCount = 0
  }
}, { immediate: true })

onMounted(() => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
  loadGroupList()
  loadFriendListForGroup()
  websocketService.onStatus((data: any) => {
    if (data.userId) {
      friendStore.updateFriendOnlineStatus?.(data.userId, data.online)
    }
  })
  websocketService.onGroupMessage(onGroupMessage)
  websocketService.onMessage(onPrivateMessage)
  websocketService.onFriendRequest(() => {
    friendStore.loadFriendRequests()
  })
  websocketService.onFriendRequestHandled(() => {
    friendStore.loadFriendList()
    friendStore.loadFriendRequests()
  })
})
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  background: var(--bg-color-white);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative;
  z-index: 20;
  border: 3px solid #b3d9ff;
  border-radius: 24px;
  box-shadow: var(--box-shadow-base);
  overflow: hidden;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-content::-webkit-scrollbar {
  width: 4px;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 4px;
}

.sidebar-content:hover::-webkit-scrollbar-thumb {
  background: var(--text-secondary);
}
</style>
