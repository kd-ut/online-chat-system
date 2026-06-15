<template>
  <button class="theme-toggle" :title="'当前: ' + label + ' — 点击切换'" @click="toggleTheme">
    <el-icon :size="18">
      <Sunny v-if="themeStore.activeType === 'light'" />
      <Moon v-else-if="themeStore.activeType === 'dark'" />
      <Monitor v-else />
    </el-icon>
  </button>
</template>

<script setup lang="ts">
/** 主题切换按钮，循环切换亮色/暗色/自动三种模式 @component */
import { computed } from 'vue'
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const { toggleTheme, currentLabel, themeStore } = useTheme()

/** 按钮标签 */
const label = computed(() => currentLabel())
</script>

<style scoped>
.theme-toggle {
  width: 38px;
  height: 38px;
  border-radius: var(--border-radius-small);
  background: var(--bg-color);
  border: 1px solid transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-regular);
  transition: all var(--transition-duration) var(--transition-timing);
  flex-shrink: 0;
}

.theme-toggle:hover {
  background: var(--color-primary-light-1);
  color: var(--color-primary);
  transform: rotate(20deg);
}

.theme-toggle:active {
  transform: rotate(20deg) scale(0.92);
}
</style>
