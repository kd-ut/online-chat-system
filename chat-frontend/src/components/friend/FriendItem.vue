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
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  margin: 2px 8px;
  border-radius: 14px;
}

.friend-item:hover {
  background: #f3f0ff;
  transform: translateX(2px);
}

.friend-item.active {
  background: linear-gradient(135deg, #f3f0ff, #e8e3ff);
  box-shadow: 0 2px 8px rgba(108, 92, 231, 0.08);
}

.avatar {
  position: relative;
  margin-right: 14px;
}

.avatar :deep(.el-avatar) {
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.friend-item.active .avatar :deep(.el-avatar) {
  border-color: var(--color-primary-light);
}

.online-dot {
  position: absolute;
  bottom: 3px;
  right: 3px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--text-secondary);
  border: 2px solid var(--bg-color-white);
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.online-dot.online {
  background: var(--color-success);
  box-shadow: 0 0 6px rgba(0, 184, 148, 0.4);
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 3px;
  color: var(--text-primary);
}

.friend-item.active .name {
  color: var(--color-primary);
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
  box-shadow: 0 2px 6px rgba(255, 118, 117, 0.35);
}
</style>
