/**
 * 主题状态管理 Store
 * 管理亮色/暗色/自动三种主题模式及其持久化
 * @module themeStore
 */
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/** 主题类型枚举 */
export type ThemeMode = 'light' | 'dark' | 'auto'
export type ThemeType = 'light' | 'dark'

const STORAGE_KEY_MODE = 'app-theme-mode'
const STORAGE_KEY_COLOR = 'app-theme-color'

/** 从 localStorage 读取主题模式，带默认值 */
function loadMode(): ThemeMode {
  try {
    const stored = localStorage.getItem(STORAGE_KEY_MODE)
    if (stored === 'light' || stored === 'dark' || stored === 'auto') return stored
  } catch { /* ignore */ }
  return 'light'
}

/** 从 localStorage 读取主题色，带默认值 */
function loadColor(): string {
  try {
    const stored = localStorage.getItem(STORAGE_KEY_COLOR)
    if (stored) return stored
  } catch { /* ignore */ }
  return '#6c5ce7'
}

export const useThemeStore = defineStore('theme', () => {
  /** 主题模式 (light | dark | auto) */
  const mode = ref<ThemeMode>(loadMode())
  /** 实际生效的主题类型 */
  const activeType = ref<ThemeType>('light')
  /** 主题色 */
  const themeColor = ref<string>(loadColor())

  /** 设置主题模式 */
  function setMode(m: ThemeMode) {
    mode.value = m
    try { localStorage.setItem(STORAGE_KEY_MODE, m) } catch { /* ignore */ }
  }

  /** 设置实际生效主题 */
  function setActiveType(t: ThemeType) {
    activeType.value = t
  }

  /** 设置主题色并应用到 DOM */
  function setThemeColor(color: string) {
    themeColor.value = color
    try { localStorage.setItem(STORAGE_KEY_COLOR, color) } catch { /* ignore */ }

    const el = document.documentElement
    el.style.setProperty('--color-primary', color)

    // 生成 9 级明度变体
    for (let i = 1; i <= 9; i++) {
      const lightColor = getLightColor(color, i / 10)
      el.style.setProperty(`--color-primary-light-${i}`, lightColor)
    }
  }

  /** 生成浅色变体 (混合白色) */
  function getLightColor(hex: string, amount: number): string {
    const r = parseInt(hex.slice(1, 3), 16)
    const g = parseInt(hex.slice(3, 5), 16)
    const b = parseInt(hex.slice(5, 7), 16)
    const mix = (c: number) => Math.round(c + (255 - c) * amount)
    return `#${mix(r).toString(16).padStart(2, '0')}${mix(g).toString(16).padStart(2, '0')}${mix(b).toString(16).padStart(2, '0')}`
  }

  return { mode, activeType, themeColor, setMode, setActiveType, setThemeColor }
})
