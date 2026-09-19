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
      el.style.display = 'none'
    }
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    el.style.display = hasPermission(binding) ? '' : 'none'
  },
}