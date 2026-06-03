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
      <span v-if="tab.key === 'friends' && friendUnreadCount > 0" class="friend-dot"></span>
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
  padding: 12px 16px;
  gap: 8px;
  background: var(--bg-color);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 14px 6px;
  cursor: pointer;
  position: relative;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.2s ease;
  border-radius: 16px;
  background: transparent;
  border: 1px solid rgba(180, 200, 230, 0.25);
}

.tab-item:hover {
  color: var(--color-primary);
  background: #f0edff;
  border-color: rgba(108, 92, 231, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.08);
}

.tab-item:active {
  transform: scale(0.93);
  box-shadow: 0 2px 6px rgba(108, 92, 231, 0.15);
}

.tab-item.active {
  color: var(--color-primary);
  background: var(--bg-color-white);
  font-weight: 600;
  border-color: rgba(108, 92, 231, 0.3);
  box-shadow: 0 4px 16px rgba(108, 92, 231, 0.15);
  transform: translateY(-1px);
}

.tab-item .el-icon {
  font-size: 20px;
  transition: transform 0.2s ease;
}

.tab-item:hover .el-icon {
  transform: scale(1.2);
}

.tab-item.active .el-icon {
  transform: scale(1.1);
}

.badge {
  position: absolute;
  top: 2px;
  right: 10px;
  background: var(--color-danger);
  color: white;
  font-size: 10px;
  padding: 0 6px;
  border-radius: 10px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(255, 118, 117, 0.3);
}

.friend-dot {
  position: absolute;
  top: 3px;
  right: 14px;
  width: 8px;
  height: 8px;
  background: var(--color-danger);
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(255, 118, 117, 0.5);
}
</style>
