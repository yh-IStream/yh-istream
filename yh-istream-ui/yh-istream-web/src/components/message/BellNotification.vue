<script setup lang="ts">
import { NotificationsOutline, NotificationsOffOutline } from '@vicons/ionicons5'
import { getMessageList, markAsRead, markAllAsRead, type SysMessage } from '@/api/modules/message'
import { useSSE } from '@/composables/useSSE'

const router = useRouter()
const message = useMessage()
const sse = useSSE()

const popoverVisible = ref(false)
const recentMessages = ref<SysMessage[]>([])
const loadingMessages = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null
let cleanupNotif: (() => void) | null = null
let cleanupSystem: (() => void) | null = null
let cleanupAIInsight: (() => void) | null = null

const EVENT_ICON_MAP: Record<string, string> = {
  SYSTEM: '⚙️',
  NOTIFICATION: '🔔',
  DATA_CHANGE: '📊',
  ALERT: '🚨',
  AI_INSIGHT: '🧠',
}

function getEventIcon(eventType: string): string {
  return EVENT_ICON_MAP[eventType] ?? '📌'
}

function formatTime(createTime: string): string {
  if (!createTime) {
    return ''
  }
  const date = new Date(createTime)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60_000) {
    return '刚刚'
  }
  if (diff < 3600_000) {
    return `${Math.floor(diff / 60_000)}分钟前`
  }
  if (diff < 86400_000) {
    return `${Math.floor(diff / 3600_000)}小时前`
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

async function fetchRecentMessages() {
  loadingMessages.value = true
  try {
    const res = await getMessageList({ pageNum: 1, pageSize: 5 })
    recentMessages.value = res.data?.records ?? []
  } catch {
    // 获取失败静默
  } finally {
    loadingMessages.value = false
  }
}

async function handleMarkAsRead(msg: SysMessage) {
  try {
    await markAsRead(msg.id)
    sse.decrementUnread()
    msg.isRead = 1
  } catch {
    message.error('标记已读失败')
  }
}

async function handleMarkAllAsRead() {
  try {
    await markAllAsRead()
    sse.resetUnread()
    recentMessages.value.forEach((m) => { m.isRead = 1 })
    message.success('已全部标为已读')
  } catch {
    message.error('操作失败')
  }
}

function handleClickMessage(msg: SysMessage) {
  if (msg.isRead !== 1) {
    handleMarkAsRead(msg)
  }
  popoverVisible.value = false
  router.push('/message/list')
}

function goToList() {
  popoverVisible.value = false
  router.push('/message/list')
}

function handlePopoverShow() {
  fetchRecentMessages()
}

onMounted(() => {
  cleanupNotif = sse.on('NOTIFICATION', () => {
    sse.fetchUnreadCount()
  })
  cleanupSystem = sse.on('SYSTEM', () => {
    sse.fetchUnreadCount()
  })
  cleanupAIInsight = sse.on('ai_insight', (data: any) => {
    if (data?.eventType === 'AI_INSIGHT') {
      const notification = window.Notification
      if (notification && notification.permission === 'granted') {
        new notification(`🧠 ${data.title}`, {
          body: data.content,
          icon: '/favicon.ico',
        })
      }
      message.info(`🧠 ${data.title}: ${data.confidence}`, {
        closable: true,
        duration: 8000,
        onAfterLeave: () => {},
      })
      sse.fetchUnreadCount()
    }
  })

  watch(() => sse.status.value, (newStatus) => {
    if (newStatus !== 'connected') {
      if (!pollTimer) {
        sse.fetchUnreadCount()
        pollTimer = setInterval(() => sse.fetchUnreadCount(), 15000)
      }
    } else {
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    }
  }, { immediate: true })
})

onBeforeUnmount(() => {
  cleanupNotif?.()
  cleanupSystem?.()
  cleanupAIInsight?.()
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<template>
  <NPopover
    v-model:show="popoverVisible"
    trigger="click"
    placement="bottom-end"
    :width="360"
    @update:show="(show: boolean) => show && handlePopoverShow()"
  >
    <template #trigger>
      <div class="bell-trigger">
        <NBadge
          :value="sse.unreadCount.value"
          :max="99"
          :show="sse.unreadCount.value > 0"
        >
          <NButton
            quaternary
            circle
            size="small"
          >
            <template #icon>
              <NIcon
                :component="sse.unreadCount.value > 0 ? NotificationsOutline : NotificationsOffOutline"
                :class="{ 'bell-ring': sse.unreadCount.value > 0 }"
              />
            </template>
          </NButton>
        </NBadge>
      </div>
    </template>

    <div class="bell-popover">
      <div class="bell-header">
        <span class="text-sm font-semibold">消息通知</span>
        <NButton
          v-if="sse.unreadCount.value > 0"
          text
          size="tiny"
          type="primary"
          @click="handleMarkAllAsRead"
        >
          全部已读
        </NButton>
      </div>

      <div class="bell-body">
        <NSpin
          :show="loadingMessages"
          size="small"
        >
          <div
            v-if="recentMessages.length === 0 && !loadingMessages"
            class="bell-empty"
          >
            <span class="text-sm text-gray-400">暂无消息</span>
          </div>
          <div
            v-for="msg in recentMessages"
            :key="msg.id"
            class="bell-item"
            :class="{ unread: msg.isRead !== 1 }"
            @click="handleClickMessage(msg)"
          >
            <span class="bell-item-icon">{{ getEventIcon(msg.eventType) }}</span>
            <div class="bell-item-content">
              <div class="bell-item-title">
                <span class="text-sm font-medium truncate">{{ msg.title }}</span>
                <span
                  v-if="msg.isRead !== 1"
                  class="bell-item-dot"
                />
              </div>
              <div class="bell-item-text text-xs text-gray-400 truncate">
                {{ msg.content }}
              </div>
              <span class="bell-item-time text-xs text-gray-400">{{ formatTime(msg.createTime) }}</span>
            </div>
          </div>
        </NSpin>
      </div>

      <div class="bell-footer">
        <NButton
          text
          size="small"
          @click="goToList"
        >
          查看全部
        </NButton>
      </div>
    </div>
  </NPopover>
</template>

<style scoped>
.bell-trigger {
  cursor: pointer;
}

.bell-ring {
  animation: bell-shake 0.5s ease-in-out;
}

@keyframes bell-shake {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(10deg); }
  75% { transform: rotate(-10deg); }
}

.bell-popover {
  display: flex;
  flex-direction: column;
  max-height: 400px;
}

.bell-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--n-border-color, #eee);
}

.bell-body {
  flex: 1;
  overflow-y: auto;
  min-height: 60px;
}

.bell-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
}

.bell-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.bell-item:hover {
  background: rgba(20, 184, 166, 0.06);
}

.bell-item.unread {
  background: rgba(20, 184, 166, 0.03);
}

.bell-item-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.bell-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.bell-item-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.bell-item-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #14b8a6;
  flex-shrink: 0;
}

.bell-item-text {
  line-height: 1.4;
}

.bell-footer {
  display: flex;
  justify-content: center;
  padding: 8px 14px;
  border-top: 1px solid var(--n-border-color, #eee);
}
</style>