<script setup lang="ts">
import { SearchOutline, RefreshOutline, DownloadOutline, CloudUploadOutline } from '@vicons/ionicons5'
import { getFileList, deleteFile, downloadFile, uploadFile } from '@/api/modules/system'

const message = useMessage()
const { pagination, resetPage, setPage, setPageSize } = usePagination()
const loading = ref(false)
const tableData = ref<any[]>([])
const searchForm = reactive({ originalName: '', fileExt: '' })
const uploadRef = ref()
const uploadLoading = ref(false)

const columns = [
  { title: '文件名', key: 'originalName', width: 240, ellipsis: { tooltip: true } },
  { title: '文件类型', key: 'fileExt', width: 120 },
  { title: '文件大小', key: 'fileSize', width: 100, render: (row: any) => formatFileSize(row.fileSize) },
  { title: '存储路径', key: 'filePath', width: 200, ellipsis: { tooltip: true } },
  { title: '上传人', key: 'createBy', width: 120 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 180, fixed: 'right' as const,
    render: (row: any) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleDownload(row) }, { default: () => '下载' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => copyUrl(row.storageUrl) }, { default: () => '复制路径' }),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？',
      }),
    ]),
  },
]

function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.originalName) params.originalName = searchForm.originalName
    if (searchForm.fileExt) params.fileExt = searchForm.fileExt
    const res: any = await getFileList(params)
    tableData.value = res.data?.records ?? []
    pagination.itemCount = res.data?.total ?? 0
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { loading.value = false }
}

function handleSearch() { resetPage(); fetchData() }
function handleReset() { searchForm.originalName = ''; searchForm.fileExt = ''; resetPage(); fetchData() }
function handlePageChange(page: number) { setPage(page); fetchData() }
function handlePageSizeChange(size: number) { setPageSize(size); fetchData() }

async function handleDownload(row: any) {
  try {
    const res = await downloadFile(row.id)
    const blob = new Blob([res as any])
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.originalName
    a.click()
    URL.revokeObjectURL(url)
  } catch (e: any) { message.error(e.message || '下载失败') }
}

function copyUrl(text: string) {
  navigator.clipboard.writeText(text).then(() => message.success('已复制到剪贴板'))
}

async function handleDelete(id: string) {
  try { await deleteFile(id); message.success('删除成功'); fetchData() }
  catch (e: any) { message.error(e.message || '删除失败') }
}

async function handleUpload(options: { file: File }) {
  const formData = new FormData()
  formData.append('file', options.file)
  uploadLoading.value = true
  try {
    await uploadFile(formData)
    message.success('上传成功')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

onMounted(() => fetchData())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <n-form inline label-placement="left" :show-feedback="false">
        <n-form-item label="文件名">
          <n-input v-model:value="searchForm.originalName" placeholder="请输入文件名" clearable style="width: 180px" />
        </n-form-item>
        <n-form-item label="文件类型">
          <n-input v-model:value="searchForm.fileExt" placeholder="请输入文件类型" clearable style="width: 140px" />
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
        <n-upload v-permission="'system:file:upload'" :show-file-list="false" :custom-request="handleUpload as any" :disabled="uploadLoading">
          <n-button type="primary" :loading="uploadLoading">
            <template #icon><n-icon :component="CloudUploadOutline" /></template>
            上传文件
          </n-button>
        </n-upload>
      </div>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: any) => row.id" striped size="small" remote
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>
  </div>
</template>