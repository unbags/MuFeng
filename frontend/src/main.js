import { createApp } from 'vue'
import router from './router'
import { setRouter } from './api/index.js'
import './styles/tokens.css'
import './styles/reset.css'
import './styles/base.css'
import './styles/layout.css'
import './styles/components.css'
import './styles/customer.css'
import './styles/admin.css'
import './styles/dashboard.css'
import './styles/animations.css'
import './styles/responsive.css'
import App from './App.vue'

setRouter(router)
createApp(App).use(router).mount('#app')
