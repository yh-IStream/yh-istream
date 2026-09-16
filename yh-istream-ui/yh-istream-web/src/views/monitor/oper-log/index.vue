<script setup lang="ts">
import { SearchOutline, RefreshOutline, TrashOutline, DownloadOutline } from '@vicons/ionicons5'
import { getOperLogList, clearOperLog, exportOperLog, type SysOperLog } from '@/api/modules/monitor'
import type { SelectOption } from 'naive-ui'
import { SUCCESS_OPTIONS } from '@/constants'
import { useDict } from '@/composables/useDict'
import { useTable } from '@/composables/useTable'
import { useExport } from '@/composables/useExport'

const message = useMessage()
const { renderSuccessTag } = useStatusRender()
const { useDictTag } = useDict()
const { render: renderBusinessTypeTag } = useDictTag('sys_oper_type', '未知')
const { loadDict } = useDict()
const { downloadExcel } = useExport()

const searchForm = reactive({ title: '', businessType: null as number | null, status: null as number | null })
const searchDefaults = { title: '', businessType: null as number | null, status: null as number | null }
const { loading, tableData, pagination, fetchData, handleSearch, handleReset, handlePageChange, handlePageSizeChange } = useTable<SysOperLog, typeof searchForm>({
  api: getOperLogList,
  searchForm,
  searchDefaults,
})

const businessTypeOptions = ref<SelectOption[]>([{ label: '全部', value: undefined }])

onMounted(async () => {
  const list = await loadDict('sys_oper_type')
  businessTypeOptions.value = [
    { label: '全部', value: undefined },
    ...list.map(item => ({ label: item.label, value: Number(item.value) })),
  ]
  fetchData()
})

const columns = [
  { title: '操作标题', key: 'title', minWidth: 120, ellipsis: { tooltip: true } },
  {
    title: '业务类型', key: 'businessType', width: 90, align: 'center' as const,
    render: (row: SysOperLog) => renderBusinessTypeTag(row.businessType),
  },
  { title: '请求方法', key: 'requestMethod', width: 90 },
  { title: '请求URL', key: 'operUrl', minWidth: 140, ellipsis: { tooltip: true } },
  { title: '操作人', key: 'operName', minWidth: 80, ellipsis: { tooltip: true } },
  { title: '操作IP', key: 'operIp', minWidth: 110, ellipsis: { tooltip: true } },
  { title: '操作地点', key: 'operLocation', minWidth: 100, ellipsis: { tooltip: true } },
  {
    title: '状态', key: 'status', width: 70, align: 'center' as const,
    render: (row: SysOperLog) => renderSuccessTag(row.status),
  },
  { title: '操作时间', key: 'operTime', minWidth: 150, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 100, fixed: 'right' as const,
    render: (row: SysOperLog) => h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => showDetail(row) }, { default: () => '详情' }),
  },
]

async function handleClear() {
  try { await clearOperLog(); message.success('清空成功'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message || '清空失败') }
}

async function handleExport() {
  await downloadExcel(exportOperLog, '操作日志.xlsx')
}

const detailVisible = ref(false)
const detailData = ref<SysOperLog>({} as SysOperLog)

function showDetail(row: SysOperLog) {
  detailData.value = row
  detailVisible.value = true
}


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
            <n-button @click="handleReset()"><template #icon><n-icon :component="RefreshOutline" /></template>重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>

    <div class="card">
      <div class="mb-12px flex gap-8px">
        <n-button type="error" ghost @click="handleClear" v-permission="'monitor:oper-log:clean'">
          <template #icon><n-icon :component="TrashOutline" /></template>
          清空日志
        </n-button>
        <n-button @click="handleExport" v-permission="'monitor:oper-log:export'">
          <template #icon><n-icon :component="DownloadOutline" /></template>
          导出
        </n-button>
      </div>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: SysOperLog) => row.id" striped size="small" remote
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>

    <n-modal v-model:show="detailVisible" preset="card" title="操作日志详情" style="width: 600px">
      <n-descriptions label-placement="left" bordered :column="1">
        <n-descriptions-item label="操作标题">{{ detailData.title }}</n-descriptions-item>
        <n-descriptions-item label="业务类型">{{ renderBusinessTypeTag(detailData.businessType) }}</n-descriptions-item>
        <n-descriptions-item label="请求方法">{{ detailData.requestMethod }}</n-descriptions-item>
        <n-descriptions-item label="请求URL">{{ detailData.operUrl }}</n-descriptions-item>
        <n-descriptions-item label="操作人">{{ detailData.operName }}</n-descriptions-item>
        <n-descriptions-item label="操作IP">{{ detailData.operIp }}</n-descriptions-item>
        <n-descriptions-item label="操作地点">{{ detailData.operLocation }}</n-descriptions-item>
        <n-descriptions-item label="请求参数">{{ detailData.operParam ?? '无' }}</n-descriptions-item>
        <n-descriptions-item label="返回参数">{{ detailData.jsonResult ?? '无' }}</n-descriptions-item>
        <n-descriptions-item label="状态">{{ renderSuccessTag(detailData.status) }}</n-descriptions-item>
        <n-descriptions-item label="错误信息">{{ detailData.errorMsg ?? '无' }}</n-descriptions-item>
        <n-descriptions-item label="操作时间">{{ detailData.operTime }}</n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </div>
</template>