<script setup lang="ts">
import { getOperLogList } from '@/api/modules/system'
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
let timer: ReturnType<typeof setInterval>

const stats = [
  { label: '用户数', value: '--', icon: PeopleOutline, color: '#18a058' },
  { label: '角色数', value: '--', icon: ShieldOutline, color: '#2080f0' },
  { label: '今日操作', value: '--', icon: DocumentTextOutline, color: '#f0a020' },
  { label: '在线用户', value: '--', icon: ServerOutline, color: '#d03050' },
]

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)

  fetchLatestLogs()
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
</script>

<template>
  <div class="flex flex-col gap-16px">
    <!-- 欢迎卡片 -->
    <div class="card flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-gray-800 dark:text-gray-100 m-0 mb-4px">
          欢迎回来，{{ authStore.nickname }}
        </h2>
        <p class="text-sm text-gray-500 dark:text-gray-400 m-0">
          {{ now.toLocaleString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' }) }}
        </p>
      </div>
      <div class="flex items-center gap-4px text-primary">
        <n-icon :component="TrendingUpOutline" size="28" />
        <span class="text-sm font-medium">iStream 智能流式管理平台</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-16px">
      <div v-for="stat in stats" :key="stat.label" class="card flex items-center gap-12px">
        <div
          class="w-48px h-48px rounded-lg flex-center"
          :style="{ backgroundColor: stat.color + '18', color: stat.color }"
        >
          <n-icon :component="stat.icon" size="24" />
        </div>
        <div>
          <div class="text-2xl font-bold text-gray-800 dark:text-gray-100">{{ stat.value }}</div>
          <div class="text-sm text-gray-500 dark:text-gray-400">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 最新操作日志 -->
    <div class="card">
      <h3 class="text-base font-semibold text-gray-800 dark:text-gray-100 m-0 mb-12px">
        最新操作日志
      </h3>
      <n-table :single-line="false" size="small">
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
          <tr v-for="log in logList" :key="log.id">
            <td>{{ log.operName }}</td>
            <td>{{ log.title }}</td>
            <td>{{ log.operIp }}</td>
            <td>{{ log.operTime }}</td>
            <td>
              <n-tag :type="log.status === 0 ? 'success' : 'error'" size="small">
                {{ log.status === 0 ? '成功' : '失败' }}
              </n-tag>
            </td>
          </tr>
          <tr v-if="logList.length === 0">
            <td colspan="5" class="text-center text-gray-400 py-16px">暂无操作日志</td>
          </tr>
        </tbody>
      </n-table>
    </div>
  </div>
</template>