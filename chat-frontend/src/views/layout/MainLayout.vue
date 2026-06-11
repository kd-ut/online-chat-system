<template>
  <div class="app-container" @click="unlockAudio">
    <div v-if="isMobile && sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false" />

    <div class="main-layout" :class="{
      'is-resizing': isResizing,
      'is-mobile': isMobile,
      'sidebar-open': sidebarOpen
    }" :style="{ '--sidebar-width': isMobile ? '85%' : sidebarWidth + 'px' }">
      <Sidebar :class="{ 'sidebar-panel': true, show: isMobile ? sidebarOpen : true }"
        @select-chat="handleSelectChat" @select-group="handleSelectGroup" />

      <div v-if="!isMobile" class="resize-handle" @mousedown="startResize" />

      <div class="right-panel">
        <Header @toggle-sidebar="sidebarOpen = !sidebarOpen" />
        <Content />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 主布局组件，包含侧边栏、头部和内容区域，支持响应式适配 @component */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import Content from './Content.vue'
import { useFriendStore } from '@/stores/friendStore'
import { useResizable } from '@/composables'

const router = useRouter()
const friendStore = useFriendStore()

/** 移动端侧边栏开关 */
const sidebarOpen = ref(false)

/** 是否为移动端 */
const isMobile = ref(false)
/** 移动端断点 */
const MOBILE_BREAKPOINT = 768

/** 检测屏幕尺寸 */
const checkMobile = () => {
  isMobile.value = window.innerWidth < MOBILE_BREAKPOINT
  if (!isMobile.value) sidebarOpen.value = false
}

const { sidebarWidth, isResizing, startResize } = useResizable({
  minWidth: 280,
  maxWidth: 800,
  defaultWidth: 420,
  storageKey: 'sidebar-width'
})

const handleSelectChat = (friend: any) => {
  router.push({ name: 'Main', query: { friendId: friend.userId || friend.id } })
  if (isMobile.value) sidebarOpen.value = false
}

const handleSelectGroup = (group: any) => {
  router.push({ name: 'Main', query: { groupId: group.id } })
  if (isMobile.value) sidebarOpen.value = false
}

let audioUnlocked = false
const unlockAudio = () => {
  if (audioUnlocked) return
  audioUnlocked = true
  if (navigator.mediaDevices) {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => stream.getTracks().forEach(t => t.stop()))
      .catch(() => {})
  }
  try {
    const ctx = new (window.AudioContext || (window as any).webkitAudioContext)()
    const osc = ctx.createOscillator()
    osc.frequency.value = 1
    osc.connect(ctx.destination)
    osc.start()
    osc.stop(0.001)
  } catch { /* ignore */ }
}

onMounted(() => {
  friendStore.loadFriendList()
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  width: 100%;
  background: var(--bg-color);
  overflow: hidden;
}

.main-layout {
  display: flex;
  height: 100%;
  width: 100%;
  gap: 0;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  background: var(--bg-color);
}

.main-layout.is-resizing {
  cursor: col-resize;
  user-select: none;
}

.main-layout.is-resizing .sidebar,
.main-layout.is-resizing .right-panel {
  pointer-events: none;
}

.resize-handle {
  width: 6px;
  flex-shrink: 0;
  cursor: col-resize;
  position: relative;
  z-index: 30;
  background: transparent;
  transition: background 0.2s;
}

.resize-handle:hover,
.main-layout.is-resizing .resize-handle {
  background: var(--color-primary-light-5);
  opacity: 0.4;
}

/* ===== 移动端适配 ===== */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 50;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@media (max-width: 768px) {
  .sidebar-panel {
    position: fixed !important;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 60;
    transform: translateX(-100%);
    transition: transform 0.3s var(--transition-timing);
    width: 85% !important;
    max-width: 380px;
  }

  .sidebar-panel.show {
    transform: translateX(0);
    box-shadow: 4px 0 30px rgba(0, 0, 0, 0.2);
  }

  .resize-handle {
    display: none;
  }
}
</style>
