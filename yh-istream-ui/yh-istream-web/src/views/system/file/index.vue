<script setup lang="ts">
import { SearchOutline, RefreshOutline, DownloadOutline, CloudUploadOutline } from '@vicons/ionicons5'
import { getFileList, deleteFile, downloadFile as downloadFileApi, uploadFile, type SysFile } from '@/api/modules/system'
import { useTable } from '@/composables/useTable'
import { useExport } from '@/composables/useExport'

const message = useMessage()
const searchForm = reactive({ originalName: '', fileExt: '' })
const searchDefaults = { originalName: '', fileExt: '' }
const { loading, tableData, pagination, fetchData, handleSearch, handleReset, handlePageChange, handlePageSizeChange } = useTable<SysFile, typeof searchForm>({
  api: getFileList,
  searchForm,
  searchDefaults,
})
const { downloadFile } = useExport()
const uploadRef = ref()
const uploadLoading = ref(false)

const columns = [
  { title: '文件名', key: 'originalName', minWidth: 160, ellipsis: { tooltip: true } },
  { title: '文件类型', key: 'fileExt', width: 90 },
  { title: '文件大小', key: 'fileSize', width: 90, render: (row: SysFile) => formatFileSize(row.fileSize) },
  { title: '存储路径', key: 'filePath', minWidth: 140, ellipsis: { tooltip: true } },
  { title: '上传人', key: 'createBy', minWidth: 80, ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'createTime', minWidth: 150, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 180, fixed: 'right' as const,
    render: (row: SysFile) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleDownload(row) }, { default: () => '下载' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => copyUrl(row.storageUrl) }, { default: () => '复制路径' }),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？',
      }),
    ]),
  },
]

function formatFileSize(bytes: string | number): string {
  const num = typeof bytes === 'string' ? Number(bytes) : bytes
  if (!num || num === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(num) / Math.log(1024))
  return (num / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

async function handleDownload(row: SysFile) {
  await downloadFile(() => downloadFileApi(row.id), row.originalName)
}

function copyUrl(text: string) {
  navigator.clipboard.writeText(text).then(() => message.success('已复制到剪贴板'))
}

async function handleDelete(id: string) {
  try { await deleteFile(id); message.success('删除成功'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message || '删除失败') }
}

async function handleUpload({ file }: { file: { file: File | null } }) {
  if (!file.file) return
  const formData = new FormData()
  formData.append('file', file.file)
  uploadLoading.value = true
  try {
    await uploadFile(formData)
    message.success('上传成功')
    fetchData()
  } catch (e: unknown) {
    message.error((e as Error).message || '上传失败')
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
            <n-button @click="handleReset()"><template #icon><n-icon :component="RefreshOutline" /></template>重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>

    <div class="card">
      <div class="mb-12px">
        <n-upload v-permission="'system:file:upload'" :show-file-list="false" :custom-request="handleUpload" :disabled="uploadLoading">
          <n-button type="primary" :loading="uploadLoading">
            <template #icon><n-icon :component="CloudUploadOutline" /></template>
            上传文件
          </n-button>
        </n-upload>
      </div>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: SysFile) => row.id" striped size="small" remote
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>
  </div>
</template>