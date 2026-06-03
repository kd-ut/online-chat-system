<template>
  <div v-if="kind === 'video'" class="media-tile" :class="{ local }">
    <video ref="mediaRef" autoplay playsinline :muted="muted" />
    <div class="media-label">{{ label }}</div>
  </div>
  <audio v-else ref="mediaRef" autoplay :muted="muted" />
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'

const props = defineProps<{
  stream: MediaStream | null
  kind: 'audio' | 'video'
  muted?: boolean
  local?: boolean
  label?: string
}>()

const mediaRef = ref<HTMLVideoElement | HTMLAudioElement>()
const label = computed(() => props.label || (props.local ? '我' : '成员'))

const bindStream = () => {
  if (mediaRef.value && mediaRef.value.srcObject !== props.stream) {
    mediaRef.value.srcObject = props.stream
  }
}

watch(() => props.stream, bindStream)
onMounted(bindStream)
</script>

<style scoped>
.media-tile {
  position: relative;
  min-height: 180px;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: #111827;
  border-radius: 8px;
}

.media-tile video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.media-tile.local {
  border: 1px solid #d8dee9;
}

.media-label {
  position: absolute;
  left: 10px;
  bottom: 10px;
  max-width: calc(100% - 20px);
  padding: 4px 8px;
  border-radius: 6px;
  color: #fff;
  background: rgb(17 24 39 / 72%);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
