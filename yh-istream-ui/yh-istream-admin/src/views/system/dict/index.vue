<script setup lang="ts">
import { AddOutline } from '@vicons/ionicons5'
import { getDictTypeList, addDictType, updateDictType, deleteDictType, getDictDataList, addDictData, updateDictData, deleteDictData } from '@/api/modules/system'
import { STATUS, STATUS_LABEL, LIST_CLASS_OPTIONS } from '@/constants'
import { useDict, renderDictTag } from '@/composables/useDict'

const message = useMessage()
const { renderStatusTag } = useStatusRender()
const { clearDict } = useDict()
const { pagination: typePagination, resetPage: resetTypePage, setPage: setTypePage, setPageSize: setTypePageSize } = usePagination()
const { pagination: dataPagination, resetPage: resetDataPage, setPage: setDataPage, setPageSize: setDataPageSize } = usePagination()
const activeTab = ref('type')

// ====== 字典类型 ======
const typeLoading = ref(false)
const typeData = ref<any[]>([])
const typeDialogVisible = ref(false)
const typeIsEdit = ref(false)
const typeFormRef = ref()
const typeForm = reactive({ id: null as string | null, dictName: '', dictType: '', status: 0, remark: '' })
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
}

const typeColumns = [
  { title: '字典名称', key: 'dictName', width: 160 },
  { title: '字典类型', key: 'dictType', width: 200, ellipsis: { tooltip: true } },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => renderStatusTag(row.status),
  },
  { title: '备注', key: 'remark', width: 160, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 200, fixed: 'right' as const,
    render: (row: any) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleTypeEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => { selectedDictType.value = row.dictType; dataSearchForm.dictType = row.dictType; activeTab.value = 'data'; fetchDataList() } }, { default: () => '数据' }),
      h(NPopconfirm, { onPositiveClick: () => handleTypeDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '删除类型将同时删除其下所有字典数据，确认删除？',
      }),
    ]),
  },
]

async function fetchTypeList() {
  typeLoading.value = true
  try {
    const res: any = await getDictTypeList({ pageNum: typePagination.page, pageSize: typePagination.pageSize })
    typeData.value = res.data?.records ?? []
    typePagination.itemCount = res.data?.total ?? 0
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { typeLoading.value = false }
}

function handleTypeAdd() {
  typeIsEdit.value = false
  Object.assign(typeForm, { id: null, dictName: '', dictType: '', status: STATUS.NORMAL, remark: '' })
  typeDialogVisible.value = true
}

function handleTypeEdit(row: any) {
  typeIsEdit.value = true
  Object.assign(typeForm, { id: row.id, dictName: row.dictName, dictType: row.dictType, status: row.status, remark: row.remark ?? '' })
  typeDialogVisible.value = true
}

async function handleTypeSubmit() {
  try { await typeFormRef.value?.validate() } catch { return }
  try {
    const data: any = { ...typeForm }
    if (typeForm.id) { data.id = typeForm.id; await updateDictType(data); message.success('修改成功') }
    else { await addDictType(data); message.success('新增成功') }
    typeDialogVisible.value = false; fetchTypeList()
  } catch (e: any) { message.error(e.message || '操作失败') }
}

async function handleTypeDelete(id: string) {
  try { await deleteDictType(id); message.success('删除成功'); clearDict(); fetchTypeList() }
  catch (e: any) { message.error(e.message || '删除失败') }
}

// ====== 字典数据 ======
const selectedDictType = ref('')
const dataLoading = ref(false)
const dataList = ref<any[]>([])
const dataSearchForm = reactive({ dictType: '', dictLabel: '' })
const dataDialogVisible = ref(false)
const dataIsEdit = ref(false)
const dataFormRef = ref()
const dataForm = reactive({ id: null as string | null, dictType: '', dictLabel: '', dictValue: '', orderNum: 0, cssClass: '', listClass: '', status: 0, remark: '' })
const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  orderNum: [{ required: true, type: 'number' as const, message: '请输入排序', trigger: 'blur' }],
}

