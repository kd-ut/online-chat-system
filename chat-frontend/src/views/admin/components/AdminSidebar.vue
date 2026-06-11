<template>
  <div class="admin-sidebar" :class="{ collapsed: isCollapsed }">
    <div class="logo">
      <div class="logo-icon-wrap">
        <svg class="logo-svg" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="var(--color-primary)" />
          <path d="M8 22V12L16 18L24 12V22" stroke="white" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" fill="none" />
        </svg>
      </div>
      <div v-show="!isCollapsed" class="logo-text">
        <span class="logo-title">管理后台</span>
        <span class="logo-sub">Admin</span>
      </div>
    </div>

    <el-menu :default-active="activeMenu" class="sidebar-menu" :collapse="isCollapsed" @select="handleSelect">
      <el-menu-item index="stats">
        <el-icon><DataLine /></el-icon>
        <template #title>数据统计</template>
      </el-menu-item>
      <el-menu-item index="users">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>
      <el-menu-item index="messages">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>消息审计</template>
      </el-menu-item>
      <el-menu-item index="notifications">
        <el-icon><Bell /></el-icon>
        <template #title>系统通知</template>
      </el-menu-item>
    </el-menu>

    <div class="sidebar-footer">
      <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
        <el-icon :size="16"><DArrowLeft v-if="!isCollapsed" /><DArrowRight v-else /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 管理后台侧边栏导航组件 @component */
import { ref } from 'vue'
import { DataLine, User, ChatDotRound, Bell, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'

defineProps<{ activeMenu: string }>()

const emit = defineEmits<{ select: [menu: string] }>()

/** 侧边栏折叠状态 */
const isCollapsed = ref(false)

const handleSelect = (index: string) => {
  emit('select', index)
}
</script>

<style scoped>
.admin-sidebar {
  width: 240px;
  height: 100vh;
  background: var(--bg-color-white);
  border-right: 1px solid var(--border-color-light);
  position: fixed;
  left: 0;
  top: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.03);
  transition: width 0.25s var(--transition-timing);
  z-index: 100;
}

.admin-sidebar.collapsed {
  width: 72px;
}

.logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid var(--border-color-light);
  overflow: hidden;
}

.logo-icon-wrap {
  flex-shrink: 0;
}

.logo-svg {
  width: 32px;
  height: 32px;
  display: block;
}

.logo-text {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
}

.logo-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.logo-sub {
  font-size: 10px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 1.5px;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  padding: 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.admin-sidebar:not(.collapsed) .sidebar-menu {
  width: 100%;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 10px;
  margin: 2px 0;
  height: 46px;
  line-height: 46px;
  font-weight: 500;
  transition: all 0.2s var(--transition-timing);
  position: relative;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--color-primary) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.25);
}

.sidebar-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--color-primary);
  border-radius: 0 3px 3px 0;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: var(--color-primary-light-1);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

.sidebar-footer {
  padding: 10px;
  border-top: 1px solid var(--border-color-light);
  display: flex;
  justify-content: center;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid var(--border-color-light);
  background: var(--bg-color-white);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.2s var(--transition-timing);
}

.collapse-btn:hover {
  background: var(--color-primary-light-1);
  color: var(--color-primary);
  border-color: var(--color-primary-light-5);
}
</style>
