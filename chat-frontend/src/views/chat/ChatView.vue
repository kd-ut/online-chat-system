<template>
  <div class="chat-view">
    <ChatWindow v-if="currentChatUser && currentChatUser.userId && !currentGroup" :friend="currentChatUser"
      :key="currentChatUser.userId" />
    <GroupChatWindow v-else-if="currentGroup" :group="currentGroup" :key="currentGroup.id"
      @update:list="refreshGroupList" />
    <div v-else class="empty-chat">
      <div class="empty-bg">
        <div class="empty-orb empty-orb-1"></div>
        <div class="empty-orb empty-orb-2"></div>
        <div class="empty-orb empty-orb-3"></div>
        <div class="empty-orb empty-orb-4"></div>
        <div class="empty-orb empty-orb-5"></div>
      </div>
      <div class="empty-content">
        <div class="empty-icon">
          <svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="50" cy="50" r="44" stroke="#5b6abf" stroke-width="1.5" opacity="0.2" />
            <circle cx="50" cy="50" r="34" stroke="#5b6abf" stroke-width="1" opacity="0.12" stroke-dasharray="6 4" />
            <circle cx="34" cy="40" r="5" fill="#5b6abf" opacity="0.5" />
            <circle cx="66" cy="40" r="5" fill="#5b6abf" opacity="0.5" />
            <path d="M34 56 Q50 72 66 56" stroke="#5b6abf" stroke-width="2" stroke-linecap="round" fill="none" opacity="0.4" />
          </svg>
          <div class="pulse-ring"></div>
          <div class="pulse-ring pulse-ring-2"></div>
        </div>
        <h3 class="empty-title">开始聊天</h3>
        <p class="empty-desc">从左侧选择一个好友或群聊，开启你们的对话吧</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 聊天主视图组件，根据路由参数切换好友聊天或群聊 @component */
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChatWindow from '@/components/message/ChatWindow.vue'
import GroupChatWindow from '@/components/group/GroupChatWindow.vue'
import { useFriendStore } from '@/stores/friendStore'
import { getGroupDetailApi } from '@/api/group'

const route = useRoute()
const router = useRouter()
const friendStore = useFriendStore()

/** 当前聊天好友 */
const currentChatUser = ref<any>(null)
/** 当前群聊 */
const currentGroup = ref<any>(null)

/** 根据 friendId 加载好友信息 @param friendId 好友 ID @returns Promise<void> */
const loadFriendById = async (friendId: number) => {
  if (friendStore.friendList.length === 0) {
    await friendStore.loadFriendList()
  }
  const friend = friendStore.getFriendById(friendId)
  if (friend) {
    currentChatUser.value = friend
    currentGroup.value = null
  } else {
    currentChatUser.value = { userId: friendId, nickname: '好友' }
  }
}

/** 根据 groupId 加载群聊信息 @param groupId 群 ID @returns Promise<void> */
const loadGroupById = async (groupId: number) => {
  try {
    const group = await getGroupDetailApi(groupId)
    currentGroup.value = group
    currentChatUser.value = null
  } catch (error) {
    console.error('加载群聊失败', error)
  }
}

/** 监听路由参数 friendId 变化 */
watch(
  () => route.query.friendId,
  (friendId) => {
    if (friendId) {
      loadFriendById(Number(friendId))
    }
  },
  { immediate: true }
)

/** 监听路由参数 groupId 变化 */
watch(
  () => route.query.groupId,
  (groupId) => {
    if (groupId) {
      loadGroupById(Number(groupId))
    }
  },
  { immediate: true }
)

/** 退出/解散群聊后刷新 @returns void */
const refreshGroupList = () => {
  currentGroup.value = null
  currentChatUser.value = null
  router.push({ query: {} })
}
</script>

<style scoped>
.chat-view {
  height: 100%;
}

.empty-chat {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--bg-color-white);
}

.empty-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.5;
}

.empty-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.25;
  animation: emptyOrb 18s ease-in-out infinite;
}

.empty-orb-1 {
  width: 280px; height: 280px;
  background: var(--color-primary-light);
  top: -12%; left: -10%;
}
.empty-orb-2 {
  width: 220px; height: 220px;
  background: #c4b5fd;
  bottom: -10%; right: -8%;
  animation-delay: -6s;
}
.empty-orb-3 {
  width: 160px; height: 160px;
  background: #a5d8ff;
  top: 50%; left: 50%;
  animation-delay: -12s;
}
.empty-orb-4 {
  width: 140px; height: 140px;
  background: #fecaca;
  top: 15%; right: 20%;
  animation-delay: -9s;
}
.empty-orb-5 {
  width: 180px; height: 180px;
  background: #bbf7d0;
  bottom: 25%; left: 15%;
  animation-delay: -15s;
}

@keyframes emptyOrb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -25px) scale(1.08); }
  66% { transform: translate(-20px, 20px) scale(0.92); }
}

.empty-content {
  position: relative;
  z-index: 1;
  text-align: center;
  animation: emptyFadeIn 0.6s ease;
}
@keyframes emptyFadeIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.empty-icon {
  position: relative;
  width: 100px; height: 100px;
  margin: 0 auto 20px;
}
.empty-icon svg {
  width: 100%; height: 100%;
  position: relative; z-index: 2;
}

.icon-outer-ring { display: none; }
.icon-inner-ring { display: none; }

.pulse-ring {
  position: absolute;
  top: 50%; left: 50%;
  width: 110px; height: 110px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  border: 1.5px solid rgba(91, 106, 191, 0.12);
  animation: pulseRing 3s ease-out infinite;
  z-index: 0;
}
.pulse-ring-2 {
  animation-delay: 1.5s;
}
@keyframes pulseRing {
  0% { transform: translate(-50%, -50%) scale(0.85); opacity: 0.6; }
  100% { transform: translate(-50%, -50%) scale(1.4); opacity: 0; }
}

.empty-title {
  font-size: 18px; font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.empty-desc {
  font-size: 13px; color: var(--text-secondary);
  margin: 0; line-height: 1.6;
}
</style>
