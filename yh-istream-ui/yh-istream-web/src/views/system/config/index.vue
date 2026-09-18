<script setup lang="ts">
import { SearchOutline, AddOutline, RefreshOutline } from '@vicons/ionicons5'
import { getConfigList, addConfig, updateConfig, deleteConfig, type SysConfig, type SysConfigSave } from '@/api/modules/system'
import { useTable } from '@/composables/useTable'
import { useCrudDialog } from '@/composables/useCrudDialog'

const message = useMessage()

const searchForm = reactive({ configName: '', configKey: '' })
const searchDefaults = { configName: '', configKey: '' }
const { loading, tableData, selectedIds, pagination, fetchData, handleSearch, handleReset, handlePageChange, handlePageSizeChange } = useTable<SysConfig, typeof searchForm>({
  api: getConfigList,
  searchForm,
  searchDefaults,
})

interface ConfigFormData {
  id: string | null
  configName: string
  configKey: string
  configValue: string
  remark: string
}

const { visible: dialogVisible, title: dialogTitle, isEdit, submitLoading, formRef, formData, openAdd, openEdit, handleSubmit } = useCrudDialog<ConfigFormData>({
  defaults: () => ({ id: null, configName: '', configKey: '', configValue: '', remark: '' }),
  addApi: (data) => addConfig(data as SysConfigSave),
  updateApi: (data) => updateConfig(data as SysConfigSave),
  onSuccess: fetchData,
  titles: { add: '新增配置', edit: '编辑配置' },
})

const rules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
}

const columns = [
  { type: 'selection' as const },
  { title: '配置名称', key: 'configName', minWidth: 120, ellipsis: { tooltip: true } },
  { title: '配置键', key: 'configKey', minWidth: 140, ellipsis: { tooltip: true } },
  { title: '配置值', key: 'configValue', minWidth: 160, ellipsis: { tooltip: true } },
  { title: '备注', key: 'remark', minWidth: 100, ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'createTime', minWidth: 150, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 150, fixed: 'right' as const,
    render: (row: SysConfig) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？',
      }),
    ]),
  },
]

async function handleDelete(id: string) {
  try { await deleteConfig(id); message.success('删除成功'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message || '删除失败') }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    message.warning('请选择要删除的配置')
    return
  }
  try {
    await deleteConfig(selectedIds.value)
    message.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch (e: unknown) {
    message.error((e as Error).message || '批量删除失败')
  }
}

onMounted(() => fetchData())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <n-form inline label-placement="left" :show-feedback="false">
        <n-form-item label="配置名称">
          <n-input v-model:value="searchForm.configName" placeholder="请输入" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="配置键">
          <n-input v-model:value="searchForm.configKey" placeholder="请输入" clearable style="width: 160px" />
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
      <div class="mb-12px flex items-center justify-between">
        <n-space>
          <n-button type="primary" @click="openAdd" v-permission="'system:config:add'"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
          <n-popconfirm @positive-click="handleBatchDelete">
            <template #trigger>
              <n-button type="error" :disabled="selectedIds.length === 0" v-permission="'system:config:delete'">批量删除</n-button>
            </template>
            确认删除选中的 {{ selectedIds.length }} 条配置吗？
          </n-popconfirm>
        </n-space>
      </div>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: SysConfig) => row.id" :checked-row-keys="selectedIds" striped size="small" remote
        @update:checked-row-keys="(keys: (string | number)[]) => selectedIds = keys as string[]"
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>

    <n-modal v-model:show="dialogVisible" :title="dialogTitle" preset="card" style="width: 560px" :mask-closable="false">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80px">
        <n-form-item label="配置名称" path="configName">
          <n-input v-model:value="formData.configName" placeholder="请输入配置名称" />
        </n-form-item>
        <n-form-item label="配置键" path="configKey">
          <n-input v-model:value="formData.configKey" placeholder="请输入配置键" :disabled="isEdit" />
        </n-form-item>
        <n-form-item label="配置值" path="configValue">
          <n-input v-model:value="formData.configValue" type="textarea" placeholder="请输入配置值" :rows="4" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit(() => formRef?.validate())">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>