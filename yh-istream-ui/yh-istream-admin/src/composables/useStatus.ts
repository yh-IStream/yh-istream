/**
 * 状态渲染组合式函数
 * 字典驱动渲染，颜色取自 sys_status / sys_login_status 的 listClass 字段
 * 模块级单例：全局只创建一次字典渲染器，所有页面复用同一 tagMap，避免重复 API 请求
 */
import { useDict } from '@/composables/useDict'

const { useDictTag } = useDict()
const { render: renderStatusFromDict } = useDictTag('sys_status', '未知')
const { render: renderLoginStatusFromDict } = useDictTag('sys_login_status', '未知')

export function useStatusRender() {
  /** 渲染正常/停用状态标签（字典驱动） */
  function renderStatusTag(status: number) {
    return renderStatusFromDict(status)
  }

  /** 渲染成功/失败状态标签（字典驱动） */
  function renderSuccessTag(status: number) {
    return renderLoginStatusFromDict(status)
  }

  return {
    renderStatusTag,
    renderSuccessTag,
  }
}