/**
 * 主题切换 Composable
 * 封装亮色/暗色/自动三种模式的切换逻辑
 * @module useTheme
 */
import { watch, onMounted } from 'vue'
import { useThemeStore, type ThemeMode, type ThemeType } from '@/stores/themeStore'

/** 系统暗色偏好检测 */
function getSystemDark(): boolean {
  if (typeof window === 'undefined') return false
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export function useTheme() {
  const themeStore = useThemeStore()

  /** 临时禁用所有过渡动画 (防切换闪烁) */
  function disableTransitions() {
    const existing = document.getElementById('disable-transitions')
    if (existing) return
    const style = document.createElement('style')
    style.setAttribute('id', 'disable-transitions')
    style.textContent = '* { transition: none !important; animation: none !important; }'
    document.head.appendChild(style)
  }

  /** 恢复过渡动画 */
  function enableTransitions() {
    const style = document.getElementById('disable-transitions')
    if (style) {
      style.remove()
    }
  }

  /** 应用主题到 DOM */
  function applyTheme(type: ThemeType) {
    const el = document.documentElement

    if (type === 'dark') {
      el.classList.add('dark')
    } else {
      el.classList.remove('dark')
    }

    themeStore.setActiveType(type)
  }

  /** 切换主题模式 */
  function switchTheme(mode: ThemeMode) {
    disableTransitions()

    let actualType: ThemeType
    if (mode === 'auto') {
      actualType = getSystemDark() ? 'dark' : 'light'
    } else {
      actualType = mode
    }

    themeStore.setMode(mode)
    applyTheme(actualType)

    // 双 requestAnimationFrame 确保在下一帧恢复过渡
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        enableTransitions()
      })
    })
  }

  /** 快速切换 (亮 ↔ 暗) */
  function toggleTheme() {
    const currentMode = themeStore.mode
    if (currentMode === 'dark') {
      switchTheme('light')
    } else {
      // light 和 auto 都切换到 dark
      switchTheme('dark')
    }
  }

  /** 初始化主题 (应用启动时调用) */
  function initTheme() {
    const mode = themeStore.mode
    let actualType: ThemeType

    if (mode === 'auto') {
      actualType = getSystemDark() ? 'dark' : 'light'
    } else {
      actualType = mode
    }

    applyTheme(actualType)

    // 监听系统主题变化 (仅在 auto 模式下响应)
    if (typeof window !== 'undefined') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      const handler = (e: MediaQueryListEvent) => {
        if (themeStore.mode === 'auto') {
          applyTheme(e.matches ? 'dark' : 'light')
        }
      }
      mediaQuery.addEventListener('change', handler)
    }
  }

  /** 当前主题模式标签 */
  function currentLabel(): string {
    const map: Record<ThemeMode, string> = { light: '浅色', dark: '深色', auto: '自动' }
    return map[themeStore.mode]
  }

  /** 下一主题模式 */
  function nextMode(): ThemeMode {
    const cycle: ThemeMode[] = ['light', 'dark', 'auto']
    const idx = cycle.indexOf(themeStore.mode)
    return cycle[(idx + 1) % cycle.length]
  }

  /** 下一主题图标 */
  function nextIcon(): string {
    const icons: Record<ThemeMode, string> = { light: 'Sunny', dark: 'Moon', auto: 'Monitor' }
    return icons[nextMode()]
  }

  return { switchTheme, toggleTheme, initTheme, currentLabel, nextMode, nextIcon, themeStore }
}
