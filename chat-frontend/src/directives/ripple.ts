/**
 * v-ripple 水波纹效果指令
 * Material Design 风格的点击波纹，提升交互触感
 *
 * 用法:
 *   <el-button v-ripple>点击</el-button>
 *   <div v-ripple="{ color: 'rgba(255,0,0,0.2)' }">自定义颜色</div>
 */
import type { App, Directive, DirectiveBinding } from 'vue'

interface RippleOptions {
  color?: string
}

const vRipple: Directive<HTMLElement, RippleOptions> = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const options: RippleOptions = binding.value || {}

    el.style.position = 'relative'
    el.style.overflow = 'hidden'

    el.addEventListener('mousedown', (e: MouseEvent) => {
      const rect = el.getBoundingClientRect()
      const left = e.clientX - rect.left
      const top = e.clientY - rect.top

      const ripple = document.createElement('div')
      const diameter = Math.max(el.clientWidth, el.clientHeight)
      const radius = diameter / 2
      const animationDuration = 600 + diameter * 0.5

      ripple.style.width = ripple.style.height = `${diameter}px`
      ripple.style.left = `${left - radius}px`
      ripple.style.top = `${top - radius}px`
      ripple.style.position = 'absolute'
      ripple.style.borderRadius = '50%'
      ripple.style.pointerEvents = 'none'

      const buttonTypes = ['primary', 'info', 'warning', 'danger', 'success'].map(t => `el-button--${t}`)
      const isColoredButton = buttonTypes.some(type => el.classList.contains(type))
      const defaultColor = isColoredButton
        ? 'rgba(255, 255, 255, 0.25)'
        : 'var(--color-primary-light-5)'

      ripple.style.backgroundColor = options.color || defaultColor
      ripple.style.transform = 'scale(0)'
      ripple.style.opacity = '0.6'
      ripple.style.transition = `transform ${animationDuration}ms cubic-bezier(0.3,0,0.2,1), opacity ${animationDuration}ms cubic-bezier(0.3,0,0.5,1)`
      ripple.style.zIndex = '1'

      el.appendChild(ripple)

      requestAnimationFrame(() => {
        ripple.style.transform = 'scale(2)'
        ripple.style.opacity = '0'
      })

      setTimeout(() => ripple.remove(), animationDuration + 500)
    })
  }
}

export function setupRippleDirective(app: App) {
  app.directive('ripple', vRipple)
}
