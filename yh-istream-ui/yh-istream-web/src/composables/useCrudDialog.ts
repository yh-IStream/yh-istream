/**
 * CRUD 对话框组合式函数
 * 封装新增/编辑对话框的通用状态与逻辑
 *
 * 适用场景：纯CRUD页面（config、dict、dept、menu等）
 * 不适用场景：有额外异步逻辑的页面（如user需加载部门树+角色列表，role需加载部门树+分配部门）
 * 这些复杂页面保持手写逻辑更直观、可调试
 *
 * @since 2026-09-08
 */
import { useMessage } from 'naive-ui'

export interface UseCrudDialogOptions<T> {
  defaults: () => T
  addApi: (data: unknown) => Promise<unknown>
  updateApi: (data: unknown) => Promise<unknown>
  onSuccess: () => void
  titles?: { add?: string; edit?: string }
}

export function useCrudDialog<T extends object>(options: UseCrudDialogOptions<T>) {
  const { defaults, addApi, updateApi, onSuccess, titles } = options

  const message = useMessage()
  const visible = ref(false)
  const title = ref(titles?.add ?? '新增')
  const isEdit = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()
  const formData = reactive(defaults()) as T

  function openAdd() {
    isEdit.value = false
    title.value = titles?.add ?? '新增'
    Object.assign(formData, defaults())
    visible.value = true
  }

  function openEdit(row: unknown) {
    isEdit.value = true
    title.value = titles?.edit ?? '编辑'
    // 浅合并：defaults() 提供缺失字段的默认值，row 覆盖已有字段
    // 注意：若 FormData 含嵌套对象，浅拷贝会导致残留旧数据，需改用深拷贝
    Object.assign(formData, { ...defaults(), ...row as Record<string, unknown> })
    visible.value = true
  }

  async function handleSubmit(validate?: () => Promise<void>) {
    if (validate) {
      try { await validate() } catch { return }
    }
    submitLoading.value = true
    try {
      if (isEdit.value && 'id' in formData && formData.id) {
        await updateApi(formData)
        message.success('修改成功')
      } else {
        await addApi(formData)
        message.success('新增成功')
      }
      visible.value = false
      onSuccess()
    } catch (e: unknown) {
      message.error((e as Error).message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  }

  return {
    visible,
    title,
    isEdit,
    submitLoading,
    formRef,
    formData,
    openAdd,
    openEdit,
    handleSubmit,
  }
}