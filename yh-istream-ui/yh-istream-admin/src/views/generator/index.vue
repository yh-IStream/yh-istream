<script setup lang="ts">
import {
  SearchOutline, RefreshOutline, DownloadOutline, EyeOutline, CodeOutline,
} from '@vicons/ionicons5'
import {
  getTableList, previewCode, downloadCode, batchPreviewCode, batchDownloadCode,
  type TableInfo, type GenRequest,
} from '@/api/modules/generator'

const message = useMessage()

// ==================== 状态 ====================
const loading = ref(false)
const tableList = ref<TableInfo[]>([])
const selectedTable = ref<string>('')
const keyword = ref('')

// 生成配置
const genConfig = reactive<GenRequest>({
  author: 'isteam',
  moduleName: 'system',
  packageName: 'com.istream.system',
  controllerPackage: 'com.istream.admin.controller',
  genMigration: true,
})

// 预览
const previewLoading = ref(false)
const previewData = ref<Record<string, string>>({})
const activeFile = ref('')

// 批量
const selectedTables = ref<string[]>([])
const batchPreviewData = ref<Record<string, Record<string, string>>>({})
const batchActiveTab = ref('')

// ==================== 计算属性 ====================
const filteredTables = computed(() => {
  if (!keyword.value) return tableList.value
  return tableList.value.filter(t =>
    t.tableName.toLowerCase().includes(keyword.value.toLowerCase()) ||
    (t.tableComment && t.tableComment.includes(keyword.value)),
  )
})

const previewFiles = computed(() => Object.keys(previewData.value))

// ==================== 方法 ====================
async function fetchTables() {
  loading.value = true
  try {
    const res: any = await getTableList()
    tableList.value = res.data ?? []
  } catch (e: any) { message.error(e.message || '获取表列表失败') }
  finally { loading.value = false }
}

async function handlePreview() {
  if (!selectedTable.value) {
    message.warning('请先选择数据表')
    return
  }
  previewLoading.value = true
  try {
    const res: any = await previewCode(selectedTable.value, genConfig)
    previewData.value = res.data ?? {}
    if (previewFiles.value.length > 0) {
      activeFile.value = previewFiles.value[0]
    }
  } catch (e: any) { message.error(e.message || '预览失败') }
  finally { previewLoading.value = false }
}

