<script setup lang="ts">
import { AddOutline, RefreshOutline } from '@vicons/ionicons5'
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api/modules/system'
import { STATUS, STATUS_LABEL } from '@/constants'
import { useDict } from '@/composables/useDict'

const message = useMessage()
const { renderStatusTag } = useStatusRender()
const { useDictTag, loadDict } = useDict()
const { render: renderMenuTypeTag } = useDictTag('sys_menu_type', '未知')
const loading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({ id: null as string | null, parentId: '0' as string, menuName: '', menuType: 'M', path: '', component: '', permission: '', icon: '', orderNum: 0, status: 0, visible: 1 })

const menuTreeSelectData = computed(() => {
  const filterTree = (list: any[]): any[] =>
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
  { title: '菜单名称', key: 'menuName', tree: true, width: 200 },
  {
    title: '类型', key: 'menuType', width: 80, align: 'center' as const,
    render: (row: any) => renderMenuTypeTag(row.menuType),
  },
  { title: '路由路径', key: 'path', width: 160, ellipsis: { tooltip: true } },
  { title: '权限标识', key: 'permission', width: 180, ellipsis: { tooltip: true } },
  { title: '图标', key: 'icon', width: 80 },
  { title: '排序', key: 'orderNum', width: 60, align: 'center' as const },
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
        default: () => '确认删除？子菜单将一并删除，不可恢复！',
      }),
    ]),
  },
]

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getMenuTree()
    treeData.value = res.data ?? []
  } catch (e: any) { message.error(e.message || '查询失败') }
  finally { loading.value = false }
}

function handleAdd(parent?: any) {
  isEdit.value = false; dialogTitle.value = '新增菜单'
  const parentId = parent ? String(parent.id) : '0'
  Object.assign(formData, { id: null, parentId, menuName: '', menuType: parent ? 'C' : 'M', path: '', component: '', permission: '', icon: '', orderNum: 0, status: STATUS.NORMAL, visible: 1 })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true; dialogTitle.value = '编辑菜单'
  Object.assign(formData, { id: row.id, parentId: row.parentId != null ? String(row.parentId) : '0', menuName: row.menuName, menuType: row.menuType, path: row.path ?? '', component: row.component ?? '', permission: row.permission ?? '', icon: row.icon ?? '', orderNum: row.orderNum ?? 0, status: row.status ?? 0, visible: row.visible ?? 1 })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value?.validate() } catch { return }
  submitLoading.value = true
  try {
    const data: any = { ...formData, parentId: Number(formData.parentId) || 0 }
    if (formData.id) { data.id = formData.id; await updateMenu(data); message.success('修改成功') }
    else { await addMenu(data); message.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e: any) { message.error(e.message || '操作失败') }
  finally { submitLoading.value = false }
}

async function handleDelete(id: string) {
  try { await deleteMenu(id); message.success('删除成功'); fetchData() }
  catch (e: any) { message.error(e.message || '删除失败') }
}


</script>

<template>
  <div class="flex flex-col gap-12px">
    <div class="card">
      <div class="flex items-center justify-between mb-12px">
        <n-button type="primary" @click="handleAdd()" v-permission="'system:menu:add'"><template #icon><n-icon :component="AddOutline" /></template>新增</n-button>
        <n-button @click="fetchData"><template #icon><n-icon :component="RefreshOutline" /></template>刷新</n-button>
      </div>
      <n-data-table :columns="columns" :data="treeData" :loading="loading" :row-key="(row: any) => row.id" striped size="small" default-expand-all />
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
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>