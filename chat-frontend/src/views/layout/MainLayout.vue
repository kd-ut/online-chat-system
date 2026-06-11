<template>
  <div class="app-container" @click="unlockAudio">
    <!-- 移动端遮罩 -->
    <div v-if="isMobile && sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false" />

    <div class="main-layout" :class="{
      'is-resizing': isResizing,
      'is-mobile': isMobile,
    }" :style="{ '--chatlist-width': chatlistWidth + 'px' }">
      <!-- 列1: 图标导航栏 -->
      <NavBar v-model="activeNav" />

      <!-- 列2: 聊天列表面板 -->
      <div class="list-panel" :class="{ 'mobile-overlay': isMobile, 'show': sidebarOpen }"
        :style="{ width: isMobile ? '85%' : 'var(--chatlist-width)' }" v-show="!isMobile || sidebarOpen">
        <ChatListPanel
          :active-nav="activeNav"
          :current-chat-user-id="currentChatUserId"
          :current-group-id="currentGroupId"
          :groups="groupList"
          :impression-target-user-id="impressionTargetUserId"
          @select-chat="handleSelectChat"
          @select-group="handleSelectGroup"
          @agree="handleRequest($event, 1)"
          @reject="handleRequest($event, 2)"
          @write-impression="handleWriteImpression"
          @create-group="showCreateGroupDialog = true"
          @clear-impression-target="impressionTargetUserId = null"
        />

        <!-- 创建群聊弹窗 -->
        <CreateGroupDialog v-model="showCreateGroupDialog" :friend-list="friendListForGroup"
          @submit="handleCreateGroup" />
      </div>

      <!-- 桌面端拖拽手柄 -->
      <div v-if="!isMobile" class="resize-handle" @mousedown="startResize" />

      <!-- 列3: 右侧面板 -->
      <div class="right-panel">
        <Header @toggle-sidebar="sidebarOpen = !sidebarOpen" />
        <Content />
      </div>
    </div>

    <!-- 移动端底部导航栏 -->
    <div v-if="isMobile" class="mobile-navbar">
      <button v-for="item in mobileNavItems" :key="item.key"
        class="mobile-nav-btn" :class="{ active: activeNav === item.key }"
        @click="activeNav = item.key">
        <el-icon :size="20"><component :is="item.icon" /></el-icon>
        <span class="mobile-nav-label">{{ item.label }}</span>
        <span v-if="item.badge" class="mobile-nav-badge">{{ item.badge }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 主布局组件，三栏布局（NavBar | ChatListPanel | Right Panel），支持响应式 @component */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Message, ChatLineSquare, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useFriendStore } from '@/stores/friendStore'
import { handleFriendRequestApi } from '@/api/friend'
import { getGroupListApi, createGroupApi, type GroupVO } from '@/api/group'
import { getFriendListApi, type FriendVO } from '@/api/friend'
import { websocketService } from '@/utils/websocket'
import { useResizable } from '@/composables'
import NavBar from './NavBar.vue'
import ChatListPanel from './ChatListPanel.vue'
import Header from './Header.vue'
import Content from './Content.vue'
import CreateGroupDialog from './sidebar/CreateGroupDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const friendStore = useFriendStore()

// ===== 导航状态 =====
const activeNav = ref('chats')

// ===== 聊天状态 =====
const currentChatUserId = ref<number | null>(null)
const currentGroupId = ref<number | null>(null)
const groupList = ref<GroupVO[]>([])
const showCreateGroupDialog = ref(false)
const friendListForGroup = ref<FriendVO[]>([])
const impressionTargetUserId = ref<number | null>(null)

// ===== 好友未读消息总数 =====
const friendUnreadCount = computed(() => {
  let total = 0
  for (const group of friendStore.friendList) {
    for (const friend of group.friends) {
      total += friend.unreadCount || 0
    }
  }
  return total
})

// ===== 申请数量 =====
const requestCount = computed(() => friendStore.friendRequests.length)

// ===== 移动端底部导航项 =====
const mobileNavItems = computed(() => [
  { key: 'chats', label: '聊天', icon: ChatDotRound, badge: friendUnreadCount.value || null },
  { key: 'requests', label: '申请', icon: Message, badge: requestCount.value || null },
  { key: 'groups', label: '群聊', icon: ChatLineSquare, badge: null },
  { key: 'impressions', label: '印象', icon: Star, badge: null },
])

// ===== 移动端 =====
const sidebarOpen = ref(false)
const isMobile = ref(false)
const MOBILE_BREAKPOINT = 768

const checkMobile = () => {
  isMobile.value = window.innerWidth < MOBILE_BREAKPOINT
  if (!isMobile.value) sidebarOpen.value = false
}

// ===== 聊天列表面板可拖拽 =====
const { sidebarWidth: chatlistWidth, isResizing, startResize } = useResizable({
  minWidth: 280,
  maxWidth: 450,
  defaultWidth: 320,
  storageKey: 'chatlist-width'
})

// ===== 数据加载 =====
const loadGroupList = async () => {
  try {
    const res = await getGroupListApi()
    groupList.value = res
  } catch (error) {
    console.error('加载群聊列表失败', error)
  }
}

