<template>
  <div class="content">
    <ChatView v-if="$route.name === 'Main'" />
    <ProfileView v-else-if="$route.name === 'Profile'" />
    <AdminView v-else-if="$route.name === 'Admin'" />
    <router-view v-else />
  </div>
</template>

<script setup lang="ts">
/** 内容区域路由分发组件 @component */
import ChatView from '@/views/chat/ChatView.vue'
import ProfileView from '@/views/profile/ProfileView.vue'
import AdminView from '@/views/admin/AdminView.vue'
</script>

<style scoped>
.content {
  flex: 1;
  overflow: hidden;
  background: var(--bg-color-white);
  border: 3px solid #b3d9ff;
  border-radius: 24px;
  box-shadow: var(--box-shadow-base);
}
</style>
