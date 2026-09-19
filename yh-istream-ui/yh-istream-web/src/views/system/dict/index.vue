<script setup lang="ts">
import { AddOutline } from '@vicons/ionicons5'
import { getDictTypeList, addDictType, updateDictType, deleteDictType, getDictDataList, addDictData, updateDictData, deleteDictData, type SysDictType, type SysDictTypeSave, type SysDictData, type SysDictDataSave } from '@/api/modules/system'
import { STATUS, STATUS_LABEL, LIST_CLASS_OPTIONS } from '@/constants'
import { useDict, renderDictTag } from '@/composables/useDict'
import { useTable } from '@/composables/useTable'
import { useCrudDialog } from '@/composables/useCrudDialog'

const message = useMessage()

const { renderStatusTag } = useStatusRender()
const { clearDict } = useDict()
const activeTab = ref('type')

// ====== 字典类型 ======
const typeSearchForm = reactive({ dictName: '', dictType: '' })
const { loading: typeLoading, tableData: typeData, pagination: typePagination, fetchData: fetchTypeList, handlePageChange: handleTypePageChange, handlePageSizeChange: handleTypePageSizeChange } = useTable<SysDictType, typeof typeSearchForm>({
  api: getDictTypeList,
  searchForm: typeSearchForm,
})

interface DictTypeFormData {
  id: string | null
  dictName: string
  dictType: string
  status: number
  remark: string
}

const {
  visible: typeDialogVisible,
  isEdit: typeIsEdit,
  formRef: typeFormRef,
  formData: typeForm,
  openAdd: handleTypeAddInner,
  openEdit: handleTypeEditInner,
  handleSubmit: handleTypeSubmit,
} = useCrudDialog<DictTypeFormData>({
  defaults: () => ({ id: null, dictName: '', dictType: '', status: STATUS.NORMAL, remark: '' }),
  addApi: (data) => addDictType(data as SysDictTypeSave),
  updateApi: (data) => updateDictType(data as SysDictTypeSave),
  onSuccess: fetchTypeList,
})

function handleTypeAdd() {
  handleTypeAddInner()
}

function handleTypeEdit(row: SysDictType) {
  handleTypeEditInner(row)
}

const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
}

const typeColumns = [
  { title: '字典名称', key: 'dictName', minWidth: 120, ellipsis: { tooltip: true } },
  { title: '字典类型', key: 'dictType', minWidth: 140, ellipsis: { tooltip: true } },
  {
    title: '状态', key: 'status', width: 70, align: 'center' as const,
    render: (row: SysDictType) => renderStatusTag(row.status),
  },
  { title: '备注', key: 'remark', minWidth: 100, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 200, fixed: 'right' as const,
    render: (row: SysDictType) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleTypeEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => { selectedDictType.value = row.dictType; dataSearchForm.dictType = row.dictType; dataSearchForm.dictLabel = ''; activeTab.value = 'data'; handleDataReset(dataSearchForm) } }, { default: () => '数据' }),
      h(NPopconfirm, { onPositiveClick: () => handleTypeDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '删除类型将同时删除其下所有字典数据，确认删除？',
      }),
    ]),
  },
]

async function handleTypeDelete(id: string) {
  try { await deleteDictType(id); message.success('删除成功'); clearDict(); fetchTypeList() }
  catch (e: unknown) { message.error((e as Error).message || '删除失败') }
}

// ====== 字典数据 ======
const selectedDictType = ref('')
const dataSearchForm = reactive({ dictType: '', dictLabel: '' })
const { loading: dataLoading, tableData: dataList, pagination: dataPagination, fetchData: fetchDataList, handleSearch: handleDataSearch, handlePageChange: handleDataPageChange, handlePageSizeChange: handleDataPageSizeChange, handleReset: handleDataReset } = useTable<SysDictData, typeof dataSearchForm>({
  api: getDictDataList,
  searchForm: dataSearchForm,
})

interface DictDataFormData {
  id: string | null
  dictType: string
  dictLabel: string
  dictValue: string
  orderNum: number
  cssClass: string
  listClass: string
  isDefault: number
  status: number
  remark: string
}

const {
  visible: dataDialogVisible,
  formRef: dataFormRef,
  formData: dataForm,
  openAdd: handleDataAddInner,
  openEdit: handleDataEditInner,
  handleSubmit: handleDataSubmitInner,
} = useCrudDialog<DictDataFormData>({
  defaults: () => ({ id: null, dictType: selectedDictType.value, dictLabel: '', dictValue: '', orderNum: 0, cssClass: '', listClass: '', isDefault: 0, status: STATUS.NORMAL, remark: '' }),
  addApi: (data) => addDictData(data as SysDictDataSave),
  updateApi: (data) => updateDictData(data as SysDictDataSave),
  onSuccess: () => { clearDict(dataForm.dictType); fetchDataList() },
})

function handleDataAdd() {
  handleDataAddInner()
}

function handleDataEdit(row: SysDictData) {
  handleDataEditInner(row)
}

async function handleDataSubmit() {
  await handleDataSubmitInner(() => dataFormRef.value?.validate())
}

const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  orderNum: [{ required: true, type: 'number' as const, message: '请输入排序', trigger: 'blur' }],
}

