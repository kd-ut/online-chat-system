/** 应用入口文件 @module main */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { setupRippleDirective } from './directives/ripple'
import './assets/styles/main.css'
import './assets/styles/dark.css'
import './assets/styles/tailwind.css'

const app = createApp(App)
setupRippleDirective(app)

/** 注册 Element Plus 所有图标组件 */
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
