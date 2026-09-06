<script setup lang="ts">
import { SearchOutline, RefreshOutline, TrashOutline, DownloadOutline } from '@vicons/ionicons5'
import { getLoginInfoList, clearLoginInfo, exportLoginInfo, type SysLoginInfo } from '@/api/modules/monitor'
import { SUCCESS_OPTIONS } from '@/constants'
import { useTable } from '@/composables/useTable'
import { useExport } from '@/composables/useExport'

const message = useMessage()
const dialog = useDialog()
const { renderSuccessTag } = useStatusRender()
const { downloadExcel } = useExport()

const searchForm = reactive({ username: '', ipAddress: '', status: null as number | null })
const searchDefaults = { username: '', ipAddress: '', status: null as number | null }
const { loading, tableData, pagination, fetchData, handleSearch, handleReset, handlePageChange, handlePageSizeChange } = useTable<SysLoginInfo, typeof searchForm>({
  api: getLoginInfoList,
  searchForm,
  searchDefaults,
})

const columns = [
  { title: '用户名', key: 'username', width: 120 },
  { title: '登录IP', key: 'ipAddress', width: 140 },
  { title: '登录地点', key: 'loginLocation', width: 140 },
  { title: '浏览器', key: 'browser', width: 120 },
  { title: '操作系统', key: 'os', width: 120 },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: SysLoginInfo) => renderSuccessTag(row.status),
  },
  { title: '登录信息', key: 'msg', width: 200, ellipsis: { tooltip: true } },
  { title: '登录时间', key: 'loginTime', width: 170 },
]

function handleClear() {
  dialog.warning({
    title: '确认清空',
    content: '清空所有登录日志后不可恢复，确认继续？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await clearLoginInfo()
        message.success('清空成功')
        fetchData()
      } catch (e: unknown) {
        message.error((e as Error).message || '清空失败')
      }
    },
  })
}

async function handleExport() {
  await downloadExcel(exportLoginInfo, '登录日志.xlsx')
}

onMounted(() => fetchData())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <n-form inline label-placement="left" :show-feedback="false">
        <n-form-item label="用户名">
          <n-input v-model:value="searchForm.username" placeholder="请输入" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="登录IP">
          <n-input v-model:value="searchForm.ipAddress" placeholder="请输入" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="searchForm.status" :options="SUCCESS_OPTIONS" placeholder="请选择" clearable style="width: 120px" />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSearch"><template #icon><n-icon :component="SearchOutline" /></template>搜索</n-button>
            <n-button @click="handleReset()"><template #icon><n-icon :component="RefreshOutline" /></template>重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>

    <div class="card">
      <div class="flex justify-end gap-8px mb-12px">
        <n-button v-permission="'monitor:login-info:clean'" type="warning" @click="handleClear">
          <template #icon><n-icon :component="TrashOutline" /></template>清空
        </n-button>
        <n-button v-permission="'monitor:login-info:export'" @click="handleExport">
          <template #icon><n-icon :component="DownloadOutline" /></template>导出
        </n-button>
      </div>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: SysLoginInfo) => row.id" striped size="small" remote
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>
  </div>
</template>