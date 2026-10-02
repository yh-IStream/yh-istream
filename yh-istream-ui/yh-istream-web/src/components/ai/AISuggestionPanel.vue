<template>
  <n-drawer v-model:show="visible" :width="420" placement="right">
    <n-drawer-content title="AI 处置建议" closable>

      <n-spin :show="loading">
        <n-empty v-if="suggestions.length === 0 && !loading" description="暂无待确认的 AI 建议" />

        <div v-else>
          <AISuggestionCard
            v-for="item in suggestions"
            :key="item.id"
            :suggestion="item"
            @confirmed="onSuggestionHandled"
            @rejected="onSuggestionHandled"
          />
        </div>
      </n-spin>

      <template #footer>
        <n-button type="primary" ghost block @click="fetchPending">
          刷新
        </n-button>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  NDrawer, NDrawerContent, NSpin, NEmpty, NButton, useMessage
} from 'naive-ui'
import { getPendingSuggestions, type ActionSuggestion } from '@/api/modules/ai'
import AISuggestionCard from './AISuggestionCard.vue'

const visible = defineModel<boolean>('show', { default: false })

const suggestions = ref<ActionSuggestion[]>([])
const loading = ref(false)
const message = useMessage()

const pendingCount = computed(() => suggestions.value.filter(s => s.status === 0).length)

async function fetchPending() {
  loading.value = true
  try {
    const { data } = await getPendingSuggestions()
    suggestions.value = data ?? []
  } catch (e: unknown) {
    message.error(`加载 AI 建议失败：${e instanceof Error ? e.message : '未知错误'}`)
  } finally {
    loading.value = false
  }
}

function onSuggestionHandled(id: number) {
  suggestions.value = suggestions.value.filter(s => s.id !== id)
}

onMounted(() => {
  fetchPending()
})

defineExpose({ fetchPending, pendingCount })
</script>