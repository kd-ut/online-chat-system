<template>
  <div class="friend-item" :class="{ active: isActive }" @click="$emit('click')">
    <div class="avatar">
      <MiniProfile :user-id="friend.userId" @start-chat="emit('click')" @write-impression="emit('writeImpression', $event)">
        <el-avatar :size="40" :src="friend.avatar || ''">
          {{ friend.nickname?.charAt(0) || 'U' }}
        </el-avatar>
      </MiniProfile>
      <span class="online-dot" :class="{ online: friend.isOnline }" />
    </div>

    <div class="friend-info">
      <div class="name">{{ friend.remark || friend.nickname }}</div>
      <div class="message">{{ friend.signature || '这个人很懒，什么都没写' }}</div>
    </div>

    <span v-if="friend.unreadCount > 0" class="unread-badge">
      {{ friend.unreadCount > 99 ? '99+' : friend.unreadCount }}
    </span>

    <el-dropdown trigger="click" @command="handleCommand">
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
/** 好友列表项组件，展示头像/名称/在线状态/未读消息和操作菜单 @component */
import { MoreFilled } from '@element-plus/icons-vue'
import MiniProfile from '@/components/user/MiniProfile.vue'

/** 组件属性：好友信息、是否激活 */
const props = defineProps<{
  friend: any
  isActive: boolean
}>()

/** 组件事件：点击、操作命令、写印象 */
const emit = defineEmits<{
  (e: 'click'): void
  (e: 'command', command: string, friend: any): void
  (e: 'writeImpression', userId: number): void
}>()

/** 处理下拉菜单命令 @param command 命令标识 @returns void */
const handleCommand = (command: string) => {
  emit('command', command, props.friend)
}
</script>

<style scoped>
.friend-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
  margin: 0 4px;
  border-radius: var(--border-radius-small);
}

.friend-item:hover {
  background: var(--bg-color);
}

.friend-item.active {
  background: #eef0ff;
}

.avatar {
  position: relative;
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar :deep(.el-avatar) {
  border: 2px solid transparent;
}

.friend-item.active .avatar :deep(.el-avatar) {
  border-color: var(--color-primary-light);
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--text-placeholder);
  border: 2px solid var(--bg-color-white);
}
.online-dot.online {
  background: var(--color-success);
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 2px;
  color: var(--text-primary);
}

.friend-item.active .name {
  font-weight: 600;
}

.message {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
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
  margin-left: 8px;
  flex-shrink: 0;
  line-height: 1;
}
</style>
