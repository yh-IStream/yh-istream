<script setup lang="ts">
import { AddOutline, RefreshOutline } from '@vicons/ionicons5'
import { getDeptTree, addDept, updateDept, deleteDept } from '@/api/modules/system'
import { STATUS, STATUS_LABEL } from '@/constants'

const message = useMessage()
const { renderStatusTag } = useStatusRender()
const loading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({ id: null as number | null, parentId: 0, deptName: '', leader: '', phone: '', email: '', sort: 0, status: 0 })

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  sort: [{ required: true, type: 'number', message: '请输入排序', trigger: 'blur' }],
}

const columns = [
  { title: '部门名称', key: 'deptName', tree: true, width: 200 },
  { title: '负责人', key: 'leader', width: 120 },
  { title: '联系电话', key: 'phone', width: 130 },
  { title: '邮箱', key: 'email', width: 180 },
  { title: '排序', key: 'sort', width: 60, align: 'center' as const },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => renderStatusTag(row.status),
  },
  {
    title: '操作', key: 'actions', width: 180, fixed: 'right' as const,
    render: (row: any) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleAdd(row) }, { default: () => '新增' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？',
      }),
    ]),
  },
]

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getDeptTree()
    treeData.value = res.data ?? []
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { loading.value = false }
}

function handleAdd(parent?: any) {
  isEdit.value = false; dialogTitle.value = '新增部门'
  Object.assign(formData, { id: null, parentId: parent ? parent.id : 0, deptName: '', leader: '', phone: '', email: '', sort: 0, status: STATUS.NORMAL })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true; dialogTitle.value = '编辑部门'
  Object.assign(formData, { id: row.id, parentId: row.parentId ?? 0, deptName: row.deptName, leader: row.leader ?? '', phone: row.phone ?? '', email: row.email ?? '', sort: row.sort ?? 0, status: row.status ?? 0 })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value?.validate() } catch { return }
  submitLoading.value = true
  try {
    const data: any = { ...formData, parentId: formData.parentId || 0 }
    if (formData.id) { data.id = formData.id; await updateDept(data); message.success('修改成功') }
    else { await addDept(data); message.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e: any) { message.error(e.message || '操作失败') }
  finally { submitLoading.value = false }
}

async function handleDelete(id: number) {
  try { await deleteDept(id); message.success('删除成功'); fetchData() }
  catch (e: any) { message.error(e.message || '删除失败') }
}

onMounted(() => fetchData())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <div class="flex items-center justify-between mb-12px">
        <n-button type="primary" @click="handleAdd()"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
        <n-button @click="fetchData"><template #icon><n-icon :component="RefreshOutline" /></template>刷新</n-button>
      </div>
      <n-data-table :columns="columns" :data="treeData" :loading="loading" :row-key="(row: any) => row.id" striped size="small" default-expand-all />
    </div>

    <n-modal v-model:show="dialogVisible" :title="dialogTitle" preset="card" style="width: 560px" :mask-closable="false">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80px">
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
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="STATUS.NORMAL" :unchecked-value="STATUS.DISABLED">
            <template #checked>{{ STATUS_LABEL[STATUS.NORMAL] }}</template><template #unchecked>{{ STATUS_LABEL[STATUS.DISABLED] }}</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>