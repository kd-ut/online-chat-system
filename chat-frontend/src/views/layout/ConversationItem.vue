<template>
  <div class="conv-item" :class="{ active: isActive }" @click="$emit('click')">
    <div class="conv-avatar">
      <MiniProfile :user-id="conversation.userId" @start-chat="$emit('click')" @write-impression="$emit('writeImpression', $event)">
        <el-avatar :size="40" :src="conversation.avatar || ''">
          {{ (conversation.nickname || 'U').charAt(0) }}
        </el-avatar>
      </MiniProfile>
      <span class="online-dot" :class="{ online: conversation.isOnline }" />
    </div>

    <div class="conv-info">
      <div class="conv-top">
        <span class="conv-name">{{ conversation.remark || conversation.nickname }}</span>
        <span class="conv-time" v-if="conversation.lastMessageTime">{{ conversation.lastMessageTime }}</span>
      </div>
      <div class="conv-bottom">
        <span class="conv-preview">{{ conversation.lastMessage || conversation.signature || '' }}</span>
        <span v-if="conversation.unreadCount > 0" class="conv-badge">
          {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
        </span>
      </div>
    </div>

    <el-dropdown trigger="click" @command="handleCommand" class="conv-menu">
      <el-button :icon="MoreFilled" size="small" text @click.stop />
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="remark">修改备注</el-dropdown-item>
          <el-dropdown-item command="move">移动分组</el-dropdown-item>
          <el-dropdown-item command="delete" divided>删除好友</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
/** 会话列表条目，展示头像/名称/最近消息/时间/未读红点 @component */
import { MoreFilled } from '@element-plus/icons-vue'
import MiniProfile from '@/components/user/MiniProfile.vue'

export interface ConversationData {
  userId: number
  nickname: string
  avatar: string | null
  remark: string | null
  isOnline: boolean
  unreadCount: number
  signature: string | null
  lastMessage: string
  lastMessageTime: string
}

const props = defineProps<{
  conversation: ConversationData
  isActive: boolean
}>()

const emit = defineEmits<{
  (e: 'click'): void
  (e: 'command', command: string, friend: any): void
  (e: 'writeImpression', userId: number): void
}>()

const handleCommand = (command: string) => {
  emit('command', command, props.conversation)
}
</script>

<style scoped>
.conv-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.12s ease;
  position: relative;
  border-radius: 0;
  gap: 10px;
  height: 60px;
}

.conv-item:hover {
  background: var(--bg-color);
}

.conv-item.active {
  background: var(--color-primary-light-1);
}

.conv-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--color-primary);
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.conv-avatar :deep(.el-avatar) {
  border: 2px solid transparent;
}

.conv-item.active .conv-avatar :deep(.el-avatar) {
  border-color: var(--color-primary-light-5);
}

.online-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--text-placeholder);
  border: 2px solid var(--bg-color-white);
}

.conv-item.active .online-dot {
  border-color: var(--color-primary-light-1);
}

.online-dot.online {
  background: var(--color-success);
}

.conv-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conv-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.conv-item.active .conv-name {
  font-weight: 600;
}

.conv-time {
  font-size: 11px;
  color: var(--text-placeholder);
  flex-shrink: 0;
  margin-left: 8px;
}

.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conv-preview {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.conv-badge {
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
  flex-shrink: 0;
  line-height: 1;
  margin-left: 4px;
}

.conv-menu {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.15s;
}

.conv-item:hover .conv-menu {
  opacity: 1;
}
</style>
