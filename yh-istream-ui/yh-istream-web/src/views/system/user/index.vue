<script setup lang="ts">
import { SearchOutline, AddOutline, TrashOutline, DownloadOutline, RefreshOutline } from '@vicons/ionicons5'
import {
  getUserList, getUserById, addUser, updateUser, assignUserRoles, deleteUser,
  resetUserPwd, changeUserStatus, exportUser, getDeptTree, getAllRoles, type SysUser,
  type SysUserSave, type SysRole, type SysDept,
} from '@/api/modules/system'
import { STATUS, STATUS_OPTIONS, STATUS_LABEL } from '@/constants'
import { useDict } from '@/composables/useDict'
import { useTable } from '@/composables/useTable'
import { useExport } from '@/composables/useExport'

const message = useMessage()
const dialog = useDialog()
const { renderStatusTag } = useStatusRender()
const statusLoadingMap = ref(new Map<string, boolean>())
const { loadDict, useDictTag } = useDict()
const { render: renderGenderTag } = useDictTag('sys_user_sex')
const { downloadExcel } = useExport()

const searchForm = reactive({
  username: '',
  phone: '',
  status: null as number | null,
  deptId: null as string | null,
  beginTime: null as string | null,
  endTime: null as string | null,
})
const searchDefaults = {
  username: '',
  phone: '',
  status: null as number | null,
  deptId: null as string | null,
  beginTime: null as string | null,
  endTime: null as string | null,
}
const { loading, tableData, selectedIds, pagination, fetchData, handleSearch, handleReset, handlePageChange, handlePageSizeChange, handleSelectionChange } = useTable<SysUser, typeof searchForm>({
  api: getUserList,
  searchForm,
  searchDefaults,
})

const genderOptions = ref<{ label: string; value: string }[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({
  id: null as string | null,
  username: '',
  nickname: '',
  password: '',
  deptId: null as string | null,
  email: '',
  phone: '',
  gender: 0,
  status: STATUS.NORMAL,
  remark: '',
})

const deptOptions = ref<SysDept[]>([])
const roleOptions = ref<{ label: string; value: string }[]>([])

// 重置密码对话框
const pwdDialogVisible = ref(false)
const pwdUserId = ref<string>()
const pwdFormRef = ref()
const pwdForm = reactive({ password: '', confirmPassword: '' })

const roleDialogVisible = ref(false)
const roleUserId = ref<string>()
const roleFormRef = ref()
const roleForm = reactive({ roleIds: [] as string[] })
const roleInitLoading = ref(false)

// 表格列
const columns = [
  { type: 'selection' as const },
  { title: '用户名', key: 'username', minWidth: 100, ellipsis: { tooltip: true } },
  { title: '昵称', key: 'nickname', minWidth: 100, ellipsis: { tooltip: true } },
  { title: '部门', key: 'dept', minWidth: 100, ellipsis: { tooltip: true }, render: (row: SysUser) => row.deptName ?? '-' },
  { title: '手机号', key: 'phone', minWidth: 120, ellipsis: { tooltip: true } },
  { title: '邮箱', key: 'email', minWidth: 140, ellipsis: { tooltip: true } },
  {
    title: '性别', key: 'gender', width: 70, align: 'center' as const,
    render: (row: SysUser) => renderGenderTag(row.gender),
  },
  {
    title: '状态', key: 'status', width: 70, align: 'center' as const,
    render: (row: SysUser) => h(NSwitch, {
      value: row.status === STATUS.NORMAL,
      checkedValue: true,
      uncheckedValue: false,
      loading: statusLoadingMap.value.get(row.id) ?? false,
      onUpdateValue: (val: boolean) => handleStatusChange(row, val),
    }),
  },
  { title: '创建时间', key: 'createTime', minWidth: 150, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 320, fixed: 'right' as const,
    render: (row: SysUser) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => handleRoleAssign(row) }, { default: () => '分配角色' }),
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
const rules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: isEdit.value
    ? []
    : [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change', type: 'string' as const }],
  email: [{ type: 'email' as const, message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
}))

const pwdRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_rule: unknown, value: string) => value === pwdForm.password, message: '两次密码不一致', trigger: 'blur' },
  ],
}

// ==================== Methods ====================
async function fetchDeptTree() {
  try { deptOptions.value = (await getDeptTree()).data ?? [] } catch (e) { console.warn('部门树加载失败', e) }
}

