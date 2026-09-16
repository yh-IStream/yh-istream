import type { SelectOption } from 'naive-ui'

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

export const STATUS_OPTIONS: SelectOption[] = [
  { label: '全部', value: undefined, type: 'option' },
  { label: '正常', value: STATUS.NORMAL, type: 'option' },
  { label: '停用', value: STATUS.DISABLED, type: 'option' },
]

export const SUCCESS_OPTIONS: SelectOption[] = [
  { label: '全部', value: undefined, type: 'option' },
  { label: '成功', value: STATUS.NORMAL, type: 'option' },
  { label: '失败', value: STATUS.DISABLED, type: 'option' },
]

export const PAGINATION_DEFAULTS = {
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100] as number[],
  prefix: (info: { startIndex: number; endIndex: number; itemCount: number | undefined }) =>
    `共 ${info.itemCount ?? 0} 条`,
}

/**
 * 数据权限范围（与后端 DataScopeEnum 保持一致）
 */
export const DATA_SCOPE = {
  /** 全部数据 */
  ALL: 1,
  /** 自定义数据 */
  CUSTOM: 2,
  /** 本部门数据 */
  DEPT: 3,
  /** 本部门及以下数据 */
  DEPT_AND_CHILD: 4,
  /** 仅本人数据 */
  SELF: 5,
} as const

export const DATA_SCOPE_LABEL: Record<number, string> = {
  [DATA_SCOPE.ALL]: '全部数据',
  [DATA_SCOPE.CUSTOM]: '自定义数据',
  [DATA_SCOPE.DEPT]: '本部门数据',
  [DATA_SCOPE.DEPT_AND_CHILD]: '本部门及以下',
  [DATA_SCOPE.SELF]: '仅本人数据',
}

export const DATA_SCOPE_OPTIONS: SelectOption[] = [
  { label: '全部数据', value: DATA_SCOPE.ALL },
  { label: '自定义数据', value: DATA_SCOPE.CUSTOM },
  { label: '本部门数据', value: DATA_SCOPE.DEPT },
  { label: '本部门及以下', value: DATA_SCOPE.DEPT_AND_CHILD },
  { label: '仅本人数据', value: DATA_SCOPE.SELF },
]

/**
 * 字典标签回显样式预设主题（对应 Naive UI NTag 的 type 属性）
 * 用于字典数据的 listClass 字段，控制消费端标签颜色
 * 除预设主题外，listClass 也支持填入任意 hex 色值（如 '#ff6b6b'），渲染时自动识别
 */
export const LIST_CLASS_OPTIONS: SelectOption[] = [
  { label: '默认', value: 'default' },
  { label: '主要', value: 'primary' },
  { label: '信息', value: 'info' },
  { label: '成功', value: 'success' },
  { label: '警告', value: 'warning' },
  { label: '错误', value: 'error' },
]