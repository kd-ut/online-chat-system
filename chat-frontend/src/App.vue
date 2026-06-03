<template>
  <router-view />
  <RtcCallDialog />
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { useRtcStore } from '@/stores/rtcStore'
import { websocketService } from '@/utils/websocket'
import RtcCallDialog from '@/components/rtc/RtcCallDialog.vue'

const userStore = useUserStore()
const rtcStore = useRtcStore()

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
