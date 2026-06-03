<template>
  <div class="app-container" @click="unlockAudio">
    <div class="main-layout" :class="{ 'is-resizing': isResizing }" :style="{ '--sidebar-width': sidebarWidth + 'px' }">
      <Sidebar @select-chat="handleSelectChat" @select-group="handleSelectGroup" />
      <div class="resize-handle" @mousedown="startResize" />
      <div class="right-panel">
        <Header />
        <Content />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 主布局组件，包含侧边栏、头部和内容区域 @component */
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import Content from './Content.vue'
import { useFriendStore } from '@/stores/friendStore'
import { useResizable } from '@/composables'

const router = useRouter()
const friendStore = useFriendStore()

const { sidebarWidth, isResizing, startResize } = useResizable({
  minWidth: 280,
  maxWidth: 800,
  defaultWidth: 420,
  storageKey: 'sidebar-width'
})

/** 选择好友聊天 @param friend 好友对象 @returns void */
const handleSelectChat = (friend: any) => {
  router.push({ name: 'Main', query: { friendId: friend.userId || friend.id } })
}

/** 选择群聊 @param group 群聊对象 @returns void */
const handleSelectGroup = (group: any) => {
  router.push({ name: 'Main', query: { groupId: group.id } })
}

/** 是否已完成音频解锁 */
let audioUnlocked = false
/** 解锁音频上下文（解决浏览器自动播放策略） @returns void */
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
  background: var(--color-primary-light);
  opacity: 0.4;
}
</style>
