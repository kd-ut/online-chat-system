<template>
  <router-view />
  <RtcCallDialog />
</template>

<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { useRtcStore } from '@/stores/rtcStore'
import { websocketService } from '@/utils/websocket'
import { useTheme } from '@/composables/useTheme'
import RtcCallDialog from '@/components/rtc/RtcCallDialog.vue'

const userStore = useUserStore()
const rtcStore = useRtcStore()
const { initTheme, themeStore } = useTheme()

onMounted(() => {
  initTheme()
  themeStore.setThemeColor('#6c5ce7')
})

watch(
  () => userStore.token,
  (token) => {
    if (!token) {
      rtcStore.disconnect()
      return
    }
    websocketService.connect()
    rtcStore.ensureSocket()
  },
  { immediate: true }
)
</script>

<style>
/* 全局样式已在 main.css 中定义 */
</style>
