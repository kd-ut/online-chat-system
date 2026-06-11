<template>
  <div class="header">
    <div class="logo">
      <button class="hamburger" @click="emit('toggleSidebar')">
        <el-icon :size="20"><Expand /></el-icon>
      </button>
      <span>快来聊天吧</span>
    </div>

    <div class="actions">
      <ThemeToggle />

      <el-badge :value="messageStore.unreadCount?.total || 0" :hidden="!messageStore.unreadCount?.total" class="bell-badge">
        <el-button circle @click="showMessageBox = true" class="bell-btn">
          <el-icon :size="20"><Bell /></el-icon>
        </el-button>
      </el-badge>

      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="36" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <span>{{ userStore.userInfo?.nickname }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item v-if="userStore.isAdmin()" command="admin">管理后台</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <MessageBox v-model="showMessageBox" />
    <ConfirmDialog v-model="showLogoutConfirm" title="退出登录" message="确定要退出登录吗？"
      type="warning" confirm-text="退出" cancel-text="取消" @confirm="confirmLogout" />
  </div>
</template>

<script setup lang="ts">
/** 主布局头部组件，显示未读角标、用户菜单和消息盒子 @component */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Expand } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'
import { websocketService } from '@/utils/websocket'
import MessageBox from '@/components/message/MessageBox.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import ThemeToggle from '@/components/common/ThemeToggle.vue'

const emit = defineEmits<{ toggleSidebar: [] }>()

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
/** 消息盒子显示状态 */
const showMessageBox = ref(false)
/** 退出确认对话框显示状态 */
const showLogoutConfirm = ref(false)

/** 处理用户下拉菜单命令 @param command 命令标识 @returns Promise<void> */
const handleCommand = async (command: string) => {
  if (command === 'logout') {
    showLogoutConfirm.value = true
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}

/** 确认退出登录 @returns void */
const confirmLogout = () => {
  userStore.logout()
  router.push('/login')
}

/** 通知提示音 URL（缓存） */
let _notifSoundUrl: string | null = null
/** 有系统通知时触发（仅系统通知使用统一铃铛提醒，好友消息通过侧边栏红点提示） */
const onSystemNotification = () => {
  messageStore.loadUnreadCount()
  if (_notifSoundUrl === null) {
    try { _notifSoundUrl = new URL('../../assets/audio/notice.MP3', import.meta.url).href }
    catch { _notifSoundUrl = '' }
  }
  if (!_notifSoundUrl) return
  try {
    const a = new Audio(_notifSoundUrl)
    a.volume = 0.3
    a.play().catch(() => {})
  } catch { /* ignore */ }
}

onMounted(() => {
  messageStore.loadUnreadCount()
  websocketService.onNotification(onSystemNotification)
})
</script>

<style scoped>
.header {
  height: var(--header-height);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-color-white);
  flex-shrink: 0;
  position: relative;
  z-index: 10;
  border-bottom: 1px solid var(--border-color);
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}
.hamburger {
  display: none;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: var(--text-regular);
  cursor: pointer;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.hamburger:hover {
  background: var(--bg-color);
  color: var(--color-primary);
}
@media (max-width: 768px) {
  .hamburger { display: flex; }
}
.logo span {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}
.actions { display: flex; align-items: center; gap: 16px; }
.user-info {
  display: flex; align-items: center; gap: 10px; cursor: pointer;
  padding: 6px 14px; border-radius: 8px; transition: all 0.2s;
}
.user-info:hover {
  background: var(--bg-color);
}
.user-info span { font-size: 14px; font-weight: 600; color: var(--text-primary); }

.bell-badge :deep(.el-badge__content) {
  border: 2px solid var(--bg-color-white) !important;
}

.bell-btn {
  width: 38px !important;
  height: 38px !important;
  border-radius: var(--border-radius-small) !important;
  background: var(--bg-color) !important;
  border: 1px solid transparent !important;
  font-size: 18px;
  transition: all 0.2s !important;
  color: var(--text-regular);
}

.bell-btn:hover {
  background: #f0f1ff !important;
  color: var(--color-primary) !important;
}

.bell-btn:active {
  transform: scale(0.95) !important;
}
</style>