async function fetchRoles() {
  try { roleOptions.value = ((await getAllRoles()).data ?? []).map((r: SysRole) => ({ label: r.roleName, value: String(r.id) })) } catch (e) { console.warn('角色列表加载失败', e) }
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(formData, { id: null, username: '', nickname: '', password: '', deptId: null, email: '', phone: '', gender: 0, status: STATUS.NORMAL, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row: SysUser) {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(formData, {
    id: row.id, username: row.username, nickname: row.nickname, password: '',
    deptId: row.deptId, email: row.email ?? '', phone: row.phone ?? '',
    gender: row.gender ?? 0, status: row.status, remark: row.remark ?? '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value?.validate() } catch { return }
  submitLoading.value = true
  try {
    if (formData.id) {
      await updateUser({
        id: formData.id, username: formData.username, nickname: formData.nickname,
        deptId: formData.deptId ?? undefined, email: formData.email || undefined,
        phone: formData.phone || undefined, gender: formData.gender,
        status: formData.status, remark: formData.remark || undefined,
      })
      message.success('修改成功')
    } else {
      await addUser({
        username: formData.username, nickname: formData.nickname, password: formData.password,
        deptId: formData.deptId ?? undefined, email: formData.email || undefined,
        phone: formData.phone || undefined, gender: formData.gender,
        status: formData.status, remark: formData.remark || undefined,
      })
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e: unknown) {
    message.error((e as Error).message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleRoleAssign(row: SysUser) {
  roleUserId.value = row.id
  roleForm.roleIds = []
  roleDialogVisible.value = true
  roleInitLoading.value = true
  try {
    const res = await getUserById(row.id)
    if (res.data?.roleIds) {
      roleForm.roleIds = res.data.roleIds.map((id: string) => String(id))
    }
  } catch (e) { console.warn('用户角色加载失败', e) }
  finally { roleInitLoading.value = false }
}

async function handleRoleSubmit() {
  if (roleUserId.value == null) {
    return
  }
  try {
    await assignUserRoles(roleUserId.value, roleForm.roleIds)
    message.success('角色分配成功')
    roleDialogVisible.value = false
    fetchData()
  } catch (e: unknown) {
    message.error((e as Error).message || '角色分配失败')
  }
}

async function handleDelete(id: string) {
  try {
    await deleteUser(id)
    message.success('删除成功')
    fetchData()
  } catch (e: unknown) {
    message.error((e as Error).message || '删除失败')
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
        await deleteUser(selectedIds.value)
        message.success('批量删除成功')
        selectedIds.value = []
        fetchData()
      } catch (e: unknown) {
        message.error((e as Error).message || '删除失败')
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
  } catch (e: unknown) {
    message.error((e as Error).message || '重置失败')
  }
}

async function handleStatusChange(row: SysUser, value: boolean) {
  if (statusLoadingMap.value.get(row.id)) return
  const newStatus = value ? STATUS.NORMAL : STATUS.DISABLED
  statusLoadingMap.value.set(row.id, true)
  try {
    await changeUserStatus(row.id, newStatus)
    row.status = newStatus
    message.success('状态修改成功')
  } catch (e: unknown) {
    message.error((e as Error).message || '修改失败')
  } finally {
    statusLoadingMap.value.delete(row.id)
  }
}

async function handleExport() {
  const params = { username: searchForm.username, phone: searchForm.phone, status: searchForm.status }
  await downloadExcel(() => exportUser(params), '用户列表.xlsx')
}

onMounted(async () => {
  genderOptions.value = await loadDict('sys_user_sex')
  fetchData()
  fetchDeptTree()
  fetchRoles()
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
            <n-button @click="handleReset()">
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
          <n-button type="error" ghost @click="handleBatchDelete" :disabled="selectedIds.length === 0" v-permission="'system:user:delete'">
            <template #icon><n-icon :component="TrashOutline" /></template>
            批量删除
          </n-button>
        </n-space>
        <n-button @click="handleExport" v-permission="'system:user:export'">
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
        <n-form-item label="性别" path="gender">
          <n-radio-group v-model:value="formData.gender">
            <n-radio v-for="item in genderOptions" :key="item.value" :value="Number(item.value)">{{ item.label }}</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" :rows="2" />
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

    <!-- 分配角色对话框 -->
    <n-modal v-model:show="roleDialogVisible" title="分配角色" preset="card" style="width: 480px" :mask-closable="false">
      <n-spin :show="roleInitLoading">
        <n-form ref="roleFormRef" :model="roleForm" label-placement="left" label-width="80px">
          <n-form-item label="角色">
            <n-select
              v-model:value="roleForm.roleIds"
              :options="roleOptions"
              placeholder="请选择角色"
              multiple
              clearable
            />
          </n-form-item>
        </n-form>
      </n-spin>
      <template #footer>
        <n-space justify="end">
          <n-button @click="roleDialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handleRoleSubmit">确定</n-button>
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