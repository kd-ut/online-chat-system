<template>
  <div class="sidebar-tabs">
    <div v-for="tab in visibleTabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }"
      @click="handleTabClick(tab)">
      <el-icon>
        <component :is="tab.icon" />
      </el-icon>
      <span>{{ tab.label }}</span>
      <span v-if="tab.badge && badgeCount > 0" class="badge">
        {{ badgeCount }}
      </span>
      <span v-if="tab.key === 'friends' && friendUnreadCount > 0" class="friend-badge">
        {{ friendUnreadCount > 99 ? '99+' : friendUnreadCount }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 侧边栏选项卡导航组件 @component */
import { computed } from 'vue'
import { User, ChatDotRound, Message, Star, Setting } from '@element-plus/icons-vue'

/** 组件属性：当前激活 Tab、角标数量、好友未读计数、是否为管理员 */
const props = defineProps<{
  activeTab: string
  badgeCount: number
  friendUnreadCount: number
  isAdmin: boolean
}>()

/** 组件事件：切换 Tab、跳转管理后台 */
const emit = defineEmits<{
  'update:activeTab': [tab: string]
  'goToAdmin': []
}>()

/** 所有 Tab 定义 */
const allTabs = [
  { key: 'friends', label: '好友', icon: User, badge: false, requireAdmin: false },
  { key: 'groups', label: '群聊', icon: ChatDotRound, badge: false, requireAdmin: false },
  { key: 'requests', label: '申请', icon: Message, badge: true, requireAdmin: false },
  { key: 'impressions', label: '印象', icon: Star, badge: false, requireAdmin: false },
  { key: 'admin', label: '管理', icon: Setting, badge: false, requireAdmin: true }
]

/** 根据管理员权限过滤可见 Tab @returns 可见 Tab 列表 */
const visibleTabs = computed(() => {
  return allTabs.filter(tab => {
    if (tab.requireAdmin && !props.isAdmin) return false
    return true
  })
})

/** Tab 点击处理 @param tab 点击的 Tab 对象 @returns void */
const handleTabClick = (tab: any) => {
  if (tab.key === 'admin') {
    emit('goToAdmin')
  } else {
    emit('update:activeTab', tab.key)
  }
}
</script>

<style scoped>
.sidebar-tabs {
  display: flex;
  padding: 8px 12px;
  gap: 4px;
  background: var(--bg-color-white);
  border-bottom: 1px solid var(--border-color);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 4px;
  cursor: pointer;
  position: relative;
  font-size: 13px;
  color: var(--text-regular);
  transition: all 0.15s ease;
  border-radius: 8px;
  background: transparent;
  border: none;
}

.tab-item:hover {
  color: var(--color-primary);
  background: #f5f6ff;
}

.tab-item:active { transform: scale(0.96); }

.tab-item.active {
  color: var(--color-primary);
  background: #eef0ff;
  font-weight: 600;
}

.tab-item .el-icon {
  font-size: 18px;
  transition: transform 0.15s ease;
}

.tab-item:hover .el-icon { transform: scale(1.1); }

.badge {
  position: absolute;
  top: 0;
  right: 4px;
  background: var(--color-danger);
  color: white;
  font-size: 10px;
  padding: 0 5px;
  border-radius: 8px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  font-weight: 600;
}

.friend-badge {
  position: absolute;
  top: 0;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 5px;
  background: var(--color-danger);
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
