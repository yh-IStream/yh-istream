<script setup lang="ts">
import { SearchOutline, AddOutline, TrashOutline, DownloadOutline, RefreshOutline } from '@vicons/ionicons5'
import {
  getUserList, addUser, updateUser, deleteUser, batchDeleteUser,
  resetUserPwd, changeUserStatus, exportUser, type SysUser,
} from '@/api/modules/system'
import { STATUS, STATUS_OPTIONS, STATUS_LABEL } from '@/constants'

const message = useMessage()
const dialog = useDialog()
const { pagination, resetPage, setPage, setPageSize } = usePagination()
const { renderStatusTag } = useStatusRender()

// ==================== State ====================
const loading = ref(false)
const tableData = ref<SysUser[]>([])
const selectedIds = ref<number[]>([])

const searchForm = reactive({
  username: '',
  phone: '',
  status: null as number | null,
  deptId: null as number | null,
  beginTime: null as string | null,
  endTime: null as string | null,
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({
  id: null as number | null,
  username: '',
  nickname: '',
  password: '',
  deptId: null as number | null,
  email: '',
  phone: '',
  gender: 0,
  status: STATUS.NORMAL,
  roleIds: [] as number[],
})

const deptOptions = ref<any[]>([])

// 重置密码对话框
const pwdDialogVisible = ref(false)
const pwdUserId = ref<number>()
const pwdFormRef = ref()
const pwdForm = reactive({ password: '', confirmPassword: '' })

// 表格列
const columns = [
  { type: 'selection' as const },
  { title: '用户名', key: 'username', width: 120, ellipsis: { tooltip: true } },
  { title: '昵称', key: 'nickname', width: 120, ellipsis: { tooltip: true } },
  { title: '部门', key: 'dept', width: 140, render: (row: any) => row.dept?.deptName ?? '-' },
  { title: '手机号', key: 'phone', width: 130 },
  { title: '邮箱', key: 'email', width: 180, ellipsis: { tooltip: true } },
  {
    title: '状态', key: 'status', width: 80, align: 'center',
    render: (row: SysUser) => renderStatusTag(row.status),
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 260, fixed: 'right' as const,
    render: (row: SysUser) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'warning', onClick: () => handleResetPwd(row) }, { default: () => '重置密码' }),
      h(
        NPopconfirm,
        { onPositiveClick: () => handleDelete(row.id) },
        {
          trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除该用户吗？',
        },
      ),
    ]),
  },
]

// 表单验证规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: isEdit.value
    ? []
    : [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change', type: 'number' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
}

const pwdRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_: any, value: string) => value === pwdForm.password, message: '两次密码不一致', trigger: 'blur' },
  ],
}

// ==================== Methods ====================
async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.phone) params.phone = searchForm.phone
    if (searchForm.status !== null) params.status = searchForm.status
    if (searchForm.deptId) params.deptId = searchForm.deptId
    if (searchForm.beginTime) params.beginTime = searchForm.beginTime
    if (searchForm.endTime) params.endTime = searchForm.endTime

    const res: any = await getUserList(params as any)
    tableData.value = res.data?.records ?? []
    pagination.itemCount = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '查询失败')
  } finally {
    loading.value = false
  }
}

async function fetchDeptTree() {
  try {
    const res: any = await getDeptTree()
    deptOptions.value = res.data ?? []
  } catch { /* ignore */ }
}

function handleSearch() {
  resetPage()
  fetchData()
}

function handleReset() {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = null
  searchForm.deptId = null
  searchForm.beginTime = null
  searchForm.endTime = null
  resetPage()
  fetchData()
}

function handlePageChange(page: number) {
  setPage(page)
  fetchData()
}

function handlePageSizeChange(pageSize: number) {
  setPageSize(pageSize)
  fetchData()
}

function handleSelectionChange(keys: any[]) {
  selectedIds.value = keys as number[]
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: SysUser) {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    password: '',
    deptId: row.deptId,
    email: row.email ?? '',
    phone: row.phone ?? '',
    gender: 0,
    status: row.status,
    roleIds: [],
  })
  dialogVisible.value = true
}

