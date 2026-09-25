<script setup lang="ts">
import { CheckmarkDoneOutline } from '@vicons/ionicons5'
import { getMessageList, markAsRead, markAllAsRead, type SysMessage } from '@/api/modules/message'
import { useSSE } from '@/composables/useSSE'

const message = useMessage()
const sse = useSSE()

const searchForm = reactive({
  eventType: null as string | null,
})
const searchDefaults = {
  eventType: null as string | null,
}
const { loading, tableData, pagination, fetchData, handleSearch, handleReset, handlePageChange, handlePageSizeChange } = useTable<SysMessage, typeof searchForm>({
  api: getMessageList,
  searchForm,
  searchDefaults,
})

const eventTypeLabelMap: Record<string, string> = {
  DATA_CHANGE: '数据变更',
  NOTIFICATION: '通知',
  SYSTEM: '系统',
  ALERT: '告警',
  AI_INSIGHT: 'AI 洞察',
}

function getEventTypeLabel(eventType: string): string {
  return eventTypeLabelMap[eventType] ?? eventType
}

function formatTime(createTime: string): string {
  if (!createTime) {
    return ''
  }
  const date = new Date(createTime)
  const y = date.getFullYear()
  const M = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${M}-${d} ${h}:${m}`
}

const READ_FLAG = 1

async function handleMarkAsRead(row: SysMessage) {
  if (row.isRead === READ_FLAG) {
    return
  }
  try {
    await markAsRead(row.id)
    row.isRead = READ_FLAG
    sse.decrementUnread()
    message.success('已标记为已读')
  } catch {
    message.error('操作失败')
  }
}

async function handleMarkAllAsRead() {
  try {
    await markAllAsRead()
    sse.resetUnread()
    tableData.value.forEach((row) => { row.isRead = READ_FLAG })
    message.success('已全部标为已读')
  } catch {
    message.error('操作失败')
  }
}

const columns = [
  { title: '类型', key: 'eventType', width: 90, render: (row: SysMessage) => getEventTypeLabel(row.eventType) },
  { title: '标题', key: 'title', minWidth: 150, ellipsis: { tooltip: true } },
  { title: '内容', key: 'content', minWidth: 200, ellipsis: { tooltip: true } },
  { title: '时间', key: 'createTime', width: 160, render: (row: SysMessage) => formatTime(row.createTime) },
  {
    title: '状态',
    key: 'isRead',
    width: 80,
    render: (row: SysMessage) => row.isRead === READ_FLAG ? '已读' : '未读',
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    render: (row: SysMessage) => {
      if (row.isRead === READ_FLAG) {
        return h('span', { class: 'text-gray-400 text-xs' }, '已读')
      }
      return h(
        NButton,
        { size: 'tiny', text: true, type: 'primary', onClick: () => handleMarkAsRead(row) },
        { default: () => '标记已读' },
      )
    },
  },
]

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">
        消息中心
      </h2>
      <NButton
        type="primary"
        ghost
        size="small"
        @click="handleMarkAllAsRead"
      >
        <template #icon>
          <NIcon :component="CheckmarkDoneOutline" />
        </template>
        全部已读
      </NButton>
    </div>

    <div class="page-search">
      <NSpace>
        <NSelect
          v-model:value="searchForm.eventType"
          :options="[
            { label: '全部', value: undefined, type: 'option' as const },
            { label: '📊 数据变更', value: 'DATA_CHANGE', type: 'option' as const },
            { label: '🔔 通知', value: 'NOTIFICATION', type: 'option' as const },
            { label: '⚙️ 系统', value: 'SYSTEM', type: 'option' as const },
          ]"
          placeholder="消息类型"
          clearable
          style="width: 140px"
        />
        <NButton
          type="primary"
          size="small"
          @click="handleSearch"
        >
          查询
        </NButton>
        <NButton
          size="small"
          @click="() => handleReset()"
        >
          重置
        </NButton>
      </NSpace>
    </div>

    <NDataTable
      :columns="columns"
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :row-key="(row: SysMessage) => row.id"
      :row-class-name="(row: SysMessage) => row.isRead !== READ_FLAG ? 'unread-row' : ''"
      size="small"
      striped
      flex-height
      @update:page="handlePageChange"
      @update:page-size="handlePageSizeChange"
    />
  </div>
</template>

<style scoped>
.page-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--n-text-color);
  margin: 0;
}

.page-search {
  padding: 12px 16px;
  background: var(--n-card-color, #fff);
  border-radius: 8px;
  border: 1px solid var(--n-border-color, #eee);
}

:deep(.unread-row td) {
  font-weight: 500;
}
</style>