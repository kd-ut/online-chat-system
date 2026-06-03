<template>
  <div class="chat-header">
    <div class="friend-info">
      <el-avatar :size="40" :src="friend?.avatar || ''">
        {{ friend?.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="friend-detail">
        <div class="name">{{ friend?.remark || friend?.nickname }}</div>
        <div class="status">
          <span class="status-dot" :class="{ online: friend?.isOnline }"></span>
          {{ friend?.isOnline ? '在线' : '离线' }}
        </div>
      </div>
    </div>
    <div class="actions">
      <el-tooltip content="下载聊天记录" placement="bottom">
        <el-button class="download-trigger" @click="$emit('download')">
          <el-icon :size="18"><Download /></el-icon>
          <span>下载</span>
        </el-button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 聊天窗口头部组件，展示好友信息和操作按钮 @component */
import { Download } from '@element-plus/icons-vue'

/** 组件属性：好友信息 */
defineProps<{ friend: any }>()
/** 组件事件：下载聊天记录 */
defineEmits<{ (e: 'download'): void }>()
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
  position: relative;
  z-index: 5;
  flex-shrink: 0;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.friend-detail .name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.friend-detail .status {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--text-placeholder);
}
.status-dot.online {
  background: var(--color-success);
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.download-trigger {
  height: 34px;
  padding: 0 14px !important;
  border-radius: var(--border-radius-small) !important;
  font-size: 12px !important;
  font-weight: 500 !important;
  gap: 5px;
  border: 1px solid var(--border-color) !important;
  color: var(--text-regular) !important;
  transition: all 0.15s !important;
}

.download-trigger:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
  background: #f5f6ff !important;
}

.download-trigger:active {
  transform: scale(0.97);
}
</style>