const loadFriendListForGroup = async () => {
  const res = await getFriendListApi()
  const friends: FriendVO[] = []
  for (const group of res) {
    friends.push(...group.friends)
  }
  friendListForGroup.value = friends
}

// ===== 聊天选择 =====
const handleSelectChat = (friend: any) => {
  router.push({ name: 'Main', query: { friendId: friend.userId || friend.id } })
  if (isMobile.value) sidebarOpen.value = false
}

const handleSelectGroup = (group: any) => {
  currentGroupId.value = group.id
  group.unreadCount = 0
  router.push({ name: 'Main', query: { groupId: group.id } })
  if (isMobile.value) sidebarOpen.value = false
}

// ===== 好友申请 =====
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

const handleWriteImpression = (userId: number) => {
  impressionTargetUserId.value = userId
  activeNav.value = 'impressions'
}

// ===== 创建群聊 =====
const handleCreateGroup = async (data: { name: string; notice?: string; memberIds: number[] }) => {
  try {
    const newGroup = await createGroupApi(data)
    groupList.value.unshift(newGroup)
    ElMessage.success('群聊创建成功')
    currentGroupId.value = newGroup.id
    router.push({ name: 'Main', query: { groupId: newGroup.id } })
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
    throw error
  }
}

// ===== 路由同步 =====
watch(() => route.query, () => {
  loadGroupList()
  friendStore.loadFriendRequests()
}, { deep: true })

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

// ===== WebSocket 监听 =====
const onGroupMessage = (data: any) => {
  const group = groupList.value.find(g => g.id === data.groupId)
  if (group && currentGroupId.value !== data.groupId) {
    group.unreadCount = (group.unreadCount || 0) + 1
  }
}

const onPrivateMessage = (data: any) => {
  if (currentChatUserId.value !== data.fromUserId) {
    friendStore.incrementUnreadForFriend(data.fromUserId)
  }
}

// ===== 音频解锁 =====
let audioUnlocked = false
const unlockAudio = () => {
  if (audioUnlocked) return
  audioUnlocked = true
  if (navigator.mediaDevices) {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => stream.getTracks().forEach(t => t.stop()))
      .catch(() => {})
  }
  try {
    const ctx = new (window.AudioContext || (window as any).webkitAudioContext)()
    const osc = ctx.createOscillator()
    osc.frequency.value = 1
    osc.connect(ctx.destination)
    osc.start()
    osc.stop(0.001)
  } catch { /* ignore */ }
}

onMounted(() => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
  loadGroupList()
  loadFriendListForGroup()
  checkMobile()
  window.addEventListener('resize', checkMobile)

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

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  width: 100%;
  background: var(--bg-color);
  overflow: hidden;
  position: relative;
}

.main-layout {
  display: flex;
  height: 100%;
  width: 100%;
  gap: 0;
}

.list-panel {
  flex-shrink: 0;
  height: 100%;
  position: relative;
  z-index: 20;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  background: var(--bg-color);
}

.main-layout.is-resizing {
  cursor: col-resize;
  user-select: none;
}

.main-layout.is-resizing .list-panel,
.main-layout.is-resizing .right-panel {
  pointer-events: none;
}

.resize-handle {
  width: 6px;
  flex-shrink: 0;
  cursor: col-resize;
  position: relative;
  z-index: 30;
  background: transparent;
  transition: background 0.2s;
}

.resize-handle:hover,
.main-layout.is-resizing .resize-handle {
  background: var(--color-primary-light-5);
  opacity: 0.4;
}

/* ===== 移动端适配 ===== */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 50;
}

@media (max-width: 768px) {
  .list-panel {
    width: 85% !important;
    max-width: 380px;
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 60;
    transform: translateX(-100%);
    transition: transform 0.3s var(--transition-timing);
  }

  .list-panel.mobile-overlay.show {
    transform: translateX(0);
    box-shadow: 4px 0 30px rgba(0, 0, 0, 0.2);
  }

  .list-panel.mobile-overlay:not(.show) {
    visibility: hidden;
    transition: transform 0.3s var(--transition-timing), visibility 0s 0.3s;
  }

  .list-panel.mobile-overlay.show {
    visibility: visible;
    transition: transform 0.3s var(--transition-timing), visibility 0s;
  }

  .resize-handle {
    display: none;
  }

  .right-panel {
    padding-bottom: 56px; /* 为底部导航留空间 */
  }
}

/* ===== 移动端底部导航栏 ===== */
.mobile-navbar {
  display: none;
}

@media (max-width: 768px) {
  .mobile-navbar {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 56px;
    background: var(--bg-color-white);
    border-top: 1px solid var(--border-color);
    z-index: 100;
    justify-content: space-around;
    align-items: center;
    padding-bottom: env(safe-area-inset-bottom, 0);
  }
}

.mobile-nav-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
  padding: 4px 12px;
  flex: 1;
  min-width: 0;
}

.mobile-nav-btn.active {
  color: var(--color-primary);
}

.mobile-nav-label {
  font-size: 10px;
  line-height: 1.2;
}

.mobile-nav-badge {
  position: absolute;
  top: 2px;
  right: calc(50% - 18px);
  min-width: 14px;
  height: 14px;
  padding: 0 3px;
  background: var(--color-danger);
  color: white;
  font-size: 8px;
  font-weight: 700;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
