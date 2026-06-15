<template>
  <div class="navbar">
    <!-- 顶部 logo -->
    <div class="nav-logo">
      <svg viewBox="0 0 32 32" width="28" height="28" fill="none">
        <circle cx="16" cy="16" r="14" fill="var(--color-primary)" opacity="0.12" />
        <path d="M10 22V12l6 8 6-8v10" stroke="var(--color-primary)" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </div>

    <!-- 导航图标 -->
    <div class="nav-icons">
      <el-tooltip v-for="item in navItems" :key="item.key" :content="item.label" placement="right" :show-after="400">
        <button class="nav-btn" :class="{ active: modelValue === item.key }" @click="$emit('update:modelValue', item.key)">
          <el-icon :size="20">
            <component :is="item.icon" />
          </el-icon>
          <span v-if="item.badge" class="nav-badge" :class="{ dot: item.badgeDot }">
            <template v-if="!item.badgeDot">{{ item.badge }}</template>
          </span>
        </button>
      </el-tooltip>
    </div>

    <!-- 底部区域 -->
    <div class="nav-bottom">
      <!-- 主题切换 -->
      <el-tooltip content="切换主题" placement="right" :show-after="400">
        <div class="nav-theme">
          <ThemeToggle />
        </div>
      </el-tooltip>

      <!-- 用户头像 -->
      <el-dropdown trigger="click" @command="handleCommand" placement="right-end">
        <button class="nav-avatar-btn">
          <el-avatar :size="28" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> 个人信息
            </el-dropdown-item>
            <el-dropdown-item v-if="userStore.isAdmin?.()" command="admin" divided>
              <el-icon><Setting /></el-icon> 管理后台
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 左侧垂直图标导航栏，纯图标无文字标签 @component */
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Message, ChatLineSquare, Star, Setting, User, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useFriendStore } from '@/stores/friendStore'
import ThemeToggle from '@/components/common/ThemeToggle.vue'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const router = useRouter()
const userStore = useUserStore()
const friendStore = useFriendStore()

/** 好友未读数 */
const friendUnreadCount = computed(() => {
  let total = 0
  for (const group of friendStore.friendList) {
    for (const friend of group.friends) {
      total += friend.unreadCount || 0
    }
  }
  return total
})

/** 申请数量 */
const requestCount = computed(() => friendStore.friendRequests.length)

interface NavItem {
  key: string
  label: string
  icon: any
  badge: string | number
  badgeDot: boolean
}

const navItems = computed<NavItem[]>(() => {
  const items: NavItem[] = [
    { key: 'chats', label: '聊天', icon: ChatDotRound, badge: friendUnreadCount.value, badgeDot: false },
    { key: 'requests', label: '申请', icon: Message, badge: requestCount.value, badgeDot: requestCount.value === 0 },
    { key: 'groups', label: '群聊', icon: ChatLineSquare, badge: '', badgeDot: false },
    { key: 'impressions', label: '印象', icon: Star, badge: '', badgeDot: false },
  ]
  if (userStore.isAdmin?.()) {
    items.push({ key: 'admin', label: '管理', icon: Setting, badge: '', badgeDot: false })
  }
  return items
})

const handleCommand = (command: string) => {
  if (command === 'profile') router.push('/profile')
  else if (command === 'admin') router.push('/admin')
  else if (command === 'logout') {
    userStore.logout?.()
    router.push('/login')
  }
}
</script>

<style scoped>
.navbar {
  width: var(--navbar-width);
  height: 100%;
  background: var(--bg-color-sunken);
  border-right: 1px solid var(--border-color-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  padding: 0;
  z-index: 30;
}

.nav-logo {
  padding: 16px 0 12px;
  display: flex;
  justify-content: center;
}

.nav-icons {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
  width: 100%;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border-radius: var(--border-radius-small);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.15s ease;
}

.nav-btn:hover {
  background: var(--color-primary-light-1);
  color: var(--color-primary);
}

.nav-btn.active {
  background: var(--color-primary-light-1);
  color: var(--color-primary);
}

.nav-btn.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  border-radius: 0 3px 3px 0;
  background: var(--color-primary);
}

.nav-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--color-danger);
  color: white;
  font-size: 9px;
  font-weight: 700;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.nav-badge.dot {
  min-width: 6px;
  width: 6px;
  height: 6px;
  padding: 0;
  border-radius: 50%;
  top: 6px;
  right: 6px;
}

.nav-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 0 12px;
  width: 100%;
  border-top: 1px solid var(--border-color-light);
  margin-top: auto;
}

.nav-theme {
  cursor: pointer;
}

.nav-avatar-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
  border-radius: var(--border-radius-circle);
  transition: box-shadow 0.15s;
}

.nav-avatar-btn:hover {
  box-shadow: 0 0 0 2px var(--color-primary-light-5);
}

/* 移动端：隐藏导航栏（底部 tab 在 MainLayout 中处理） */
@media (max-width: 768px) {
  .navbar {
    display: none;
  }
}
</style>