async function handleDownload() {
  if (!selectedTable.value) {
    message.warning('请先选择数据表')
    return
  }
  try {
    const res = await downloadCode(selectedTable.value, genConfig)
    const blob = new Blob([res as any], { type: 'application/zip' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${selectedTable.value}.zip`
    a.click()
    URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch (e: any) { message.error(e.message || '下载失败') }
}

async function handleBatchPreview() {
  if (selectedTables.value.length === 0) {
    message.warning('请先选择数据表')
    return
  }
  previewLoading.value = true
  try {
    const res: any = await batchPreviewCode({
      ...genConfig,
      tableNames: selectedTables.value,
    })
    batchPreviewData.value = res.data ?? {}
    const tableKeys = Object.keys(batchPreviewData.value)
    if (tableKeys.length > 0) {
      batchActiveTab.value = tableKeys[0]
    }
  } catch (e: any) { message.error(e.message || '批量预览失败') }
  finally { previewLoading.value = false }
}

async function handleBatchDownload() {
  if (selectedTables.value.length === 0) {
    message.warning('请先选择数据表')
    return
  }
  try {
    const res = await batchDownloadCode({
      ...genConfig,
      tableNames: selectedTables.value,
    })
    const blob = new Blob([res as any], { type: 'application/zip' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'generator-output.zip'
    a.click()
    URL.revokeObjectURL(url)
    message.success('批量下载成功')
  } catch (e: any) { message.error(e.message || '批量下载失败') }
}

function getFileExtension(filename: string): string {
  const ext = filename.split('.').pop() ?? ''
  const map: Record<string, string> = { java: 'java', sql: 'sql', xml: 'xml', yml: 'yaml', yaml: 'yaml', properties: 'properties', vue: 'html', ts: 'typescript', json: 'json' }
  return map[ext] ?? 'text'
}

onMounted(() => fetchTables())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <!-- 配置区域 -->
    <div class="card">
      <h3 class="text-base font-semibold mb-12px">生成配置</h3>
      <n-form inline label-placement="left" :show-feedback="false">
        <n-form-item label="作者">
          <n-input v-model:value="genConfig.author" placeholder="作者" style="width: 120px" />
        </n-form-item>
        <n-form-item label="模块名">
          <n-input v-model:value="genConfig.moduleName" placeholder="模块名" style="width: 120px" />
        </n-form-item>
        <n-form-item label="包名">
          <n-input v-model:value="genConfig.packageName" placeholder="基础包名" style="width: 200px" />
        </n-form-item>
        <n-form-item label="Controller包">
          <n-input v-model:value="genConfig.controllerPackage" placeholder="Controller包名" style="width: 220px" />
        </n-form-item>
        <n-form-item label="生成迁移SQL">
          <n-switch v-model:value="genConfig.genMigration" />
        </n-form-item>
      </n-form>
    </div>

    <!-- 表列表 + 操作 -->
    <div class="flex gap-12px">
      <!-- 左侧：表列表 -->
      <div class="card flex-1 max-w-320px">
        <div class="flex items-center justify-between mb-12px">
          <h3 class="text-base font-semibold">数据表列表</h3>
          <n-button size="small" @click="fetchTables">
            <template #icon><n-icon :component="RefreshOutline" /></template>
          </n-button>
        </div>
        <n-input v-model:value="keyword" placeholder="搜索表名..." clearable class="mb-8px" />
        <div class="mb-8px">
          <n-space>
            <n-button size="small" type="primary" @click="handlePreview" :disabled="!selectedTable" :loading="previewLoading">
              <template #icon><n-icon :component="EyeOutline" /></template>
              预览
            </n-button>
            <n-button size="small" @click="handleDownload" :disabled="!selectedTable">
              <template #icon><n-icon :component="DownloadOutline" /></template>
              下载
            </n-button>
          </n-space>
        </div>
        <div class="mb-8px">
          <n-button size="small" @click="handleBatchPreview" :disabled="selectedTables.length === 0" :loading="previewLoading">
            <template #icon><n-icon :component="CodeOutline" /></template>
            批量预览
          </n-button>
          <n-button size="small" class="ml-8px" @click="handleBatchDownload" :disabled="selectedTables.length === 0">
            <template #icon><n-icon :component="DownloadOutline" /></template>
            批量下载
          </n-button>
        </div>

        <n-data-table
          :columns="[
            { type: 'selection' as const },
            { title: '表名', key: 'tableName', width: 140, ellipsis: { tooltip: true } },
            { title: '注释', key: 'tableComment', width: 120, ellipsis: { tooltip: true } },
          ]"
          :data="filteredTables"
          :loading="loading"
          :row-key="(row: TableInfo) => row.tableName"
          :checked-row-keys="selectedTables"
          max-height="500"
          size="small"
          striped
          @update:checked-row-keys="(keys: any[]) => selectedTables = keys"
        />
      </div>

      <!-- 右侧：预览区域 -->
      <div class="card flex-1 min-w-0">
        <h3 class="text-base font-semibold mb-12px">代码预览</h3>

        <!-- 单表预览 -->
        <div v-if="Object.keys(batchPreviewData).length === 0">
          <n-tabs v-if="previewFiles.length > 0" v-model:value="activeFile" type="card" size="small">
            <n-tab-pane v-for="file in previewFiles" :key="file" :name="file" :tab="file">
              <n-code
                :code="previewData[file] ?? ''"
                :language="getFileExtension(file)"
                show-line-numbers
                style="max-height: 500px"
              />
            </n-tab-pane>
          </n-tabs>
          <div v-if="previewFiles.length === 0 && !previewLoading" class="flex items-center justify-center h-300px text-gray-400">
            请选择左侧数据表，点击"预览"查看生成代码
          </div>
        </div>

        <!-- 批量预览 -->
        <div v-else>
          <n-tabs v-model:value="batchActiveTab" type="card" size="small">
            <n-tab-pane v-for="(files, tableName) in batchPreviewData" :key="tableName" :name="tableName" :tab="tableName">
              <n-tabs v-if="Object.keys(files).length > 0" type="segment" size="small">
                <n-tab-pane v-for="(code, fileName) in files" :key="fileName" :name="fileName" :tab="fileName">
                  <n-code
                    :code="code"
                    :language="getFileExtension(fileName)"
                    show-line-numbers
                    style="max-height: 450px"
                  />
                </n-tab-pane>
              </n-tabs>
            </n-tab-pane>
          </n-tabs>
        </div>
      </div>
    </div>
  </div>
</template>