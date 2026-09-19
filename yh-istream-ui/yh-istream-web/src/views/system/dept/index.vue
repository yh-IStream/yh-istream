<script setup lang="ts">
import { AddOutline, RefreshOutline } from '@vicons/ionicons5'
import { getDeptTree, addDept, updateDept, deleteDept, type SysDept, type SysDeptSave } from '@/api/modules/system'
import type { TreeSelectOption } from 'naive-ui'
import { STATUS, STATUS_LABEL } from '@/constants'
import { useTreeData } from '@/composables/useTreeData'
import { useCrudDialog } from '@/composables/useCrudDialog'

const message = useMessage()

const { renderStatusTag } = useStatusRender()
const { loading, treeData, fetchData } = useTreeData<SysDept>({ api: getDeptTree })

interface DeptFormData {
  id: string | null
  parentId: string
  deptName: string
  leader: string
  phone: string
  email: string
  orderNum: number
  status: number
  remark: string
}

const {
  visible: dialogVisible,
  title: dialogTitle,
  isEdit,
  submitLoading,
  formRef,
  formData,
  openAdd: openAddInner,
  openEdit: openEditInner,
  handleSubmit,
} = useCrudDialog<DeptFormData>({
  defaults: () => ({ id: null, parentId: '0', deptName: '', leader: '', phone: '', email: '', orderNum: 0, status: STATUS.NORMAL, remark: '' }),
  addApi: (data) => addDept(data as SysDeptSave),
  updateApi: (data) => updateDept(data as SysDeptSave),
  onSuccess: fetchData,
  titles: { add: '新增部门', edit: '编辑部门' },
})

const deptTreeSelectData = computed(() => {
  const filterTree = (list: SysDept[]): TreeSelectOption[] =>
    list
      .filter(item => String(item.id) !== String(formData.id))
      .map(item => ({
        key: String(item.id),
        label: item.deptName,
        children: item.children && item.children.length ? filterTree(item.children) : undefined,
      }))
  return [{ key: '0', label: '顶级部门', children: filterTree(treeData.value) }]
})

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  orderNum: [{ required: true, type: 'number' as const, message: '请输入排序', trigger: 'blur' }],
}

const columns = [
  { title: '部门名称', key: 'deptName', tree: true, minWidth: 160 },
  { title: '负责人', key: 'leader', minWidth: 80, ellipsis: { tooltip: true } },
  { title: '联系电话', key: 'phone', minWidth: 110, ellipsis: { tooltip: true } },
  { title: '邮箱', key: 'email', minWidth: 120, ellipsis: { tooltip: true } },
  { title: '排序', key: 'orderNum', width: 60, align: 'center' as const },
  {
    title: '状态', key: 'status', width: 70, align: 'center' as const,
    render: (row: SysDept) => renderStatusTag(row.status),
  },
  {
    title: '操作', key: 'actions', width: 180, fixed: 'right' as const,
    render: (row: SysDept) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleAdd(row) }, { default: () => '新增' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？',
      }),
    ]),
  },
]

function handleAdd(parent?: SysDept) {
  openAddInner()
  formData.parentId = parent ? String(parent.id) : '0'
}

function handleEdit(row: SysDept) {
  openEditInner({
    id: row.id,
    parentId: row.parentId != null ? String(row.parentId) : '0',
    deptName: row.deptName,
    leader: row.leader ?? '',
    phone: row.phone ?? '',
    email: row.email ?? '',
    orderNum: row.orderNum ?? 0,
    status: row.status ?? 0,
    remark: row.remark ?? '',
  })
}

async function handleDelete(id: string) {
  try { await deleteDept(id); message.success('删除成功'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message || '删除失败') }
}

onMounted(() => fetchData())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <div class="flex items-center justify-between mb-12px">
        <n-button type="primary" @click="handleAdd()" v-permission="'system:dept:add'"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
        <n-button @click="fetchData"><template #icon><n-icon :component="RefreshOutline" /></template>刷新</n-button>
      </div>
      <n-data-table :columns="columns" :data="treeData" :loading="loading" :row-key="(row: SysDept) => row.id" striped size="small" default-expand-all />
    </div>

    <n-modal v-model:show="dialogVisible" :title="dialogTitle" preset="card" style="width: 560px" :mask-closable="false">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80px">
        <n-form-item label="上级部门" path="parentId">
          <n-tree-select v-model:value="formData.parentId" :options="deptTreeSelectData" :default-expand-all="false" placeholder="请选择上级部门" />
        </n-form-item>
        <n-form-item label="部门名称" path="deptName">
          <n-input v-model:value="formData.deptName" placeholder="请输入部门名称" />
        </n-form-item>
        <n-form-item label="负责人" path="leader">
          <n-input v-model:value="formData.leader" placeholder="请输入负责人" />
        </n-form-item>
        <n-form-item label="联系电话" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="排序" path="orderNum">
          <n-input-number v-model:value="formData.orderNum" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="STATUS.NORMAL" :unchecked-value="STATUS.DISABLED">
            <template #checked>{{ STATUS_LABEL[STATUS.NORMAL] }}</template><template #unchecked>{{ STATUS_LABEL[STATUS.DISABLED] }}</template>
          </n-switch>
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