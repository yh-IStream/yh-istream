import router from './router'
import App from './App.vue'
import { vPermission } from './directives/permission'

import 'uno.css'
import './styles/global.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.directive('permission', vPermission)

app.mount('#app')