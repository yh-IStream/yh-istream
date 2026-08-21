/**
 * 状态渲染组合式函数
 * 消除重复的 h(NTag, ...) 状态标签渲染模式
 */
import { STATUS_TAG_TYPE, STATUS_LABEL, SUCCESS_LABEL } from '@/constants'

export function useStatusRender() {
  /** 渲染正常/停用状态标签 */
  function renderStatusTag(status: number) {
    const type = STATUS_TAG_TYPE[status] ?? 'default'
    const label = STATUS_LABEL[status] ?? '未知'
    return h(NTag, { type, size: 'small' }, { default: () => label })
  }

  /** 渲染成功/失败状态标签 */
  function renderSuccessTag(status: number) {
    const type = STATUS_TAG_TYPE[status] ?? 'default'
    const label = SUCCESS_LABEL[status] ?? '未知'
    return h(NTag, { type, size: 'small' }, { default: () => label })
  }

  return {
    renderStatusTag,
    renderSuccessTag,
  }
}