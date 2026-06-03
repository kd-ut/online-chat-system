<template>
  <div class="chat-header">
    <div class="group-info">
      <el-avatar :size="40" :src="group?.avatar || ''">
        {{ group?.name?.charAt(0) || '群' }}
      </el-avatar>
      <div class="group-detail">
        <div class="name">{{ group?.name }}</div>
        <div class="member-count">{{ group?.memberCount }}人</div>
      </div>
    </div>
    <div class="actions">
      <el-dropdown @command="$emit('command', $event)">
        <el-button :icon="Setting" circle text />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="notice" v-if="canEditNotice">编辑公告</el-dropdown-item>
            <el-dropdown-item command="viewNotice" v-else>群公告</el-dropdown-item>
            <el-dropdown-item command="members">群成员</el-dropdown-item>
            <el-dropdown-item command="invite">邀请好友</el-dropdown-item>
            <el-dropdown-item command="manage" v-if="canEditNotice" divided>群管理</el-dropdown-item>
            <el-dropdown-item command="quit" divided v-if="!isOwner">退出群聊</el-dropdown-item>
            <el-dropdown-item command="disband" divided v-if="isOwner" style="color: #f56c6c">解散群聊</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 群聊聊天窗口头部组件 @component */
import { Setting } from '@element-plus/icons-vue'

/** 组件属性：群信息、是否可以编辑公告、是否为群主 */
defineProps<{
  group: any
  canEditNotice: boolean
  isOwner: boolean
}>()

/** 组件事件：菜单命令 */
defineEmits<{
  (e: 'command', command: string): void
}>()
</script>

<style scoped>
.chat-header {
  padding: 0 20px;
  height: var(--header-height);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-color-white);
  flex-shrink: 0;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.group-detail .name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.group-detail .member-count {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 400;
}
</style>
