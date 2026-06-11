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
      <el-button
        v-if="activeNav === 'chats'"
        class="add-friend-btn"
        :icon="Plus"
        size="small"
        text
        @click="showAddFriend = true"
        title="添加好友"
      />
      <el-button
        v-if="activeNav === 'chats'"
        class="manage-group-btn"
        :icon="Setting"
        size="small"
        text
        @click="showGroupManage = true"
        title="管理分组"
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

    <!-- 添加好友对话框 -->
    <AddFriendDialog v-model="showAddFriend" @success="refreshFriendList" />

    <!-- 修改备注对话框 -->
    <EditRemarkDialog v-model="showEditRemark" :friend="selectedFriend" @success="refreshFriendList" />

    <!-- 移动分组对话框 -->
    <MoveGroupDialog v-model="showMoveGroup" :friend="selectedFriend" @success="refreshFriendList" />

    <!-- 管理分组对话框 -->
    <GroupManageDialog v-model="showGroupManage" @changed="refreshFriendList" />
  </div>
</template>

<script setup lang="ts">
/** 中间面板容器，根据 activeNav 切换聊天/申请/群聊/印象内容 @component */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Search, Plus, Setting } from '@element-plus/icons-vue'
import { useFriendStore } from '@/stores/friendStore'
import { deleteFriendApi } from '@/api/friend'
import type { GroupVO } from '@/api/group'
import ConversationList from './ConversationList.vue'
import RequestList from './sidebar/RequestList.vue'
import GroupList from './sidebar/GroupList.vue'
import ImpressionBoard from '@/components/impression/ImpressionBoard.vue'
import AddFriendDialog from '@/components/friend/AddFriendDialog.vue'
import EditRemarkDialog from '@/components/friend/EditRemarkDialog.vue'
import MoveGroupDialog from '@/components/friend/MoveGroupDialog.vue'
import GroupManageDialog from '@/components/friend/GroupManageDialog.vue'

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

/** 添加好友对话框 */
const showAddFriend = ref(false)
/** 修改备注对话框 */
const showEditRemark = ref(false)
/** 移动分组对话框 */
const showMoveGroup = ref(false)
/** 管理分组对话框 */
const showGroupManage = ref(false)
/** 当前选中的好友（用于备注/移动/删除操作） */
const selectedFriend = ref<{ id: number; nickname: string; avatar: string | null; remark: string | null; groupName?: string } | null>(null)

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
  selectedFriend.value = {
    id: friend.id,
    nickname: friend.nickname,
    avatar: friend.avatar,
    remark: friend.remark,
    groupName: friend.groupName
  }

  switch (command) {
    case 'remark':
      showEditRemark.value = true
      break
    case 'move':
      showMoveGroup.value = true
      break
    case 'delete':
      ElMessageBox.confirm(
        `确定要删除好友「${friend.remark || friend.nickname}」吗？删除后将无法恢复。`,
        '删除好友',
        { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
      ).then(async () => {
        try {
          await deleteFriendApi(friend.id)
          ElMessage.success('已删除好友')
          refreshFriendList()
        } catch (e: any) {
          ElMessage.error(e?.message || '删除失败')
        }
      }).catch(() => { /* 取消 */ })
      break
  }
}

const refreshFriendList = () => {
  friendStore.loadFriendList()
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
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-color-light);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-input {
  flex: 1;
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

.add-friend-btn,
.manage-group-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  color: var(--text-secondary);
}

.add-friend-btn:hover,
.manage-group-btn:hover {
  background: var(--bg-color-sunken);
  color: var(--color-primary);
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
