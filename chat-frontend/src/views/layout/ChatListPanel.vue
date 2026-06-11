<template>
  <div class="chatlist-panel">
    <!-- 搜索框 -->
    <div class="panel-search">
      <el-input
        v-model="searchQuery"
        :placeholder="searchPlaceholder"
        :prefix-icon="Search"
        clearable
        size="default"
        class="search-input"
      />
    </div>

    <!-- 内容区域 -->
    <div class="panel-content">
      <!-- 聊天列表 -->
      <ConversationList
        v-show="activeNav === 'chats'"
        :search-query="searchQuery"
        :current-chat-user-id="currentChatUserId"
        @select-chat="handleSelectChat"
        @command="handleConvCommand"
        @write-impression="(uid: number) => $emit('writeImpression', uid)"
      />

      <!-- 好友申请 -->
      <RequestList
        v-show="activeNav === 'requests'"
        :requests="friendStore.friendRequests"
        @agree="(id: number) => $emit('agree', id)"
        @reject="(id: number) => $emit('reject', id)"
      />

      <!-- 群聊列表 -->
      <GroupList
        v-show="activeNav === 'groups'"
        :groups="groups"
        :current-group-id="currentGroupId"
        @select="$emit('selectGroup', $event)"
        @create="$emit('createGroup')"
      />

      <!-- 印象面板 -->
      <ImpressionBoard
        v-show="activeNav === 'impressions'"
        :target-user-id="impressionTargetUserId"
        @clear-target="$emit('clearImpressionTarget')"
      />

      <!-- 管理中心入口 -->
      <div v-if="activeNav === 'admin'" class="admin-entry">
        <el-empty description="管理后台" :image-size="48" />
        <el-button type="primary" @click="goToAdmin">进入管理后台</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 中间面板容器，根据 activeNav 切换聊天/申请/群聊/印象内容 @component */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useFriendStore } from '@/stores/friendStore'
import type { GroupVO } from '@/api/group'
import ConversationList from './ConversationList.vue'
import RequestList from './sidebar/RequestList.vue'
import GroupList from './sidebar/GroupList.vue'
import ImpressionBoard from '@/components/impression/ImpressionBoard.vue'

const props = defineProps<{
  activeNav: string
  currentChatUserId: number | null
  currentGroupId: number | null
  groups: GroupVO[]
  impressionTargetUserId: number | null
}>()

const emit = defineEmits<{
  (e: 'selectChat', friend: any): void
  (e: 'selectGroup', group: any): void
  (e: 'agree', requestId: number): void
  (e: 'reject', requestId: number): void
  (e: 'writeImpression', userId: number): void
  (e: 'createGroup'): void
  (e: 'clearImpressionTarget'): void
}>()

const router = useRouter()
const friendStore = useFriendStore()
const searchQuery = ref('')

const searchPlaceholder = computed(() => {
  const map: Record<string, string> = {
    chats: '搜索联系人',
    requests: '搜索申请',
    groups: '搜索群聊',
    impressions: '搜索印象',
  }
  return map[props.activeNav] || '搜索'
})

const handleSelectChat = (conv: any) => {
  emit('selectChat', {
    id: conv.userId,
    userId: conv.userId,
    nickname: conv.nickname,
    avatar: conv.avatar,
    remark: conv.remark,
    isOnline: conv.isOnline,
  })
}

const handleConvCommand = (command: string, friend: any) => {
  // 委托给父组件处理，与旧 FriendItem 行为一致
  console.log('conv command:', command, friend)
}

const goToAdmin = () => {
  router.push('/admin')
}
</script>

<style scoped>
.chatlist-panel {
  width: var(--chatlist-width);
  height: 100%;
  background: var(--bg-color-white);
  border-right: 1px solid var(--border-color-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.panel-search {
  padding: 12px;
  border-bottom: 1px solid var(--border-color-light);
  flex-shrink: 0;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-color);
  border-radius: var(--border-radius-small);
  border: none;
  box-shadow: none;
  padding-left: 8px;
  padding-right: 8px;
  height: 34px;
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  background: var(--bg-color-sunken);
  box-shadow: 0 0 0 1px var(--color-primary-light-5) inset;
}

.panel-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.admin-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
  gap: 12px;
}
</style>
