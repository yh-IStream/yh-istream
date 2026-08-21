<script setup lang="ts">
import { SearchOutline, AddOutline, RefreshOutline } from '@vicons/ionicons5'
import {
  getRoleList, addRole, updateRole, deleteRole, getRoleMenuTree, assignRoleMenu,
} from '@/api/modules/system'
import { STATUS, STATUS_OPTIONS, STATUS_LABEL } from '@/constants'

const message = useMessage()
const dialog = useDialog()
const { pagination, resetPage, setPage, setPageSize } = usePagination()
const { renderStatusTag } = useStatusRender()

const loading = ref(false)
const tableData = ref<any[]>([])
const selectedIds = ref<number[]>([])

const searchForm = reactive({ roleName: '', roleKey: '', status: null as number | null })

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({ id: null as number | null, roleName: '', roleKey: '', roleSort: 0, status: 0, remark: '' })

const menuDialogVisible = ref(false)
const menuRoleId = ref<number>()
const menuTree = ref<any[]>([])
const checkedMenuKeys = ref<number[]>([])

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
  roleSort: [{ required: true, type: 'number', message: '请输入排序', trigger: 'blur' }],
}

const columns = [
  { type: 'selection' as const },
  { title: '角色名称', key: 'roleName', width: 140 },
  { title: '角色标识', key: 'roleKey', width: 160 },
  { title: '排序', key: 'roleSort', width: 80, align: 'center' as const },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => renderStatusTag(row.status),
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 260, fixed: 'right' as const,
    render: (row: any) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => handleMenuAssign(row) }, { default: () => '分配菜单' }),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除该角色吗？',
      }),
    ]),
  },
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.roleName) params.roleName = searchForm.roleName
    if (searchForm.roleKey) params.roleKey = searchForm.roleKey
    if (searchForm.status !== null) params.status = searchForm.status
    const res: any = await getRoleList(params)
    tableData.value = res.data?.records ?? []
    pagination.itemCount = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '查询失败')
  } finally { loading.value = false }
}

function handleSearch() { resetPage(); fetchData() }
function handleReset() { searchForm.roleName = ''; searchForm.roleKey = ''; searchForm.status = null; resetPage(); fetchData() }
function handlePageChange(page: number) { setPage(page); fetchData() }
function handlePageSizeChange(size: number) { setPageSize(size); fetchData() }

function handleAdd() {
  isEdit.value = false; dialogTitle.value = '新增角色'
  Object.assign(formData, { id: null, roleName: '', roleKey: '', roleSort: 0, status: STATUS.NORMAL, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true; dialogTitle.value = '编辑角色'
  Object.assign(formData, { id: row.id, roleName: row.roleName, roleKey: row.roleKey, roleSort: row.roleSort, status: row.status, remark: row.remark ?? '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value?.validate() } catch { return }
  submitLoading.value = true
  try {
    const data: any = { roleName: formData.roleName, roleKey: formData.roleKey, roleSort: formData.roleSort, status: formData.status, remark: formData.remark }
    if (formData.id) { data.id = formData.id; await updateRole(data); message.success('修改成功') }
    else { await addRole(data); message.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e: any) { message.error(e.message || '操作失败') }
  finally { submitLoading.value = false }
}

async function handleDelete(id: number) {
  try { await deleteRole(id); message.success('删除成功'); fetchData() }
  catch (e: any) { message.error(e.message || '删除失败') }
}

async function handleMenuAssign(row: any) {
  menuRoleId.value = row.id
  try {
    const res: any = await getRoleMenuTree(row.id)
    menuTree.value = res.data?.menus ?? []
    checkedMenuKeys.value = res.data?.checkedKeys ?? []
  } catch { menuTree.value = []; checkedMenuKeys.value = [] }
  menuDialogVisible.value = true
}

async function handleMenuSubmit() {
  if (!menuRoleId.value) return
  try {
    await assignRoleMenu(menuRoleId.value, checkedMenuKeys.value)
    message.success('菜单分配成功'); menuDialogVisible.value = false
  } catch (e: any) { message.error(e.message || '分配失败') }
}

onMounted(() => fetchData())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <n-form inline label-placement="left" :show-feedback="false">
        <n-form-item label="角色名称">
          <n-input v-model:value="searchForm.roleName" placeholder="请输入角色名称" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="角色标识">
          <n-input v-model:value="searchForm.roleKey" placeholder="请输入角色标识" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="searchForm.status" :options="STATUS_OPTIONS" placeholder="请选择" clearable style="width: 120px" />
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
      <div class="flex items-center justify-between mb-12px">
        <n-button type="primary" @click="handleAdd"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
      </div>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :pagination="pagination"
        :row-key="(row: any) => row.id" :checked-row-keys="selectedIds" striped size="small" remote
        @update:checked-row-keys="(keys: any[]) => selectedIds = keys"
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </div>

    <n-modal v-model:show="dialogVisible" :title="dialogTitle" preset="card" style="width: 560px" :mask-closable="false">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80px">
        <n-form-item label="角色名称" path="roleName">
          <n-input v-model:value="formData.roleName" placeholder="请输入角色名称" />
        </n-form-item>
        <n-form-item label="角色标识" path="roleKey">
          <n-input v-model:value="formData.roleKey" placeholder="请输入角色标识" :disabled="isEdit" />
        </n-form-item>
        <n-form-item label="排序" path="roleSort">
          <n-input-number v-model:value="formData.roleSort" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="STATUS.NORMAL" :unchecked-value="STATUS.DISABLED">
            <template #checked>{{ STATUS_LABEL[STATUS.NORMAL] }}</template><template #unchecked>{{ STATUS_LABEL[STATUS.DISABLED] }}</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" :rows="3" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="menuDialogVisible" title="分配菜单权限" preset="card" style="width: 480px" :mask-closable="false">
      <n-tree
        v-model:checked-keys="checkedMenuKeys"
        :data="menuTree"
        checkable
        cascade
        key-field="id"
        label-field="label"
        children-field="children"
        default-expand-all
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="menuDialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handleMenuSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>