const dataColumns = [
  { title: '字典标签', key: 'dictLabel', width: 140 },
  { title: '字典值', key: 'dictValue', width: 140 },
  {
    title: '样式', key: 'listClass', width: 100, align: 'center' as const,
    render: (row: any) => renderDictTag({ label: row.dictLabel, listClass: row.listClass, cssClass: row.cssClass }),
  },
  { title: '排序', key: 'orderNum', width: 60, align: 'center' as const },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => renderStatusTag(row.status),
  },
  { title: '备注', key: 'remark', width: 160 },
  {
    title: '操作', key: 'actions', width: 150, fixed: 'right' as const,
    render: (row: any) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleDataEdit(row) }, { default: () => '编辑' }),
      h(NPopconfirm, { onPositiveClick: () => handleDataDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？',
      }),
    ]),
  },
]

async function fetchDataList() {
  if (!dataSearchForm.dictType) return
  dataLoading.value = true
  try {
    const res: any = await getDictDataList({ pageNum: dataPagination.page, pageSize: dataPagination.pageSize, dictType: dataSearchForm.dictType, dictLabel: dataSearchForm.dictLabel || undefined })
    dataList.value = res.data?.records ?? []
    dataPagination.itemCount = res.data?.total ?? 0
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { dataLoading.value = false }
}

function handleDataAdd() {
  dataIsEdit.value = false
  Object.assign(dataForm, { id: null, dictType: selectedDictType.value, dictLabel: '', dictValue: '', orderNum: 0, cssClass: '', listClass: '', status: STATUS.NORMAL, remark: '' })
  dataDialogVisible.value = true
}

function handleDataEdit(row: any) {
  dataIsEdit.value = true
  Object.assign(dataForm, { id: row.id, dictType: row.dictType, dictLabel: row.dictLabel, dictValue: row.dictValue, orderNum: row.orderNum, cssClass: row.cssClass ?? '', listClass: row.listClass ?? '', status: row.status, remark: row.remark ?? '' })
  dataDialogVisible.value = true
}

async function handleDataSubmit() {
  try { await dataFormRef.value?.validate() } catch { return }
  try {
    const data: any = { ...dataForm }
    if (dataForm.id) { data.id = dataForm.id; await updateDictData(data); message.success('修改成功') }
    else { await addDictData(data); message.success('新增成功') }
    clearDict(dataForm.dictType)
    dataDialogVisible.value = false; fetchDataList()
  } catch (e: any) { message.error(e.message || '操作失败') }
}

async function handleDataDelete(id: string) {
  try { await deleteDictData(id); message.success('删除成功'); clearDict(selectedDictType.value); fetchDataList() }
  catch (e: any) { message.error(e.message || '删除失败') }
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
          :row-key="(row: any) => row.id" striped size="small" remote
          @update:page="(p: number) => { setTypePage(p); fetchTypeList() }"
          @update:page-size="(s: number) => { setTypePageSize(s); fetchTypeList() }" />
      </n-tab-pane>

      <n-tab-pane name="data" tab="字典数据" :disabled="!selectedDictType">
        <div class="mb-12px flex items-center gap-12px">
          <n-button type="primary" @click="handleDataAdd" :disabled="!selectedDictType" v-permission="'system:dict:add'">
            <template #icon><n-icon :component="AddOutline" /></template>新增
          </n-button>
          <n-input v-model:value="dataSearchForm.dictLabel" placeholder="搜索字典标签" clearable size="small" style="width: 180px" @update:value="() => { resetDataPage(); fetchDataList() }" />
          <span v-if="selectedDictType" class="text-sm text-gray-500">当前类型：{{ selectedDictType }}</span>
        </div>
        <n-data-table :columns="dataColumns" :data="dataList" :loading="dataLoading" :pagination="dataPagination"
          :row-key="(row: any) => row.id" striped size="small" remote
          @update:page="(p: number) => { setDataPage(p); fetchDataList() }"
          @update:page-size="(s: number) => { setDataPageSize(s); fetchDataList() }" />
      </n-tab-pane>
    </n-tabs>

    <!-- 字典类型弹窗 -->
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
          <n-button type="primary" @click="handleTypeSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 字典数据弹窗 -->
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
          <n-select v-model:value="dataForm.listClass" :options="LIST_CLASS_OPTIONS as any" placeholder="预设主题或自定义hex色值" clearable filterable tag />
        </n-form-item>
        <n-form-item label="CSS类名" path="cssClass">
          <n-input v-model:value="dataForm.cssClass" placeholder="自定义CSS类名，如 text-red" />
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