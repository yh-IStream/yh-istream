<script setup lang="ts">
import { SearchOutline, RefreshOutline } from '@vicons/ionicons5'
import { getLoginInfoList } from '@/api/modules/system'

const message = useMessage()
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })
const searchForm = reactive({ username: '', ipaddr: '', status: null as number | null })

const statusOptions = [
  { label: '全部', value: null }, { label: '成功', value: 0 }, { label: '失败', value: 1 },
]

const columns = [
  { title: '用户名', key: 'username', width: 120 },
  { title: '登录IP', key: 'ipaddr', width: 140 },
  { title: '登录地点', key: 'loginLocation', width: 140 },
  { title: '浏览器', key: 'browser', width: 120 },
  { title: '操作系统', key: 'os', width: 120 },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => h(NTag, { type: row.status === 0 ? 'success' : 'error', size: 'small' }, { default: () => row.status === 0 ? '成功' : '失败' }),
  },
  { title: '登录信息', key: 'msg', width: 200, ellipsis: { tooltip: true } },
  { title: '登录时间', key: 'loginTime', width: 170 },
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.ipaddr) params.ipaddr = searchForm.ipaddr
    if (searchForm.status !== null) params.status = searchForm.status
    const res: any = await getLoginInfoList(params)
    tableData.value = res.data?.records ?? []
    pagination.itemCount = res.data?.total ?? 0
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.username = ''; searchForm.ipaddr = ''; searchForm.status = null; pagination.page = 1; fetchData() }
function handlePageChange(page: number) { pagination.page = page; fetchData() }
function handlePageSizeChange(size: number) { pagination.pageSize = size; pagination.page = 1; fetchData() }

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
          <n-input v-model:value="searchForm.ipaddr" placeholder="请输入" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="searchForm.status" :options="statusOptions" placeholder="请选择" clearable style="width: 120px" />
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
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: any) => row.id" striped size="small" remote
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>
  </div>
</template>