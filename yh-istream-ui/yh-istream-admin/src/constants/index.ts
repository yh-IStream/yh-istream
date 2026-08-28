/**
 * 全局常量定义
 */
export const STATUS = {
  /** 正常/成功 */
  NORMAL: 0,
  /** 停用/失败 */
  DISABLED: 1,
} as const

export const STATUS_LABEL: Record<number, string> = {
  [STATUS.NORMAL]: '正常',
  [STATUS.DISABLED]: '停用',
}

export const SUCCESS_LABEL: Record<number, string> = {
  [STATUS.NORMAL]: '成功',
  [STATUS.DISABLED]: '失败',
}

export const STATUS_TAG_TYPE: Record<number, 'success' | 'error'> = {
  [STATUS.NORMAL]: 'success',
  [STATUS.DISABLED]: 'error',
}

export const STATUS_OPTIONS = [
  { label: '全部', value: null, type: 'option' as const },
  { label: '正常', value: STATUS.NORMAL, type: 'option' as const },
  { label: '停用', value: STATUS.DISABLED, type: 'option' as const },
] as any

export const SUCCESS_OPTIONS = [
  { label: '全部', value: null, type: 'option' as const },
  { label: '成功', value: STATUS.NORMAL, type: 'option' as const },
  { label: '失败', value: STATUS.DISABLED, type: 'option' as const },
] as any

export const PAGINATION_DEFAULTS = {
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100] as number[],
  prefix: (info: { startIndex: number; endIndex: number; itemCount: number | undefined }) =>
    `共 ${info.itemCount ?? 0} 条`,
}

export const PAGE_SIZES = [10, 20, 50] as number[]