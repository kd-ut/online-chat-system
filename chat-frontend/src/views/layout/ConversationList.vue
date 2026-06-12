<template>
  <div class="conversation-list">
    <!-- 空状态 -->
    <div v-if="friendStore.friendList.length === 0" class="conv-empty">
      <el-empty description="暂无好友" :image-size="48" />
      <el-button type="primary" size="small" text @click="showAddFriend = true">
        添加好友
      </el-button>
    </div>

    <!-- 分组列表 -->
    <template v-else>
      <div v-for="group in groupedList" :key="group.groupKey" class="conv-group">
        <!-- 分组标题（可点击折叠/展开） -->
        <div class="group-header" @click="toggleGroup(group.groupKey)">
          <el-icon class="group-arrow" :class="{ expanded: !collapsedGroups.has(group.groupKey) }">
            <ArrowRight />
          </el-icon>
          <span class="group-name">{{ group.groupName }}</span>
          <span class="group-count">{{ group.friends.length }}</span>
          <span v-if="group.totalUnread > 0" class="group-badge">
            {{ group.totalUnread > 99 ? '99+' : group.totalUnread }}
          </span>
        </div>

        <!-- 分组下的好友列表 -->
        <div v-show="!collapsedGroups.has(group.groupKey)" class="group-friends">
          <ConversationItem
            v-for="conv in group.friends"
            :key="conv.userId"
            :conversation="conv"
            :is-active="conv.userId === currentChatUserId"
            @click="$emit('selectChat', conv)"
            @command="handleCommand"
            @write-impression="(uid: number) => $emit('writeImpression', uid)"
          />
          <div v-if="group.friends.length === 0" class="group-empty">
            此分组暂无好友
          </div>
        </div>
      </div>
    </template>

    <!-- 添加好友对话框 -->
    <AddFriendDialog v-model="showAddFriend" />
  </div>
</template>

<script setup lang="ts">
/** 分组折叠会话列表，展示分组标题+可折叠好友列表 @component */
import { ref, computed, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
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

/** 已折叠的分组 key 集合 */
const collapsedGroups = ref<Set<string>>(new Set())

/** 从 friendStore 按分组构建数据 */
const groupedList = computed(() => {
  const kw = props.searchQuery.toLowerCase()

  return friendStore.friendList.map(group => {
    const groupName = group.groupName || ' '
    const groupKey = String(group.groupId || '__default__')

    let friends: ConversationData[] = []

    for (const friend of group.friends) {
      const conv: ConversationData = {
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
      }

      // 搜索过滤
      if (kw) {
        const name = (conv.remark || conv.nickname).toLowerCase()
        if (!name.includes(kw)) continue
      }

      friends.push(conv)
    }

    // 分组内排序：在线优先 → 未读降序 → 名称
    friends.sort((a, b) => {
      if (a.isOnline !== b.isOnline) return a.isOnline ? -1 : 1
      if (a.unreadCount !== b.unreadCount) return b.unreadCount - a.unreadCount
      return (a.remark || a.nickname).localeCompare(b.remark || b.nickname)
    })

    const totalUnread = friends.reduce((sum, f) => sum + f.unreadCount, 0)

    return { groupKey, groupName, friends, totalUnread }
  })
})

/** 搜索时自动展开所有含匹配好友的分组 */
watch(() => props.searchQuery, (kw) => {
  if (kw) {
    // 搜索时全展开
    collapsedGroups.value = new Set()
  } else {
    // 清空搜索时，折叠没有未读消息的分组
    initCollapsedState()
  }
})

/** 初始折叠状态：有未读消息的分组展开，其余折叠 */
const initCollapsedState = () => {
  const collapsed = new Set<string>()
  for (const g of groupedList.value) {
    if (g.totalUnread === 0) {
      collapsed.add(g.groupKey)
    }
  }
  collapsedGroups.value = collapsed
}

// 初始加载时设置折叠状态
watch(() => friendStore.friendList, () => {
  if (collapsedGroups.value.size === 0) {
    initCollapsedState()
  }
}, { deep: true, immediate: true })

const toggleGroup = (groupKey: string) => {
  const next = new Set(collapsedGroups.value)
  if (next.has(groupKey)) {
    next.delete(groupKey)
  } else {
    next.add(groupKey)
  }
  collapsedGroups.value = next
}

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

/* ===== 分组标题 ===== */
.group-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  cursor: pointer;
  user-select: none;
  background: var(--bg-color-sunken);
  border-bottom: 1px solid var(--border-color-lighter);
  position: sticky;
  top: 0;
  z-index: 5;
  transition: background 0.15s;
}

.group-header:hover {
  background: var(--bg-color);
}

.group-arrow {
  font-size: 12px;
  color: var(--text-secondary);
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.group-arrow.expanded {
  transform: rotate(90deg);
}

.group-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-regular);
  flex: 1;
}

.group-count {
  font-size: 11px;
  color: var(--text-placeholder);
}

.group-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--color-danger);
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

/* ===== 分组下的好友 ===== */
.group-friends {
  overflow: hidden;
}

.group-empty {
  padding: 12px;
  text-align: center;
  font-size: 12px;
  color: var(--text-placeholder);
}
</style>
