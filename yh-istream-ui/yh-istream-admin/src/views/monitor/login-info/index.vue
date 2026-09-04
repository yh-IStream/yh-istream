<script setup lang="ts">
import { SearchOutline, RefreshOutline, TrashOutline, DownloadOutline } from '@vicons/ionicons5'
import { getLoginInfoList, clearLoginInfo, exportLoginInfo } from '@/api/modules/monitor'
import { SUCCESS_OPTIONS } from '@/constants'

const message = useMessage()
const dialog = useDialog()
const { pagination, resetPage, setPage, setPageSize } = usePagination()
const { renderSuccessTag } = useStatusRender()

const loading = ref(false)
const tableData = ref<any[]>([])
const searchForm = reactive({ username: '', ipAddress: '', status: null as number | null })

const columns = [
  { title: '用户名', key: 'username', width: 120 },
  { title: '登录IP', key: 'ipAddress', width: 140 },
  { title: '登录地点', key: 'loginLocation', width: 140 },
  { title: '浏览器', key: 'browser', width: 120 },
  { title: '操作系统', key: 'os', width: 120 },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => renderSuccessTag(row.status),
  },
  { title: '登录信息', key: 'msg', width: 200, ellipsis: { tooltip: true } },
  { title: '登录时间', key: 'loginTime', width: 170 },
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.ipAddress) params.ipAddress = searchForm.ipAddress
    if (searchForm.status !== null) params.status = searchForm.status
    const res: any = await getLoginInfoList(params)
    tableData.value = res.data?.records ?? []
    pagination.itemCount = res.data?.total ?? 0
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { loading.value = false }
}

function handleSearch() { resetPage(); fetchData() }
function handleReset() { searchForm.username = ''; searchForm.ipAddress = ''; searchForm.status = null; resetPage(); fetchData() }
function handlePageChange(page: number) { setPage(page); fetchData() }
function handlePageSizeChange(size: number) { setPageSize(size); fetchData() }

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
      } catch (e: any) {
        message.error(e.message || '清空失败')
      }
    },
  })
}

async function handleExport() {
  try {
    const res: any = await exportLoginInfo()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '登录日志.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (e: any) {
    message.error(e.message || '导出失败')
  }
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
            <n-button @click="handleReset"><template #icon><n-icon :component="RefreshOutline" /></template>重置</n-button>
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
        :row-key="(row: any) => row.id" striped size="small" remote
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>
  </div>
</template>