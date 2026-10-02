<template>
  <n-card
    :title="`AI 建议：${suggestion.action}`"
    size="small"
    :bordered="true"
    class="ai-suggestion-card"
  >
    <template #header-extra>
      <n-tag :type="statusTagType" size="small">
        {{ statusLabel }}
      </n-tag>
    </template>

    <n-descriptions label-placement="left" :column="1" size="small">
      <n-descriptions-item label="实体">
        {{ suggestion.entityType }} / {{ suggestion.entityId }}
      </n-descriptions-item>
      <n-descriptions-item label="置信度">
        <n-progress
          type="line"
          :percentage="Math.round(suggestion.confidence * 100)"
          :color="confidenceColor"
          :height="16"
          :show-indicator="true"
        />
      </n-descriptions-item>
      <n-descriptions-item label="分析结论">
        {{ suggestion.conclusion }}
      </n-descriptions-item>
      <n-descriptions-item v-if="suggestion.suggestTime" label="建议时间">
        {{ suggestion.suggestTime }}
      </n-descriptions-item>
    </n-descriptions>

    <template #action>
      <n-space justify="end">
        <n-button
          v-if="suggestion.status === 0"
          type="error"
          ghost
          size="small"
          @click="handleReject"
        >
          拒绝
        </n-button>
        <n-button
          v-if="suggestion.status === 0"
          type="primary"
          size="small"
          @click="handleConfirm"
        >
          确认执行
        </n-button>
      </n-space>
    </template>
  </n-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NTag, NDescriptions, NDescriptionsItem, NProgress, NButton, NSpace, useMessage } from 'naive-ui'
import { confirmSuggestion, rejectSuggestion, type ActionSuggestion } from '../../api/modules/ai'

const props = defineProps<{
  suggestion: ActionSuggestion
}>()

const emit = defineEmits<{
  confirmed: [id: number]
  rejected: [id: number]
}>()

const message = useMessage()

const statusLabel = computed(() => {
  switch (props.suggestion.status) {
    case 0: return '待确认'
    case 1: return '已执行'
    case 2: return '已拒绝'
    default: return '未知'
  }
})

const statusTagType = computed(() => {
  switch (props.suggestion.status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'default'
    default: return 'default'
  }
})

const confidenceColor = computed(() => {
  if (props.suggestion.confidence >= 0.9) return '#d03050'
  if (props.suggestion.confidence >= 0.7) return '#f0a020'
  return '#2080f0'
})

async function handleConfirm() {
  try {
    await confirmSuggestion(props.suggestion.id)
    message.success('处置建议已确认执行')
    emit('confirmed', props.suggestion.id)
  } catch (e: unknown) {
    message.error(`确认失败：${e instanceof Error ? e.message : '未知错误'}`)
  }
}

async function handleReject() {
  try {
    await rejectSuggestion(props.suggestion.id)
    message.info('已拒绝处置建议')
    emit('rejected', props.suggestion.id)
  } catch (e: unknown) {
    message.error(`拒绝失败：${e instanceof Error ? e.message : '未知错误'}`)
  }
}
</script>

<style scoped>
.ai-suggestion-card {
  margin-bottom: 12px;
  transition: box-shadow 0.2s;
}

.ai-suggestion-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}
</style>