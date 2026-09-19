/**
 * 列表页组合式函数
 * 封装分页列表的加载、错误处理、AbortController 防重复请求、组件卸载自动取消
 */
import { useMessage } from 'naive-ui'
import { createAbortController, isAbortError } from '@/api/request'
import type { PageParams, PageResult } from '@/api/types'

export interface UseTableOptions<T, S extends Record<string, unknown>> {
  /** API 请求函数，接收 PageParams 返回 PageResult<T> */
  api: (params: PageParams, config?: { signal?: AbortSignal }) => Promise<PageResult<T>>
  /** 搜索表单 reactive 对象 */
  searchForm: S
  /** 搜索表单默认值（提供后 handleReset 无需传参） */
  searchDefaults?: S
  /** 搜索表单字段名到 API 参数名的映射（可选，默认同名） */
  searchFields?: Partial<Record<keyof S, string>>
  /** 初始分页大小，默认 10 */
  pageSize?: number
}

export function useTable<T, S extends Record<string, unknown>>(options: UseTableOptions<T, S>) {
  const { api, searchForm, searchDefaults, searchFields, pageSize = 10 } = options

  const message = useMessage()
  const loading = ref(false)
  const tableData = ref<T[]>([]) as Ref<T[]>
  const selectedIds = ref<string[]>([])

  const { pagination, resetPage, setPage, setPageSize: setPageSizeInner } = usePagination(pageSize)

  let abortController: AbortController | null = null

  function buildParams(): PageParams {
    const params: Record<string, unknown> = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
    }
    for (const [key, value] of Object.entries(searchForm)) {
      if (value !== null && value !== undefined && value !== '') {
        const paramName = searchFields?.[key as keyof S] ?? key
        params[paramName] = value
      }
    }
    return params as PageParams
  }

  async function fetchData() {
    abortController?.abort()
    abortController = createAbortController()
    const currentController = abortController
    loading.value = true
    try {
      const res = await api(buildParams(), { signal: currentController.signal })
      if (currentController === abortController) {
        tableData.value = res.data?.records ?? []
        pagination.itemCount = res.data?.total ?? 0
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

  function handleSearch() {
    resetPage()
    fetchData()
  }

  function handleReset(defaults?: S) {
    const resetValues = defaults ?? searchDefaults
    if (resetValues) {
      Object.assign(searchForm, resetValues)
    } else {
      for (const key of Object.keys(searchForm) as (keyof S)[]) {
        delete searchForm[key]
      }
    }
    resetPage()
    fetchData()
  }

  function handlePageChange(page: number) {
    setPage(page)
    fetchData()
  }

  function handlePageSizeChange(size: number) {
    setPageSizeInner(size)
    fetchData()
  }

  function handleSelectionChange(keys: (string | number)[]) {
    selectedIds.value = keys as string[]
  }

  onBeforeUnmount(() => {
    abortController?.abort()
  })

  return {
    loading,
    tableData,
    selectedIds,
    pagination,
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handlePageSizeChange,
    handleSelectionChange,
  }
}