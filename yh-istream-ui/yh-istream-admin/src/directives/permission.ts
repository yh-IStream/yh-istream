import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

function hasPermission(binding: DirectiveBinding): boolean {
  const authStore = useAuthStore()
  const { value } = binding
  if (!value) return true
  if (typeof value === 'string') {
    return authStore.permissions.includes(value)
  }
  if (Array.isArray(value)) {
    const mode = binding.arg === 'and' ? 'every' : 'some'
    return value[mode]((perm: string) => authStore.permissions.includes(perm))
  }
  return true
}

export const vPermission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    if (!hasPermission(binding)) {
      el.parentNode?.removeChild(el)
    }
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const oldVisible = binding.oldValue !== undefined && hasPermission({ ...binding, value: binding.oldValue } as DirectiveBinding)
    const newVisible = hasPermission(binding)
    if (oldVisible !== newVisible) {
      if (newVisible) {
        el.style.display = ''
      } else {
        el.style.display = 'none'
      }
    }
  },
}