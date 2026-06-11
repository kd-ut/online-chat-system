<template>
  <div class="conversation-list">
    <!-- 空状态 -->
    <div v-if="filtered.length === 0" class="conv-empty">
      <el-empty :description="searchQuery ? '未找到匹配的联系人' : '暂无好友'" :image-size="48" />
      <el-button v-if="!searchQuery" type="primary" size="small" text @click="showAddFriend = true">
        添加好友
      </el-button>
    </div>

    <!-- 会话列表 -->
    <template v-else>
      <ConversationItem
        v-for="conv in filtered"
        :key="conv.userId"
        :conversation="conv"
        :is-active="conv.userId === currentChatUserId"
        @click="$emit('selectChat', conv)"
        @command="handleCommand"
        @write-impression="(uid: number) => $emit('writeImpression', uid)"
      />
    </template>

    <!-- 添加好友对话框 -->
    <AddFriendDialog v-model="showAddFriend" />
  </div>
</template>

<script setup lang="ts">
/** 平铺会话列表，从 friendStore 扁平化，按在线+未读排序 @component */
import { ref, computed } from 'vue'
import { useFriendStore } from '@/stores/friendStore'
import ConversationItem from './ConversationItem.vue'
import type { ConversationData } from './ConversationItem.vue'
import AddFriendDialog from '@/components/friend/AddFriendDialog.vue'

const props = defineProps<{
  searchQuery: string
  currentChatUserId: number | null
}>()

const emit = defineEmits<{
  (e: 'selectChat', friend: any): void
  (e: 'command', command: string, friend: any): void
  (e: 'writeImpression', userId: number): void
}>()

const friendStore = useFriendStore()
const showAddFriend = ref(false)

/** 扁平化所有好友为一个会话列表 */
const conversations = computed<ConversationData[]>(() => {
  const list: ConversationData[] = []
  for (const group of friendStore.friendList) {
    for (const friend of group.friends) {
      list.push({
        id: friend.id,
        userId: friend.userId || friend.id,
        nickname: friend.nickname || '',
        avatar: friend.avatar || null,
        remark: friend.remark || null,
        groupName: friend.groupName || '',
        isOnline: friend.isOnline || false,
        unreadCount: friend.unreadCount || 0,
        signature: friend.signature || null,
        lastMessage: '',
        lastMessageTime: '',
      })
    }
  }
  // 排序：在线优先 → 未读降序 → 名称
  list.sort((a, b) => {
    if (a.isOnline !== b.isOnline) return a.isOnline ? -1 : 1
    if (a.unreadCount !== b.unreadCount) return b.unreadCount - a.unreadCount
    return (a.remark || a.nickname).localeCompare(b.remark || b.nickname)
  })
  return list
})

/** 搜索过滤 */
const filtered = computed(() => {
  const kw = props.searchQuery.toLowerCase()
  if (!kw) return conversations.value
  return conversations.value.filter(c => {
    const name = (c.remark || c.nickname).toLowerCase()
    return name.includes(kw)
  })
})

const handleCommand = (command: string, friend: any) => {
  emit('command', command, friend)
}
</script>

<style scoped>
.conversation-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.conversation-list::-webkit-scrollbar {
  width: 4px;
}

.conversation-list::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 4px;
}

.conversation-list:hover::-webkit-scrollbar-thumb {
  background: var(--text-placeholder);
}

.conv-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
  gap: 8px;
}
</style>
