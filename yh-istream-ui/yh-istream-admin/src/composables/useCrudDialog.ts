/**
 * CRUD 对话框组合式函数
 * 封装新增/编辑对话框的通用状态与逻辑
 */
import { useMessage } from 'naive-ui'

export interface UseCrudDialogOptions<T> {
  /** 默认表单数据工厂函数 */
  defaults: () => T
  /** 新增 API 调用 */
  addApi: (data: T) => Promise<unknown>
  /** 编辑 API 调用 */
  updateApi: (data: T & { id: string }) => Promise<unknown>
  /** 操作成功后的回调（如刷新列表） */
  onSuccess: () => void
  /** 对话框标题配置 */
  titles?: { add?: string; edit?: string }
}

export function useCrudDialog<T extends Record<string, unknown>>(options: UseCrudDialogOptions<T>) {
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

  function openEdit(row: T & { id: string }) {
    isEdit.value = true
    title.value = titles?.edit ?? '编辑'
    Object.assign(formData, { ...defaults(), ...row })
    visible.value = true
  }

  async function handleSubmit(validate?: () => Promise<void>) {
    if (validate) {
      try { await validate() } catch { return }
    }
    submitLoading.value = true
    try {
      if (isEdit.value && 'id' in formData && formData.id) {
        await updateApi(formData as T & { id: string })
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