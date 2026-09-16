<script setup lang="ts">
import { AddOutline, RefreshOutline } from '@vicons/ionicons5'
import { getMenuTree, addMenu, updateMenu, deleteMenu, type SysMenu, type SysMenuCreate, type SysMenuUpdate } from '@/api/modules/system'
import type { TreeSelectOption } from 'naive-ui'
import { STATUS, STATUS_LABEL } from '@/constants'
import { useDict } from '@/composables/useDict'
import { useTreeData } from '@/composables/useTreeData'
import { useCrudDialog } from '@/composables/useCrudDialog'

const message = useMessage()

const { renderStatusTag } = useStatusRender()
const { useDictTag, loadDict } = useDict()
const { render: renderMenuTypeTag } = useDictTag('sys_menu_type', '未知')
const { loading, treeData, fetchData } = useTreeData<SysMenu>({ api: getMenuTree })

interface MenuFormData {
  id: string | null
  parentId: string
  menuName: string
  menuType: string
  path: string
  component: string
  query: string
  permission: string
  icon: string
  orderNum: number
  status: number
  visible: number
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
} = useCrudDialog<MenuFormData>({
  defaults: () => ({ id: null, parentId: '0', menuName: '', menuType: 'M', path: '', component: '', query: '', permission: '', icon: '', orderNum: 0, status: STATUS.NORMAL, visible: 1 }),
  addApi: (data) => addMenu(data as SysMenuCreate),
  updateApi: (data) => updateMenu(data as SysMenuUpdate),
  onSuccess: fetchData,
  titles: { add: '新增菜单', edit: '编辑菜单' },
})

const menuTreeSelectData = computed(() => {
  const filterTree = (list: SysMenu[]): TreeSelectOption[] =>
    list
      .filter(item => String(item.id) !== String(formData.id))
      .map(item => ({
        key: String(item.id),
        label: item.menuName,
        children: item.children && item.children.length ? filterTree(item.children) : undefined,
      }))
  return [{ key: '0', label: '主类目', children: filterTree(treeData.value) }]
})

const menuTypeOptions = ref<{ label: string; value: string }[]>([])

onMounted(async () => {
  const list = await loadDict('sys_menu_type')
  menuTypeOptions.value = list.map(item => ({ label: item.label, value: item.value }))
  fetchData()
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  orderNum: [{ required: true, type: 'number' as const, message: '请输入排序', trigger: 'blur' }],
}

const columns = [
  { title: '菜单名称', key: 'menuName', tree: true, minWidth: 160 },
  {
    title: '类型', key: 'menuType', width: 70, align: 'center' as const,
    render: (row: SysMenu) => renderMenuTypeTag(row.menuType),
  },
  { title: '路由路径', key: 'path', minWidth: 120, ellipsis: { tooltip: true } },
  { title: '权限标识', key: 'permission', minWidth: 120, ellipsis: { tooltip: true } },
  { title: '图标', key: 'icon', width: 70, align: 'center' as const },
  { title: '排序', key: 'orderNum', width: 60, align: 'center' as const },
  {
    title: '状态', key: 'status', width: 70, align: 'center' as const,
    render: (row: SysMenu) => renderStatusTag(row.status),
  },
  {
    title: '操作', key: 'actions', width: 180, fixed: 'right' as const,
    render: (row: SysMenu) => h('div', { class: 'flex gap-4px' }, [
      h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => handleAdd(row) }, { default: () => '新增' }),
      h(NButton, { size: 'tiny', quaternary: true, type: 'info', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', quaternary: true, type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除？子菜单将一并删除，不可恢复！',
      }),
    ]),
  },
]

function handleAdd(parent?: SysMenu) {
  openAddInner()
  formData.parentId = parent ? String(parent.id) : '0'
  formData.menuType = parent ? 'C' : 'M'
}

function handleEdit(row: SysMenu) {
  openEditInner({
    id: row.id,
    parentId: row.parentId != null ? String(row.parentId) : '0',
    menuName: row.menuName,
    menuType: row.menuType,
    path: row.path ?? '',
    component: row.component ?? '',
    query: row.query ?? '',
    permission: row.permission ?? '',
    icon: row.icon ?? '',
    orderNum: row.orderNum ?? 0,
    status: row.status ?? 0,
    visible: row.visible ?? 1,
  })
}

async function handleDelete(id: string) {
  try { await deleteMenu(id); message.success('删除成功'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message || '删除失败') }
}
</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <div class="flex items-center justify-between mb-12px">
        <n-button type="primary" @click="handleAdd()" v-permission="'system:menu:add'"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
        <n-button @click="fetchData"><template #icon><n-icon :component="RefreshOutline" /></template>刷新</n-button>
      </div>
      <n-data-table :columns="columns" :data="treeData" :loading="loading" :row-key="(row: SysMenu) => row.id" striped size="small" default-expand-all />
    </div>

    <n-modal v-model:show="dialogVisible" :title="dialogTitle" preset="card" style="width: 600px" :mask-closable="false">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80px">
        <n-form-item label="上级菜单" path="parentId">
          <n-tree-select v-model:value="formData.parentId" :options="menuTreeSelectData" :default-expand-all="false" placeholder="请选择上级菜单" />
        </n-form-item>
        <n-form-item label="菜单类型" path="menuType">
          <n-radio-group v-model:value="formData.menuType">
            <n-radio v-for="opt in menuTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="菜单名称" path="menuName">
          <n-input v-model:value="formData.menuName" placeholder="请输入菜单名称" />
        </n-form-item>
        <n-form-item v-if="formData.menuType !== 'F'" label="路由路径" path="path">
          <n-input v-model:value="formData.path" placeholder="请输入路由路径" />
        </n-form-item>
        <n-form-item v-if="formData.menuType === 'C'" label="组件路径" path="component">
          <n-input v-model:value="formData.component" placeholder="请输入组件路径" />
        </n-form-item>
        <n-form-item v-if="formData.menuType === 'C'" label="路由参数" path="query">
          <n-input v-model:value="formData.query" placeholder="请输入路由参数，如 id=1&type=add" />
        </n-form-item>
        <n-form-item label="权限标识" path="permission">
          <n-input v-model:value="formData.permission" placeholder="请输入权限标识" />
        </n-form-item>
        <n-form-item label="图标" path="icon">
          <n-input v-model:value="formData.icon" placeholder="请输入图标名称" />
        </n-form-item>
        <n-form-item label="排序" path="orderNum">
          <n-input-number v-model:value="formData.orderNum" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="STATUS.NORMAL" :unchecked-value="STATUS.DISABLED">
            <template #checked>{{ STATUS_LABEL[STATUS.NORMAL] }}</template><template #unchecked>{{ STATUS_LABEL[STATUS.DISABLED] }}</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="可见">
          <n-switch v-model:value="formData.visible" :checked-value="1" :unchecked-value="0">
            <template #checked>显示</template><template #unchecked>隐藏</template>
          </n-switch>
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