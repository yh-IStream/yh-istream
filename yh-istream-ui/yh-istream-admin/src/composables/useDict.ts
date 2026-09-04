/**
 * 字典数据组合式函数
 * 封装字典数据的获取、缓存与标签渲染，消除各页面重复的字典加载与渲染逻辑
 * 缓存基于模块级 Map，同一字典类型全应用只请求一次
 * tagMap 注册表确保 clearDict 后消费端自动重载，无需刷新页面
 */
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
  /**
   * 加载指定类型的字典数据，带模块级缓存
   * @param dictType 字典类型标识，如 'sys_user_sex'
   */
  async function loadDict(dictType: string): Promise<DictOption[]> {
    if (dictCache.has(dictType)) {
      return dictCache.get(dictType)!
    }
    try {
      const res: any = await getDictDataByType(dictType)
      const list: DictOption[] = (res.data ?? []).map((item: any) => ({
        label: item.dictLabel,
        value: item.dictValue,
        listClass: item.listClass,
        cssClass: item.cssClass,
      }))
      dictCache.set(dictType, list)
      return list
    } catch {
      return []
    }
  }

  /**
   * 清除字典缓存并重载消费端 tagMap
   * 字典数据变更后调用，消费页面无需刷新即可看到最新数据
   * @param dictType 指定清除的类型；不传则清除全部
   */
  function clearDict(dictType?: string) {
    if (dictType) {
      dictCache.delete(dictType)
      const tagMap = tagMapRegistry.get(dictType)
      if (tagMap) {
        loadDict(dictType).then(list => {
          tagMap.value = new Map(list.map(item => [item.value, item]))
        })
      }
    } else {
      dictCache.clear()
      for (const [type, tagMap] of tagMapRegistry) {
        loadDict(type).then(list => {
          tagMap.value = new Map(list.map(item => [item.value, item]))
        })
      }
    }
  }

  /**
   * 创建字典标签渲染器（响应式，全局单例）
   * 同一 dictType 共享同一个 tagMap ref，clearDict 后自动重载
   * @param dictType 字典类型标识
   * @param fallback 未匹配时的默认标签文本
   */
  function useDictTag(dictType: string, fallback = '未知') {
    let tagMap = tagMapRegistry.get(dictType)
    if (!tagMap) {
      tagMap = ref<Map<string, DictOption>>(new Map())
      tagMapRegistry.set(dictType, tagMap)
      loadDict(dictType).then(list => {
        tagMap!.value = new Map(list.map(item => [item.value, item]))
      })
    }

    function render(rawValue: unknown) {
      const item = tagMap!.value.get(String(rawValue))
      if (!item) return fallback
      return renderDictTag(item)
    }

    return { render }
  }

  return {
    loadDict,
    clearDict,
    useDictTag,
  }
}