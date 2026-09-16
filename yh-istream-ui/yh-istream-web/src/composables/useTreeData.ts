/**
 * 树形数据组合式函数
 * 封装树形页面的加载、AbortController 防重复请求、组件卸载自动取消
 * 适用于菜单管理、部门管理等非分页树形数据页面
 */
import { useMessage } from 'naive-ui'
import { createAbortController, isAbortError } from '@/api/request'

export interface UseTreeDataOptions<T> {
  /** API 请求函数 */
  api: (config?: { signal?: AbortSignal }) => Promise<{ data: T[] }>
}

export function useTreeData<T>(options: UseTreeDataOptions<T>) {
  const { api } = options

  const message = useMessage()
  const loading = ref(false)
  const treeData = ref<T[]>([]) as Ref<T[]>

  let abortController: AbortController | null = null

  async function fetchData() {
    abortController?.abort()
    abortController = createAbortController()
    const currentController = abortController
    loading.value = true
    try {
      const res = await api({ signal: currentController.signal })
      if (currentController === abortController) {
        treeData.value = res.data ?? []
      }
    } catch (e: unknown) {
      if (!isAbortError(e)) {
        message.error((e as Error).message || '查询失败')
      }
    } finally {
      if (currentController === abortController) {
        loading.value = false
      }
    }
  }

  onBeforeUnmount(() => {
    abortController?.abort()
  })

  return {
    loading,
    treeData,
    fetchData,
  }
}