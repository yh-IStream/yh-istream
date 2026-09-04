<script setup lang="ts">
import { SearchOutline, AddOutline, RefreshOutline, DownloadOutline } from '@vicons/ionicons5'
import {
  getRoleList, addRole, updateRole, deleteRole, changeRoleStatus, getRoleMenuTree, assignRoleMenu,
  getRoleUsers, assignRoleUsers, getUserList,
  getDeptTree, getRoleDeptIds, assignRoleDept, exportRole,
} from '@/api/modules/system'
import { STATUS, STATUS_OPTIONS, STATUS_LABEL, DATA_SCOPE, DATA_SCOPE_LABEL, DATA_SCOPE_OPTIONS } from '@/constants'

const message = useMessage()
const dialog = useDialog()
const { pagination, resetPage, setPage, setPageSize } = usePagination()
const { renderStatusTag } = useStatusRender()

const loading = ref(false)
const tableData = ref<any[]>([])
const selectedIds = ref<string[]>([])

const searchForm = reactive({ roleName: '', roleKey: '', status: null as number | null })

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive<{ id: string | null; roleName: string; roleKey: string; roleSort: number; dataScope: number; status: number; remark: string }>({ id: null, roleName: '', roleKey: '', roleSort: 0, dataScope: DATA_SCOPE.SELF, status: 0, remark: '' })

const deptTree = ref<any[]>([])
const checkedDeptKeys = ref<string[]>([])
const isCustomScope = computed(() => formData.dataScope === DATA_SCOPE.CUSTOM)

const menuDialogVisible = ref(false)
const menuRoleId = ref<string>()
const menuTree = ref<any[]>([])
const checkedMenuKeys = ref<string[]>([])

const userDialogVisible = ref(false)
const userRoleId = ref<string>()
const allUserOptions = ref<any[]>([])
const selectedUserIds = ref<string[]>([])
const userSearchLoading = ref(false)

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
  roleSort: [{ required: true, type: 'number' as const, message: '请输入排序', trigger: 'blur' }],
}

const columns = [
  { type: 'selection' as const },
  { title: '角色名称', key: 'roleName', width: 140 },
  { title: '角色标识', key: 'roleKey', width: 160 },
  { title: '排序', key: 'roleSort', width: 80, align: 'center' as const },
  {
    title: '数据范围', key: 'dataScope', width: 140, align: 'center' as const,
    render: (row: any) => DATA_SCOPE_LABEL[row.dataScope] ?? '-',
  },
  {
    title: '状态', key: 'status', width: 80, align: 'center' as const,
    render: (row: any) => h(NSwitch, {
      value: row.status === STATUS.NORMAL,
      checkedValue: true,
      uncheckedValue: false,
      onUpdateValue: (val: boolean) => handleStatusChange(row, val),
    }),
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 340, fixed: 'right' as const,
    render: (row: any) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => handleMenuAssign(row) }, { default: () => '分配菜单' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'success', onClick: () => handleUserAssign(row) }, { default: () => '分配用户' }),
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

async function handleAdd() {
  isEdit.value = false; dialogTitle.value = '新增角色'
  Object.assign(formData, { id: null, roleName: '', roleKey: '', roleSort: 0, dataScope: DATA_SCOPE.SELF, status: STATUS.NORMAL, remark: '' })
  checkedDeptKeys.value = []
  await ensureDeptTree()
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  isEdit.value = true; dialogTitle.value = '编辑角色'
  Object.assign(formData, { id: row.id, roleName: row.roleName, roleKey: row.roleKey, roleSort: row.roleSort, dataScope: row.dataScope ?? DATA_SCOPE.SELF, status: row.status, remark: row.remark ?? '' })
  await ensureDeptTree()
  try {
    const res = await getRoleDeptIds(row.id)
    checkedDeptKeys.value = (res.data ?? []).map((id: any) => String(id))
  } catch {
    checkedDeptKeys.value = []
  }
  dialogVisible.value = true
}

async function ensureDeptTree() {
  if (deptTree.value.length === 0) {
    try {
      const res = await getDeptTree()
      deptTree.value = res.data ?? []
    } catch {
      deptTree.value = []
    }
  }
}

async function handleSubmit() {
  try { await formRef.value?.validate() } catch { return }
  submitLoading.value = true
  try {
    const data: any = { roleName: formData.roleName, roleKey: formData.roleKey, roleSort: formData.roleSort, dataScope: formData.dataScope, status: formData.status, remark: formData.remark }
    if (formData.id) {
      data.id = formData.id
      await updateRole(data)
      if (isCustomScope.value) {
        await assignRoleDept(formData.id, checkedDeptKeys.value)
      } else {
        await assignRoleDept(formData.id, [])
      }
      message.success('修改成功')
    } else {
      const res = await addRole(data)
      const newId = res.data
      if (isCustomScope.value && newId) {
        await assignRoleDept(newId, checkedDeptKeys.value)
      }
      message.success('新增成功')
    }
    dialogVisible.value = false; fetchData()
  } catch (e: any) { message.error(e.message || '操作失败') }
  finally { submitLoading.value = false }
}

async function handleDelete(id: string) {
  try { await deleteRole(id); message.success('删除成功'); fetchData() }
  catch (e: any) { message.error(e.message || '删除失败') }
}