function resetForm() {
  formData.id = null
  formData.username = ''
  formData.nickname = ''
  formData.password = ''
  formData.deptId = null
  formData.email = ''
  formData.phone = ''
  formData.gender = 0
  formData.status = STATUS.NORMAL
  formData.roleIds = []
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const data: Record<string, unknown> = {
      username: formData.username,
      nickname: formData.nickname,
      deptId: formData.deptId,
      email: formData.email || undefined,
      phone: formData.phone || undefined,
      gender: formData.gender,
      status: formData.status,
    }
    if (formData.id) {
      data.id = formData.id
      await updateUser(data)
      message.success('修改成功')
    } else {
      data.password = formData.password
      await addUser(data)
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    message.success('删除成功')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    message.warning('请选择要删除的用户')
    return
  }
  dialog.warning({
    title: '确认删除',
    content: `确认删除选中的 ${selectedIds.value.length} 个用户吗？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await batchDeleteUser(selectedIds.value)
        message.success('批量删除成功')
        selectedIds.value = []
        fetchData()
      } catch (e: any) {
        message.error(e.message || '删除失败')
      }
    },
  })
}

function handleResetPwd(row: SysUser) {
  pwdUserId.value = row.id
  pwdForm.password = ''
  pwdForm.confirmPassword = ''
  pwdDialogVisible.value = true
}

async function handlePwdSubmit() {
  try {
    await pwdFormRef.value?.validate()
  } catch {
    return
  }
  try {
    await resetUserPwd(pwdUserId.value!, pwdForm.password)
    message.success('密码重置成功')
    pwdDialogVisible.value = false
  } catch (e: any) {
    message.error(e.message || '重置失败')
  }
}

async function handleStatusChange(row: SysUser, value: boolean) {
  const newStatus = value ? STATUS.NORMAL : STATUS.DISABLED
  try {
    await changeUserStatus(row.id, newStatus)
    row.status = newStatus
    message.success('状态修改成功')
  } catch (e: any) {
    message.error(e.message || '修改失败')
  }
}

async function handleExport() {
  try {
    const res = await exportUser({})
    const blob = new Blob([res as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '用户列表.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (e: any) {
    message.error(e.message || '导出失败')
  }
}

onMounted(() => {
  fetchData()
  fetchDeptTree()
})
</script>

<template>
  <div class="flex flex-col gap-12px">
    <!-- 搜索区域 -->
    <div class="card">
      <n-form inline label-placement="left" :show-feedback="false">
        <n-form-item label="用户名">
          <n-input v-model:value="searchForm.username" placeholder="请输入用户名" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="手机号">
          <n-input v-model:value="searchForm.phone" placeholder="请输入手机号" clearable style="width: 160px" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="searchForm.status" :options="STATUS_OPTIONS" placeholder="请选择" clearable style="width: 120px" />
        </n-form-item>
        <n-form-item label="部门">
          <n-tree-select
            v-model:value="searchForm.deptId"
            :options="deptOptions"
            placeholder="请选择部门"
            clearable
            key-field="id"
            label-field="deptName"
            children-field="children"
            style="width: 180px"
          />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              搜索
            </n-button>
            <n-button @click="handleReset">
              <template #icon><n-icon :component="RefreshOutline" /></template>
              重置
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>

    <!-- 操作栏 + 表格 -->
    <div class="card">
      <div class="flex items-center justify-between mb-12px">
        <n-space>
          <n-button type="primary" @click="handleAdd" v-permission="'system:user:add'">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增
          </n-button>
          <n-button type="error" ghost @click="handleBatchDelete" :disabled="selectedIds.length === 0">
            <template #icon><n-icon :component="TrashOutline" /></template>
            批量删除
          </n-button>
        </n-space>
        <n-button @click="handleExport">
          <template #icon><n-icon :component="DownloadOutline" /></template>
          导出
        </n-button>
      </div>

      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: SysUser) => row.id"
        :checked-row-keys="selectedIds"
        striped
        size="small"
        remote
        @update:checked-row-keys="handleSelectionChange"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <n-modal v-model:show="dialogVisible" :title="dialogTitle" preset="card" style="width: 600px" :mask-closable="false">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80px">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="formData.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item v-if="!isEdit" label="密码" path="password">
          <n-input v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="部门" path="deptId">
          <n-tree-select
            v-model:value="formData.deptId"
            :options="deptOptions"
            placeholder="请选择部门"
            key-field="id"
            label-field="deptName"
            children-field="children"
          />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="STATUS.NORMAL" :unchecked-value="STATUS.DISABLED">
            <template #checked>{{ STATUS_LABEL[STATUS.NORMAL] }}</template>
            <template #unchecked>{{ STATUS_LABEL[STATUS.DISABLED] }}</template>
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

    <!-- 重置密码对话框 -->
    <n-modal v-model:show="pwdDialogVisible" title="重置密码" preset="card" style="width: 420px" :mask-closable="false">
      <n-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-placement="left" label-width="80px">
        <n-form-item label="新密码" path="password">
          <n-input v-model:value="pwdForm.password" type="password" show-password-on="click" placeholder="请输入新密码" />
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="pwdForm.confirmPassword" type="password" show-password-on="click" placeholder="请再次输入密码" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="pwdDialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handlePwdSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>