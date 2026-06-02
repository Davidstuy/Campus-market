import { createApp } from 'vue'
import '@/styles/theme.css'
// Element Plus 命令式组件的样式（模板中未使用，需手动引入）
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/message/style/css'
import { createPinia } from 'pinia'
import router from '@/router'
import App from '@/App.vue'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