const dataColumns = [
  { title: '字典标签', key: 'dictLabel', minWidth: 100, ellipsis: { tooltip: true } },
  { title: '字典值', key: 'dictValue', minWidth: 100, ellipsis: { tooltip: true } },
  {
    title: '样式', key: 'listClass', width: 80, align: 'center' as const,
    render: (row: SysDictData) => renderDictTag({ label: row.dictLabel, listClass: row.listClass, cssClass: row.cssClass }),
  },
  { title: '排序', key: 'orderNum', width: 60, align: 'center' as const },
  {
    title: '状态', key: 'status', width: 70, align: 'center' as const,
    render: (row: SysDictData) => renderStatusTag(row.status),
  },
  { title: '备注', key: 'remark', minWidth: 80, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 150, fixed: 'right' as const,
    render: (row: SysDictData) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleDataEdit(row) }, { default: () => '编辑' }),
      h(NPopconfirm, { onPositiveClick: () => handleDataDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？',
      }),
    ]),
  },
]

async function handleDataDelete(id: string) {
  try { await deleteDictData(id); message.success('删除成功'); clearDict(selectedDictType.value); fetchDataList() }
  catch (e: unknown) { message.error((e as Error).message || '删除失败') }
}

function handleTabChange(tab: string) {
  if (tab === 'type') fetchTypeList()
  else if (tab === 'data' && selectedDictType.value) fetchDataList()
}

onMounted(() => fetchTypeList())
</script>

<template>
  <div class="card">
    <n-tabs v-model:value="activeTab" @update:value="handleTabChange">
      <n-tab-pane name="type" tab="字典类型">
        <div class="mb-12px">
          <n-button type="primary" @click="handleTypeAdd" v-permission="'system:dict:add'"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
        </div>
        <n-data-table :columns="typeColumns" :data="typeData" :loading="typeLoading" :pagination="typePagination"
          :row-key="(row: SysDictType) => row.id" striped size="small" remote
          @update:page="handleTypePageChange"
          @update:page-size="handleTypePageSizeChange" />
      </n-tab-pane>

      <n-tab-pane name="data" tab="字典数据" :disabled="!selectedDictType">
        <div class="mb-12px flex items-center gap-12px">
          <n-button type="primary" @click="handleDataAdd" :disabled="!selectedDictType" v-permission="'system:dict:add'">
            <template #icon><n-icon :component="AddOutline" /></template>新增
          </n-button>
          <n-input v-model:value="dataSearchForm.dictLabel" placeholder="搜索字典标签" clearable size="small" style="width: 180px" @update:value="handleDataSearch" />
          <span v-if="selectedDictType" class="text-sm text-gray-500">当前类型：{{ selectedDictType }}</span>
        </div>
        <n-data-table :columns="dataColumns" :data="dataList" :loading="dataLoading" :pagination="dataPagination"
          :row-key="(row: SysDictType) => row.id" striped size="small" remote
          @update:page="handleDataPageChange"
          @update:page-size="handleDataPageSizeChange" />
      </n-tab-pane>
    </n-tabs>

    <n-modal v-model:show="typeDialogVisible" title="字典类型" preset="card" style="width: 560px" :mask-closable="false">
      <n-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-placement="left" label-width="80px">
        <n-form-item label="字典名称" path="dictName">
          <n-input v-model:value="typeForm.dictName" placeholder="请输入字典名称" />
        </n-form-item>
        <n-form-item label="字典类型" path="dictType">
          <n-input v-model:value="typeForm.dictType" placeholder="请输入字典类型" :disabled="typeIsEdit" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="typeForm.status" :checked-value="STATUS.NORMAL" :unchecked-value="STATUS.DISABLED">
            <template #checked>{{ STATUS_LABEL[STATUS.NORMAL] }}</template><template #unchecked>{{ STATUS_LABEL[STATUS.DISABLED] }}</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="typeForm.remark" type="textarea" placeholder="请输入备注" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="typeDialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handleTypeSubmit(() => typeFormRef?.validate())">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="dataDialogVisible" title="字典数据" preset="card" style="width: 560px" :mask-closable="false">
      <n-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-placement="left" label-width="80px">
        <n-form-item label="字典标签" path="dictLabel">
          <n-input v-model:value="dataForm.dictLabel" placeholder="请输入字典标签" />
        </n-form-item>
        <n-form-item label="字典值" path="dictValue">
          <n-input v-model:value="dataForm.dictValue" placeholder="请输入字典值" />
        </n-form-item>
        <n-form-item label="排序" path="orderNum">
          <n-input-number v-model:value="dataForm.orderNum" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="回显样式" path="listClass">
          <n-select v-model:value="dataForm.listClass" :options="LIST_CLASS_OPTIONS" placeholder="预设主题或自定义hex色值" clearable filterable tag />
        </n-form-item>
        <n-form-item label="CSS类名" path="cssClass">
          <n-input v-model:value="dataForm.cssClass" placeholder="自定义CSS类名，如 text-red" />
        </n-form-item>
        <n-form-item label="是否默认" path="isDefault">
          <n-radio-group v-model:value="dataForm.isDefault">
            <n-radio :value="1">是</n-radio>
            <n-radio :value="0">否</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="dataForm.status" :checked-value="STATUS.NORMAL" :unchecked-value="STATUS.DISABLED">
            <template #checked>{{ STATUS_LABEL[STATUS.NORMAL] }}</template><template #unchecked>{{ STATUS_LABEL[STATUS.DISABLED] }}</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="dataForm.remark" type="textarea" placeholder="请输入备注" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="dataDialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handleDataSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>