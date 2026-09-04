<script setup lang="ts">
import { getOperLogList } from '@/api/modules/monitor'
import { getDashboardStats } from '@/api/modules/dashboard'
import {
  PeopleOutline,
  ShieldOutline,
  DocumentTextOutline,
  ServerOutline,
  TrendingUpOutline,
} from '@vicons/ionicons5'

const authStore = useAuthStore()
const now = ref(new Date())
const logList = ref<any[]>([])
const statsLoaded = ref(false)
let timer: ReturnType<typeof setInterval>

const stats = reactive([
  { label: '用户数', value: '--', displayValue: '--', icon: PeopleOutline, gradient: 'from-teal-500 to-cyan-400' },
  { label: '角色数', value: '--', displayValue: '--', icon: ShieldOutline, gradient: 'from-cyan-500 to-blue-400' },
  { label: '今日操作', value: '--', displayValue: '--', icon: DocumentTextOutline, gradient: 'from-blue-500 to-violet-400' },
  { label: '在线用户', value: '--', displayValue: '--', icon: ServerOutline, gradient: 'from-violet-500 to-pink-400' },
])

function animateNumber(index: number, target: number) {
  const duration = 800
  const start = performance.now()
  const step = (timestamp: number) => {
    const progress = Math.min((timestamp - start) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    stats[index].displayValue = String(Math.round(target * eased))
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }
  requestAnimationFrame(step)
}

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)

  fetchLatestLogs()
  fetchStats()
})

onUnmounted(() => {
  clearInterval(timer)
})

async function fetchLatestLogs() {
  try {
    const res = await getOperLogList({ pageNum: 1, pageSize: 5 })
    logList.value = (res as any).data?.records ?? []
  } catch {
    // ignore
  }
}

async function fetchStats() {
  try {
    const res: any = await getDashboardStats()
    const data = res.data
    if (data) {
      const values = [
        data.userCount ?? 0,
        data.roleCount ?? 0,
        data.todayOperCount ?? 0,
        data.onlineCount ?? 0,
      ]
      values.forEach((v: number, i: number) => {
        stats[i].value = String(v)
        setTimeout(() => animateNumber(i, v), i * 120)
      })
      statsLoaded.value = true
    }
  } catch {
    // ignore
  }
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="card glass-subtle flex items-center justify-between overflow-hidden relative animate-in animate-in-1">
      <div class="absolute inset-0 stream-gradient-subtle stream-animate opacity-40" />
      <div class="relative z-1">
        <h2 class="text-xl font-bold text-gray-800 dark:text-gray-100 m-0 mb-1">
          欢迎回来，{{ authStore.nickname }}
        </h2>
        <p class="text-sm text-gray-500 dark:text-gray-400 m-0">
          {{ now.toLocaleString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' }) }}
        </p>
      </div>
      <div class="relative z-1 flex items-center gap-2 text-teal-600 dark:text-teal-400">
        <n-icon :component="TrendingUpOutline" size="28" />
        <span class="text-sm font-semibold tracking-wide">iStream</span>
      </div>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div
        v-for="(stat, index) in stats"
        :key="stat.label"
        class="card glass-subtle group relative overflow-hidden cursor-default transition-all duration-300 hover:translate-y--2 hover:shadow-lg animate-in"
        :class="[`animate-in-${index + 2}`]"
      >
        <div class="absolute top-0 right-0 w-24 h-24 opacity-10 dark:opacity-15">
          <div class="w-full h-full rounded-full bg-gradient-to-br" :class="stat.gradient" style="filter: blur(20px); transform: translate(30%, -30%)" />
        </div>
        <div class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500 shimmer-animate pointer-events-none" />
        <div class="flex items-center gap-3 relative z-1">
          <div
            class="w-12 h-12 rounded-xl flex-center text-white shadow-md transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3"
            :class="stat.gradient"
            style="background-size: 200% 200%"
          >
            <n-icon :component="stat.icon" size="22" />
          </div>
          <div>
            <div class="text-2xl font-bold text-gray-800 dark:text-gray-100 tabular-nums">
              {{ statsLoaded ? stat.displayValue : stat.value }}
            </div>
            <div class="text-xs text-gray-500 dark:text-gray-400 font-medium tracking-wide uppercase">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="card glass-subtle animate-in animate-in-6">
      <div class="flex items-center gap-2 mb-3">
        <span class="inline-block w-1 h-5 rounded-full bg-gradient-to-b from-teal-400 to-cyan-400" />
        <h3 class="text-base font-semibold text-gray-800 dark:text-gray-100 m-0">
          最新操作日志
        </h3>
      </div>
      <n-table :single-line="false" size="small" class="stream-table">
        <thead>
          <tr>
            <th>操作人</th>
            <th>操作内容</th>
            <th>IP 地址</th>
            <th>操作时间</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(log, idx) in logList" :key="log.id" class="table-row-fade-in" :style="{ animationDelay: `${idx * 60}ms` }">
            <td>{{ log.operName }}</td>
            <td>{{ log.title }}</td>
            <td>{{ log.operIp }}</td>
            <td>{{ log.operTime }}</td>
            <td>
              <n-tag :type="log.status === 0 ? 'success' : 'error'" size="small" round>
                {{ log.status === 0 ? '成功' : '失败' }}
              </n-tag>
            </td>
          </tr>
          <tr v-if="logList.length === 0">
            <td colspan="5" class="text-center text-gray-400 py-4">暂无操作日志</td>
          </tr>
        </tbody>
      </n-table>
    </div>
  </div>
</template>

<style scoped>
:deep(.stream-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.stream-table th) {
  background: rgba(20, 184, 166, 0.04) !important;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

:deep(.stream-table tr) {
  transition: background 0.2s ease;
}

:deep(.stream-table tbody tr:hover) {
  background: rgba(20, 184, 166, 0.04) !important;
}

html.dark :deep(.stream-table tbody tr:hover) {
  background: rgba(45, 212, 191, 0.06) !important;
}

.table-row-fade-in {
  animation: count-up 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}
</style>