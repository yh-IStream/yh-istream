/**
 * 字典数据组合式函数
 * 封装字典数据的获取、缓存与标签渲染，消除各页面重复的字典加载与渲染逻辑
 * 缓存基于模块级 Map，同一字典类型全应用只请求一次
 * tagMap 注册表确保 clearDict 后消费端自动重载，无需刷新页面
 * 请求版本号机制防止过时请求覆盖新数据（竞态安全）
 */
import { defineComponent, h, ref, type PropType, type Ref } from 'vue'
import { getDictDataByType } from '@/api/modules/system'

export interface DictOption {
  label: string
  value: string
  listClass?: string
  cssClass?: string
}

type TagType = 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'

const VALID_TAG_TYPES: Set<string> = new Set(['default', 'primary', 'info', 'success', 'warning', 'error'])

const HEX_COLOR_REGEX = /^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/

const dictCache = new Map<string, DictOption[]>()

const tagMapRegistry = new Map<string, Ref<Map<string, DictOption>>>()

const requestVersion = new Map<string, number>()

/**
 * 渲染单个字典标签（纯函数，无状态）
 * 颜色解析优先级：listClass 预设主题 > listClass hex 色值 > cssClass 自定义类名 > default
 * @param item 字典选项，含 label/listClass/cssClass
 */
export function renderDictTag(item: { label: string; listClass?: string; cssClass?: string }) {
  const listClass = item.listClass?.trim()
  const cssClass = item.cssClass?.trim()

  if (listClass && VALID_TAG_TYPES.has(listClass)) {
    return h(NTag, { type: listClass as TagType, size: 'small' }, { default: () => item.label })
  }
  if (listClass && HEX_COLOR_REGEX.test(listClass)) {
    return h(NTag, { color: { color: listClass, textColor: '#fff' }, size: 'small' }, { default: () => item.label })
  }
  if (cssClass) {
    return h('span', { class: cssClass }, item.label)
  }
  return h(NTag, { type: 'default' as TagType, size: 'small' }, { default: () => item.label })
}

export function useDict() {
  function syncTagMap(dictType: string, list: DictOption[]) {
    const tagMap = tagMapRegistry.get(dictType)
    if (tagMap) {
      tagMap.value = new Map(list.map(item => [item.value, item]))
    }
  }

  /**
   * 加载指定类型的字典数据，带模块级缓存
   * 加载完成后同步更新 tagMapRegistry，确保 useDictTag 的 render 能立即命中
   * @param dictType 字典类型标识，如 'sys_user_sex'
   */
  async function loadDict(dictType: string): Promise<DictOption[]> {
    if (dictCache.has(dictType)) {
      const list = dictCache.get(dictType)!
      syncTagMap(dictType, list)
      return list
    }
    const version = (requestVersion.get(dictType) ?? 0) + 1
    requestVersion.set(dictType, version)
    try {
      const res = await getDictDataByType(dictType)
      if (requestVersion.get(dictType) !== version) {
        return dictCache.get(dictType) ?? []
      }
      const list: DictOption[] = (res.data ?? []).map((item) => ({
        label: item.dictLabel,
        value: item.dictValue,
        listClass: item.listClass,
        cssClass: item.cssClass,
      }))
      dictCache.set(dictType, list)
      syncTagMap(dictType, list)
      return list
    } catch {
      if (requestVersion.get(dictType) === version) {
        requestVersion.delete(dictType)
      }
      return []
    }
  }

  function clearDict(dictType?: string) {
    if (dictType) {
      dictCache.delete(dictType)
      requestVersion.set(dictType, (requestVersion.get(dictType) ?? 0) + 1)
      loadDict(dictType)
    } else {
      dictCache.clear()
      for (const type of tagMapRegistry.keys()) {
        requestVersion.set(type, (requestVersion.get(type) ?? 0) + 1)
      }
      for (const type of tagMapRegistry.keys()) {
        loadDict(type)
      }
    }
  }

  /**
   * 创建字典标签渲染器（全局单例）
   * 同一 dictType 共享同一个 tagMap ref，clearDict 后自动重载
   * render 返回 defineComponent 组件实例，组件内部响应式读取 tagMap，字典加载完成后自动重渲染
   * @param dictType 字典类型标识
   * @param fallback 未匹配时的默认标签文本
   */
  function useDictTag(dictType: string, fallback = '未知') {
    let tagMap = tagMapRegistry.get(dictType)
    if (!tagMap) {
      tagMap = ref<Map<string, DictOption>>(new Map())
      tagMapRegistry.set(dictType, tagMap)
    }

    const cached = dictCache.get(dictType)
    if (cached && cached.length > 0) {
      tagMap.value = new Map(cached.map(item => [item.value, item]))
    } else if (!dictCache.has(dictType)) {
      loadDict(dictType)
    }

    const tagMapRef = tagMap!

    const DictTagComponent = defineComponent({
      name: `DictTag_${dictType}`,
      props: {
        rawValue: { type: [String, Number, Boolean] as PropType<string | number | boolean | null | undefined>, default: undefined },
      },
      setup(props) {
        return () => {
          const map = tagMapRef.value
          const item = map.get(String(props.rawValue))
          if (!item) return fallback
          return renderDictTag(item)
        }
      },
    })

    function render(rawValue: string | number | boolean | null | undefined) {
      return h(DictTagComponent, { rawValue })
    }

    return { render }
  }

  return {
    loadDict,
    clearDict,
    useDictTag,
  }
}