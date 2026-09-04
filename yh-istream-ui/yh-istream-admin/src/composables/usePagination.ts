/**
 * 分页组合式函数
 * 封装列表页通用的分页、搜索、重置逻辑
 */
import { PAGINATION_DEFAULTS } from '@/constants'

export function usePagination(pageSize = 10) {
  const pagination = reactive({
    page: PAGINATION_DEFAULTS.page,
    pageSize,
    itemCount: PAGINATION_DEFAULTS.itemCount,
    showSizePicker: PAGINATION_DEFAULTS.showSizePicker,
    pageSizes: [...PAGINATION_DEFAULTS.pageSizes],
    prefix: PAGINATION_DEFAULTS.prefix,
  })

  function resetPage() {
    pagination.page = 1
  }

  function setPage(page: number) {
    pagination.page = page
  }

  function setPageSize(size: number) {
    pagination.pageSize = size
    pagination.page = 1
  }

  return {
    pagination,
    resetPage,
    setPage,
    setPageSize,
  }
}