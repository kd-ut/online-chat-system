<template>
  <div class="admin-layout">
    <AdminSidebar :active-menu="activeMenu" @select="handleMenuSelect" />

    <div class="admin-main">
      <AdminHeader :title="pageTitle" />

      <div class="admin-content">
        <AdminBreadcrumb :current-title="pageTitle" />

        <transition name="page-fade" mode="out-in">
          <template v-if="activeMenu === 'stats'" key="stats">
            <StatsCards :stats="stats" />
            <StatsChart :stats="stats" @refresh="loadStats" />
          </template>

          <UserManage v-else-if="activeMenu === 'users'" key="users" />
          <MessageAudit v-else-if="activeMenu === 'messages'" key="messages" />
          <NotificationManage v-else-if="activeMenu === 'notifications'" key="notifications" />
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 管理后台主页面组件 @component */
import { ref, computed, onMounted } from 'vue'
import { getAdminStatsApi, type StatisticsVO } from '@/api/admin'
import AdminSidebar from './components/AdminSidebar.vue'
import AdminHeader from './components/AdminHeader.vue'
import AdminBreadcrumb from './components/AdminBreadcrumb.vue'
import StatsCards from './components/StatsCards.vue'
import StatsChart from './components/StatsChart.vue'
import UserManage from './components/UserManage.vue'
import MessageAudit from './components/MessageAudit.vue'
import NotificationManage from './components/NotificationManage.vue'

const activeMenu = ref('stats')
const stats = ref<StatisticsVO>({
  totalUsers: 0,
  todayActiveUsers: 0,
  todayMessages: 0,
  onlineUsers: 0
})

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    stats: '数据统计',
    users: '用户管理',
    messages: '消息审计',
    notifications: '系统通知'
  }
  return titles[activeMenu.value] || '管理后台'
})

const loadStats = async () => {
  try {
    stats.value = await getAdminStatsApi()
  } catch { /* ignore */ }
}

const handleMenuSelect = (menu: string) => {
  activeMenu.value = menu
  if (menu === 'stats') {
    loadStats()
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-color);
}

.admin-main {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  min-width: 0;
  transition: margin-left 0.25s var(--transition-timing);
}

.admin-content {
  padding: 24px;
  flex: 1;
}

@media (max-width: 768px) {
  .admin-main {
    margin-left: 0;
  }
}
</style>
