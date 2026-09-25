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

const placeholderMap = new WeakMap<HTMLElement, Comment>()

export const vPermission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    if (!hasPermission(binding)) {
      const placeholder = new Comment('v-permission')
      placeholderMap.set(el, placeholder)
      el.replaceWith(placeholder)
    }
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const placeholder = placeholderMap.get(el)
    const permitted = hasPermission(binding)
    if (!permitted && !placeholder) {
      const newPlaceholder = new Comment('v-permission')
      placeholderMap.set(el, newPlaceholder)
      el.replaceWith(newPlaceholder)
    } else if (permitted && placeholder) {
      placeholder.replaceWith(el)
      placeholderMap.delete(el)
    }
  },
  unmounted(el: HTMLElement) {
    const placeholder = placeholderMap.get(el)
    if (placeholder) {
      placeholder.remove()
      placeholderMap.delete(el)
    }
  },
}