<template>
  <div class="sidebar-tabs">
    <div class="tabs-track">
      <div
        class="tab-indicator"
        :style="{ left: indicatorLeft + 'px', width: indicatorWidth + 'px' }"
      />
    </div>
    <div v-for="tab in visibleTabs" :key="tab.key" :ref="el => setTabRef(tab.key, el)"
      class="tab-item" :class="{ active: activeTab === tab.key }"
      @click="handleTabClick(tab)">
      <el-icon>
        <component :is="tab.icon" />
      </el-icon>
      <span>{{ tab.label }}</span>
      <span v-if="tab.badge && badgeCount > 0" class="badge">
        {{ badgeCount > 99 ? '99+' : badgeCount }}
      </span>
      <span v-if="tab.key === 'friends' && friendUnreadCount > 0" class="friend-badge">
        {{ friendUnreadCount > 99 ? '99+' : friendUnreadCount }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 侧边栏选项卡导航组件 — 带滑动指示器 @component */
import { computed, ref, watch, onMounted, nextTick } from 'vue'
import { User, ChatDotRound, Message, Star, Setting } from '@element-plus/icons-vue'

const props = defineProps<{
  activeTab: string
  badgeCount: number
  friendUnreadCount: number
  isAdmin: boolean
}>()

const emit = defineEmits<{
  'update:activeTab': [tab: string]
  'goToAdmin': []
}>()

const allTabs = [
  { key: 'friends', label: '好友', icon: User, badge: false, requireAdmin: false },
  { key: 'groups', label: '群聊', icon: ChatDotRound, badge: false, requireAdmin: false },
  { key: 'requests', label: '申请', icon: Message, badge: true, requireAdmin: false },
  { key: 'impressions', label: '印象', icon: Star, badge: false, requireAdmin: false },
  { key: 'admin', label: '管理', icon: Setting, badge: false, requireAdmin: true }
]

const visibleTabs = computed(() => {
  return allTabs.filter(tab => {
    if (tab.requireAdmin && !props.isAdmin) return false
    return true
  })
})

/** 指示器位置和宽度 */
const indicatorLeft = ref(0)
const indicatorWidth = ref(0)

/** Tab 元素引用映射 */
const tabRefs = ref<Record<string, HTMLElement | null>>({})

function setTabRef(key: string, el: any) {
  if (el) {
    tabRefs.value[key] = el.$el || el as HTMLElement
  }
}

/** 更新滑动指示器位置 */
function updateIndicator() {
  const el = tabRefs.value[props.activeTab]
  if (el) {
    indicatorLeft.value = el.offsetLeft
    indicatorWidth.value = el.offsetWidth
  }
}

watch(() => props.activeTab, () => {
  nextTick(() => updateIndicator())
})

watch(visibleTabs, () => {
  nextTick(() => updateIndicator())
})

onMounted(() => {
  nextTick(() => updateIndicator())
})

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
  flex-direction: column;
  padding: 6px 10px;
  background: var(--bg-color-white);
  border-bottom: 1px solid var(--border-color);
  position: relative;
}

.tabs-track {
  position: absolute;
  bottom: 0;
  left: 10px;
  right: 10px;
  height: 2px;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  height: 2px;
  background: var(--color-primary);
  border-radius: 1px;
  transition: left 0.25s var(--transition-timing-bounce), width 0.25s var(--transition-timing-bounce);
}

.tab-item {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 4px;
  cursor: pointer;
  position: relative;
  font-size: 13px;
  color: var(--text-regular);
  transition: all 0.2s var(--transition-timing);
  border-radius: 8px;
  background: transparent;
  border: none;
}

.tab-item:hover {
  color: var(--color-primary);
  background: var(--color-primary-light-1);
}

.tab-item:active { transform: scale(0.96); }

.tab-item.active {
  color: var(--color-primary);
  font-weight: 600;
}

.tab-item .el-icon {
  font-size: 18px;
  transition: transform 0.2s var(--transition-timing);
}

.tab-item:hover .el-icon { transform: scale(1.1); }

.badge,
.friend-badge {
  position: absolute;
  top: 2px;
  right: 2px;
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
  animation: bounceIn 0.4s var(--transition-timing-bounce);
}
</style>
