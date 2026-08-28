<script setup lang="ts">
import { SearchOutline, RefreshOutline, TrashOutline } from '@vicons/ionicons5'
import { getOperLogList, clearOperLog } from '@/api/modules/system'
import { SUCCESS_OPTIONS } from '@/constants'

const message = useMessage()
const { pagination, resetPage, setPage, setPageSize } = usePagination()
const { renderSuccessTag } = useStatusRender()

const loading = ref(false)
const tableData = ref<any[]>([])
const searchForm = reactive({ title: '', businessType: null as number | null, status: null as number | null })

const businessTypeOptions: any = [
  { label: '全部', value: null }, { label: '新增', value: 1 }, { label: '修改', value: 2 }, { label: '删除', value: 3 },
  { label: '授权', value: 4 }, { label: '导出', value: 5 }, { label: '导入', value: 6 }, { label: '其它', value: 0 },
]

const businessTypeMap: Record<number, { type: 'default' | 'info' | 'primary' | 'error' | 'warning' | 'success'; label: string }> = {
  0: { type: 'default', label: '其它' }, 1: { type: 'info', label: '新增' }, 2: { type: 'primary', label: '修改' },
  3: { type: 'error', label: '删除' }, 4: { type: 'warning', label: '授权' }, 5: { type: 'success', label: '导出' }, 6: { type: 'info', label: '导入' },
}

const columns = [
  { title: '操作标题', key: 'title', width: 160 },
  {
    title: '业务类型', key: 'businessType', width: 100, align: 'center' as const,
    render: (row: any) => {
      const info = businessTypeMap[row.businessType] ?? { type: 'default', label: '未知' }
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label })
    },
  },
  { title: '请求方法', key: 'requestMethod', width: 100 },
  { title: '请求URL', key: 'operUrl', width: 200, ellipsis: { tooltip: true } },
  { title: '操作人', key: 'operName', width: 120 },
  { title: '操作IP', key: 'operIp', width: 140 },
  { title: '操作地点', key: 'operLocation', width: 140 },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => renderSuccessTag(row.status),
  },
  { title: '操作时间', key: 'operTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 100, fixed: 'right' as const,
    render: (row: any) => h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => showDetail(row) }, { default: () => '详情' }),
  },
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.title) params.title = searchForm.title
    if (searchForm.businessType !== null) params.businessType = searchForm.businessType
    if (searchForm.status !== null) params.status = searchForm.status
    const res: any = await getOperLogList(params)
    tableData.value = res.data?.records ?? []
    pagination.itemCount = res.data?.total ?? 0
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { loading.value = false }
}

function handleSearch() { resetPage(); fetchData() }
function handleReset() { searchForm.title = ''; searchForm.businessType = null; searchForm.status = null; resetPage(); fetchData() }
function handlePageChange(page: number) { setPage(page); fetchData() }
function handlePageSizeChange(size: number) { setPageSize(size); fetchData() }

async function handleClear() {
  try { await clearOperLog(); message.success('清空成功'); fetchData() }
  catch (e: any) { message.error(e.message || '清空失败') }
}

function showDetail(row: any) {
  message.info(`请求参数：${row.operParam ?? '无'}`)
}

onMounted(() => fetchData())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <n-form inline label-placement="left" :show-feedback="false">
        <n-form-item label="操作标题">
          <n-input v-model:value="searchForm.title" placeholder="请输入" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="业务类型">
          <n-select v-model:value="searchForm.businessType" :options="businessTypeOptions" placeholder="请选择" clearable style="width: 120px" />
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
      <div class="mb-12px">
        <n-button type="error" ghost @click="handleClear">
          <template #icon><n-icon :component="TrashOutline" /></template>
          清空日志
        </n-button>
      </div>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: any) => row.id" striped size="small" remote
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>
  </div>
</template>