async function handleStatusChange(row: any, val: boolean) {
  const newStatus = val ? STATUS.NORMAL : STATUS.DISABLED
  try {
    await changeRoleStatus(row.id, newStatus)
    row.status = newStatus
    message.success('状态修改成功')
  } catch (e: any) {
    message.error(e.message || '修改失败')
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    message.warning('请选择要删除的角色')
    return
  }
  try {
    await deleteRole(selectedIds.value)
    message.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch (e: any) {
    message.error(e.message || '批量删除失败')
  }
}

async function handleExport() {
  try {
    const res: any = await exportRole()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '角色列表.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (e: any) {
    message.error(e.message || '导出失败')
  }
}

async function handleMenuAssign(row: any) {
  menuRoleId.value = row.id
  try {
    const res: any = await getRoleMenuTree(row.id)
    menuTree.value = res.data?.menus ?? []
    checkedMenuKeys.value = (res.data?.checkedKeys ?? []).map((id: any) => String(id))
  } catch { menuTree.value = []; checkedMenuKeys.value = [] }
  menuDialogVisible.value = true
}

function getAllMenuKeys(tree: any[]): string[] {
  const keys: string[] = []
  for (const node of tree) {
    keys.push(String(node.id))
    if (node.children && node.children.length > 0) {
      keys.push(...getAllMenuKeys(node.children))
    }
  }
  return keys
}

function handleSelectAllMenus() {
  checkedMenuKeys.value = getAllMenuKeys(menuTree.value)
}

function handleDeselectAllMenus() {
  checkedMenuKeys.value = []
}

async function handleMenuSubmit() {
  if (!menuRoleId.value) return
  try {
    await assignRoleMenu(menuRoleId.value, checkedMenuKeys.value)
    message.success('菜单分配成功'); menuDialogVisible.value = false
  } catch (e: any) { message.error(e.message || '分配失败') }
}

async function handleUserAssign(row: any) {
  userRoleId.value = row.id
  selectedUserIds.value = []
  allUserOptions.value = []
  userDialogVisible.value = true

  try {
    const [allRes, roleRes] = await Promise.all([
      getUserList({ pageNum: 1, pageSize: 200 }),
      getRoleUsers(row.id),
    ]) as any[]
    const allUsers = (allRes.data?.records ?? []) as any[]
    const assignedUsers = (roleRes.data ?? []) as any[]
    const assignedIds = new Set(assignedUsers.map((u: any) => String(u.id)))

    allUserOptions.value = allUsers.map((u: any) => ({
      label: `${u.nickname} (${u.username})`,
      value: String(u.id),
    }))
    for (const u of assignedUsers) {
      const uid = String(u.id)
      if (!allUserOptions.value.some((o: any) => o.value === uid)) {
        allUserOptions.value.push({ label: `${u.nickname} (${u.username})`, value: uid })
      }
    }
    selectedUserIds.value = Array.from(assignedIds)
  } catch {
    /* ignore */
  }
}

async function handleUserSearch(query: string) {
  if (!query.trim()) {
    return
  }
  userSearchLoading.value = true
  try {
    const res: any = await getUserList({ pageNum: 1, pageSize: 50, username: query })
    const records = (res.data?.records ?? []) as any[]
    const newOptions = records.map((u: any) => ({
      label: `${u.nickname} (${u.username})`,
      value: String(u.id),
    }))
    const existingIds = new Set(allUserOptions.value.map((o: any) => o.value))
    for (const opt of newOptions) {
      if (!existingIds.has(opt.value)) {
        allUserOptions.value.push(opt)
      }
    }
  } catch {
    /* ignore */
  } finally {
    userSearchLoading.value = false
  }
}

async function handleUserSubmit() {
  if (!userRoleId.value) return
  try {
    await assignRoleUsers(userRoleId.value, selectedUserIds.value)
    message.success('用户分配成功'); userDialogVisible.value = false
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
        <n-space>
          <n-button type="primary" @click="handleAdd" v-permission="'system:role:add'"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
          <n-popconfirm @positive-click="handleBatchDelete">
            <template #trigger>
              <n-button type="error" :disabled="selectedIds.length === 0" v-permission="'system:role:delete'">批量删除</n-button>
            </template>
            确认删除选中的 {{ selectedIds.length }} 条角色吗？
          </n-popconfirm>
          <n-button @click="handleExport" v-permission="'system:role:export'"><template #icon><n-icon :component="DownloadOutline" /></template>导出</n-button>
        </n-space>
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
        <n-form-item label="数据范围" path="dataScope">
          <n-select v-model:value="formData.dataScope" :options="DATA_SCOPE_OPTIONS as any" placeholder="请选择数据范围" />
        </n-form-item>
        <n-form-item v-if="isCustomScope" label="授权部门" path="deptIds">
          <n-tree
            v-model:checked-keys="checkedDeptKeys"
            :data="deptTree"
            key-field="id"
            label-field="deptName"
            children-field="children"
            checkable
            cascade
            :selectable="false"
            style="max-height: 280px; overflow: auto; width: 100%"
            placeholder="请选择该角色可访问的部门"
          />
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
      <div class="mb-8px flex gap-8px">
        <n-button size="tiny" @click="handleSelectAllMenus">全选</n-button>
        <n-button size="tiny" @click="handleDeselectAllMenus">取消全选</n-button>
      </div>
      <n-tree
        v-model:checked-keys="checkedMenuKeys"
        :data="menuTree"
        checkable
        cascade
        key-field="id"
        label-field="menuName"
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

    <n-modal v-model:show="userDialogVisible" title="分配用户" preset="card" style="width: 520px" :mask-closable="false">
      <n-select
        v-model:value="selectedUserIds"
        :options="allUserOptions"
        placeholder="请输入用户名搜索"
        multiple
        clearable
        filterable
        remote
        :loading="userSearchLoading"
        @search="handleUserSearch"
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="userDialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handleUserSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>