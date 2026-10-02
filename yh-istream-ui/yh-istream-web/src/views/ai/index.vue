<script setup lang="ts">
import {
  NCard, NTag, NDescriptions, NDescriptionsItem, NProgress,
  NButton, NSpace, NSpin, NEmpty, NGrid, NGi, NText, NH3,
  NDivider, NAlert, NTabs, NTabPane, NInputNumber, NInput,
  NSwitch, useMessage
} from 'naive-ui'
import {
  getPendingSuggestions, confirmSuggestion, rejectSuggestion,
  getMcpTools, triggerDemoDetection, triggerDemoDetectionBatch,
  guardOrder, guardConfig, guardExport, guardLogin,
  getAlertRecipients, addAlertRecipient, updateAlertRecipient, deleteAlertRecipient,
  type ActionSuggestion, type ToolDefinition, type AiAlertRecipient
} from '@/api/modules/ai'
import { onMounted, ref, reactive, computed } from 'vue'

const message = useMessage()

const suggestions = ref<ActionSuggestion[]>([])
const tools = ref<ToolDefinition[]>([])
const loading = ref(true)
const confirming = ref<Set<number>>(new Set())
const triggering = ref(false)
const lastTrigger = ref<string | null>(null)

const orderForm = reactive({ orderAmount: 89999, creditScore: 420, deliveryAddress: '新疆阿克苏', ordersInLastHour: 6 })
const configForm = reactive({ configKey: 'SYS_PAYMENT_ENABLED', changeDetail: 'true→false', recentChangesInMinutes: 3 })
const exportForm = reactive({ dataType: 'OperLog', recordCount: 520, dailyAvgRecords: 50, containsPii: true })
const loginForm = reactive({ loginCity: '深圳', usualCity: '北京', recentLoginCities: 3, secondsSinceLastLogin: 600 })

const pendingCount = computed(() => suggestions.value.filter(s => s.status === 0).length)

const statusLabel = (status: number) => {
  switch (status) {
    case 0: return '待确认'
    case 1: return '已执行'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

const statusTagType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'default'
    default: return 'default'
  }
}

const confidenceColor = (confidence: number) => {
  if (confidence >= 0.9) return '#d03050'
  if (confidence >= 0.7) return '#f0a020'
  return '#2080f0'
}

async function fetchData() {
  loading.value = true
  try {
    const [suggRes, toolsRes] = await Promise.all([
      getPendingSuggestions(),
      getMcpTools().catch(() => ({ data: [] as ToolDefinition[] })),
    ])
    suggestions.value = suggRes.data ?? []
    tools.value = toolsRes.data ?? []
  } catch (e: unknown) {
    message.error(`加载失败：${e instanceof Error ? e.message : '未知错误'}`)
  } finally {
    loading.value = false
  }
}

async function handleConfirm(id: number) {
  confirming.value.add(id)
  try {
    await confirmSuggestion(id)
    message.success('处置已执行')
    suggestions.value = suggestions.value.filter(s => s.id !== id)
  } catch (e: unknown) {
    message.error(`执行失败：${e instanceof Error ? e.message : '未知错误'}`)
  } finally {
    confirming.value.delete(id)
  }
}

async function handleReject(id: number) {
  confirming.value.add(id)
  try {
    await rejectSuggestion(id)
    message.info('已拒绝')
    suggestions.value = suggestions.value.filter(s => s.id !== id)
  } catch (e: unknown) {
    message.error(`操作失败：${e instanceof Error ? e.message : '未知错误'}`)
  } finally {
    confirming.value.delete(id)
  }
}

async function handleTrigger() {
  triggering.value = true
  try {
    const res = await triggerDemoDetection()
    const data = res.data
    message.success(`已触发（模拟）：${data.action}（置信度 ${Math.round(data.confidence * 100)}%）`)
    lastTrigger.value = `[模拟] ${data.action} → ${data.entityType}/${data.entityId}`
    setTimeout(() => fetchData(), 300)
  } catch (e: unknown) {
    message.error(`触发失败：${e instanceof Error ? e.message : '后端 AI 模块可能未启用'}`)
  } finally {
    triggering.value = false
  }
}

async function handleTriggerBatch() {
  triggering.value = true
  try {
    const res = await triggerDemoDetectionBatch()
    message.success(`批量触发（模拟） ${res.data?.length ?? 0} 条检测`)
    lastTrigger.value = `[模拟] 批量 ${res.data?.length ?? 0} 条`
    setTimeout(() => fetchData(), 500)
  } catch (e: unknown) {
    message.error(`触发失败：${e instanceof Error ? e.message : '后端 AI 模块可能未启用'}`)
  } finally {
    triggering.value = false
  }
}

async function handleGuard(action: string, fn: () => Promise<unknown>) {
  triggering.value = true
  try {
    await fn()
    message.success(`真实 AI 检测已触发：${action}，等待 AI 返回结果...`)
    lastTrigger.value = `[真实AI] ${action}`
    setTimeout(async () => {
      await fetchData()
      if (suggestions.value.length === 0) {
        message.warning('未收到 AI 建议 — AI 模型可能调用失败（限流/超时/无免费额度），请查看后端控制台日志')
      }
    }, 2000)
  } catch (e: unknown) {
    message.error(`AI 检测失败：${e instanceof Error ? e.message : '请确认 AI 模型连接正常'}`)
  } finally {
    triggering.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchRecipients()
})

const recipients = ref<AiAlertRecipient[]>([])
const newUserId = ref<number | null>(null)
const newUserName = ref('')
const recipientLoading = ref(false)

async function fetchRecipients() {
  try {
    const res = await getAlertRecipients()
    recipients.value = res.data ?? []
  } catch {
    // 静默处理
  }
}

async function handleAddRecipient() {
  if (!newUserId.value) {
    message.warning('请输入用户ID')
    return
  }
  recipientLoading.value = true
  try {
    await addAlertRecipient({
      userId: newUserId.value,
      userName: newUserName.value || undefined,
    })
    message.success('接收人已添加')
    newUserId.value = null
    newUserName.value = ''
    await fetchRecipients()
  } catch (e: unknown) {
    message.error(`添加失败：${e instanceof Error ? e.message : '未知错误'}`)
  } finally {
    recipientLoading.value = false
  }
}

async function handleToggleRecipient(recipient: AiAlertRecipient) {
  try {
    await updateAlertRecipient(recipient.id, { enabled: recipient.enabled === 1 ? 0 : 1 })
    await fetchRecipients()
  } catch {
    message.error('操作失败')
  }
}

async function handleDeleteRecipient(id: number) {
  try {
    await deleteAlertRecipient(id)
    message.success('已删除')
    await fetchRecipients()
  } catch {
    message.error('删除失败')
  }
}
</script>

<template>
  <div class="ai-page">
    <n-grid cols="1" responsive="screen" :x-gap="16" :y-gap="16">
      <n-gi>
        <n-card title="AI 智能守护中心" size="small" class="intro-card">
          <template #header-extra>
            <n-space align="center" :wrap="false">
              <n-tag type="info" size="small" round>
                {{ pendingCount }} 条待确认
              </n-tag>
              <n-tag v-if="lastTrigger" size="small" round
                :type="lastTrigger.includes('真实AI') ? 'success' : 'warning'">
                {{ lastTrigger }}
              </n-tag>
            </n-space>
          </template>
          <n-text depth="3">
            <p style="margin: 0 0 12px 0; line-height: 1.6;">
              当业务数据发生变更时，<code>@AnomalyGuard</code>（传感器）自动调用 AI 检测未知异常。
              发现异常后，AI 会建议处置操作，须经人工确认后由 <code>@Tool</code>（执行器）实际执行，
              形成从发现到处置的完整闭环。
            </p>
          </n-text>
        </n-card>
      </n-gi>

      <n-gi>
        <n-tabs type="segment" animated>
          <n-tab-pane name="real" tab="真实 AI 检测（需模型连接）">
            <n-alert type="success" closable :bordered="false" style="margin-bottom: 16px;">
              <template #header>
                调用 Spring AI 真实模型分析，建议配置后使用
              </template>
              后端 <code>istream.ai.enabled=true</code> +
              <code>spring.ai.openai.api-key=sk-xxx</code> 或 Ollama，AI 返回的结论会被卡片展示。
            </n-alert>

            <n-grid cols="2" :x-gap="12" :y-gap="12" responsive="screen">
              <n-gi>
                <n-card title="订单异常检测" size="small" :bordered="true">
                  <n-space vertical :size="8">
                    <n-space align="center"><n-text depth="3" style="width:80px;">金额</n-text><n-input-number v-model:value="orderForm.orderAmount" :min="0" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">信用分</n-text><n-input-number v-model:value="orderForm.creditScore" :min="0" :max="1000" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">地址</n-text><n-input v-model:value="orderForm.deliveryAddress" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">小时下单</n-text><n-input-number v-model:value="orderForm.ordersInLastHour" :min="0" size="small" style="flex:1" /></n-space>
                  </n-space>
                  <template #action>
                    <n-button type="primary" size="small" :loading="triggering"
                      @click="handleGuard('订单异常检测', () => guardOrder({ ...orderForm }))">
                      触发检测
                    </n-button>
                  </template>
                </n-card>
              </n-gi>

              <n-gi>
                <n-card title="配置变更检测" size="small" :bordered="true">
                  <n-space vertical :size="8">
                    <n-space align="center"><n-text depth="3" style="width:80px;">配置项</n-text><n-input v-model:value="configForm.configKey" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">变更详情</n-text><n-input v-model:value="configForm.changeDetail" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">分钟变更</n-text><n-input-number v-model:value="configForm.recentChangesInMinutes" :min="0" size="small" style="flex:1" /></n-space>
                  </n-space>
                  <template #action>
                    <n-button type="primary" size="small" :loading="triggering"
                      @click="handleGuard('配置变更检测', () => guardConfig({ ...configForm }))">
                      触发检测
                    </n-button>
                  </template>
                </n-card>
              </n-gi>

              <n-gi>
                <n-card title="数据导出检测" size="small" :bordered="true">
                  <n-space vertical :size="8">
                    <n-space align="center"><n-text depth="3" style="width:80px;">数据类型</n-text><n-input v-model:value="exportForm.dataType" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">导出条数</n-text><n-input-number v-model:value="exportForm.recordCount" :min="0" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">日均条数</n-text><n-input-number v-model:value="exportForm.dailyAvgRecords" :min="0" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">含隐私</n-text><n-switch v-model:value="exportForm.containsPii" size="small" /></n-space>
                  </n-space>
                  <template #action>
                    <n-button type="primary" size="small" :loading="triggering"
                      @click="handleGuard('数据导出检测', () => guardExport({ ...exportForm }))">
                      触发检测
                    </n-button>
                  </template>
                </n-card>
              </n-gi>

              <n-gi>
                <n-card title="异地登录检测" size="small" :bordered="true">
                  <n-space vertical :size="8">
                    <n-space align="center"><n-text depth="3" style="width:80px;">登录城市</n-text><n-input v-model:value="loginForm.loginCity" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">常用城市</n-text><n-input v-model:value="loginForm.usualCity" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">城市切换</n-text><n-input-number v-model:value="loginForm.recentLoginCities" :min="0" size="small" style="flex:1" /></n-space>
                    <n-space align="center"><n-text depth="3" style="width:80px;">上次间隔秒</n-text><n-input-number v-model:value="loginForm.secondsSinceLastLogin" :min="0" size="small" style="flex:1" /></n-space>
                  </n-space>
                  <template #action>
                    <n-button type="primary" size="small" :loading="triggering"
                      @click="handleGuard('异地登录检测', () => guardLogin({ ...loginForm }))">
                      触发检测
                    </n-button>
                  </template>
                </n-card>
              </n-gi>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="mock" tab="模拟触发（无需 AI，快速演示）">
            <n-alert type="warning" closable :bordered="false" style="margin-bottom: 16px;">
              跳过 AI 调用，使用预设场景数据演示完整闭环 UI。适合无模型环境快速预览。
            </n-alert>

            <n-space style="margin-bottom: 16px;">
              <n-button
                type="primary"
                size="medium"
                :loading="triggering"
                @click="handleTrigger"
              >
                随机触发一条 AI 检测
              </n-button>
              <n-button
                type="info"
                ghost
                size="medium"
                :loading="triggering"
                @click="handleTriggerBatch"
              >
                批量触发三条场景
              </n-button>
            </n-space>

            <n-text depth="3" style="font-size: 13px;">
              预设场景包括：订单金额异常 (92%)、高危操作 (88%)、异地登录 (85%)、误报演示 (35%)、数据泄露 (95%)
            </n-text>
          </n-tab-pane>

          <n-tab-pane name="recipient" tab="告警接收人">
            <n-alert type="info" closable :bordered="false" style="margin-bottom: 16px;">
              配置 AI 异常检测告警的接收人。添加后，AI 每次发现异常都会通过右上角铃铛实时通知对应账号。
              同时支持操作人本人接收（自动兜底）。
            </n-alert>

            <n-space align="center" style="margin-bottom: 16px;">
              <n-input-number v-model:value="newUserId" :min="1" placeholder="用户ID" style="width: 160px;" />
              <n-input v-model:value="newUserName" placeholder="用户名称（可选）" style="width: 200px;" />
              <n-button type="primary" size="small" :loading="recipientLoading" @click="handleAddRecipient">
                添加接收人
              </n-button>
            </n-space>

            <n-empty v-if="recipients.length === 0" description="暂无告警接收人，AI 告警仅推送给操作人" />

            <n-grid v-else cols="1" :x-gap="12" :y-gap="8">
              <n-gi v-for="r in recipients" :key="r.id">
                <n-card size="small" :bordered="true">
                  <n-space justify="space-between" align="center">
                    <n-space align="center">
                      <n-text strong>{{ r.userName || ('用户' + r.userId) }}</n-text>
                      <n-tag size="small" :type="r.enabled === 1 ? 'success' : 'default'">
                        {{ r.enabled === 1 ? '启用' : '停用' }}
                      </n-tag>
                      <n-text depth="3" style="font-size: 12px;">ID: {{ r.userId }}</n-text>
                    </n-space>
                    <n-space>
                      <n-switch
                        :value="r.enabled === 1"
                        @update:value="handleToggleRecipient(r)"
                        size="small"
                      />
                      <n-button text type="error" size="tiny" @click="handleDeleteRecipient(r.id)">
                        删除
                      </n-button>
                    </n-space>
                  </n-space>
                </n-card>
              </n-gi>
            </n-grid>
          </n-tab-pane>
        </n-tabs>
      </n-gi>

      <n-gi>
        <n-divider />
      </n-gi>

      <n-gi>
        <n-h3>
          <n-text>处置建议</n-text>
        </n-h3>
        <n-spin :show="loading">
          <n-empty v-if="suggestions.length === 0 && !loading"
            description="暂无 AI 建议，点击上方场景按钮触发检测后稍等 1-2 秒" />

          <n-grid v-else cols="2" :x-gap="12" :y-gap="12" responsive="screen">
            <n-gi v-for="item in suggestions" :key="item.id">
              <n-card
                :title="`AI 建议：${item.action}`"
                size="small"
                :bordered="true"
                :segmented="{ content: 'soft' }"
              >
                <template #header-extra>
                  <n-tag :type="statusTagType(item.status)" size="small">
                    {{ statusLabel(item.status) }}
                  </n-tag>
                </template>

                <n-descriptions label-placement="left" :column="1" size="small">
                  <n-descriptions-item label="实体">
                    {{ item.entityType }} / {{ item.entityId }}
                  </n-descriptions-item>
                  <n-descriptions-item label="置信度">
                    <n-progress
                      type="line"
                      :percentage="Math.round(item.confidence * 100)"
                      :color="confidenceColor(item.confidence)"
                      :height="16"
                      :show-indicator="true"
                    />
                  </n-descriptions-item>
                  <n-descriptions-item label="分析结论">
                    {{ item.conclusion }}
                  </n-descriptions-item>
                  <n-descriptions-item v-if="item.suggestTime" label="建议时间">
                    {{ item.suggestTime }}
                  </n-descriptions-item>
                </n-descriptions>

                <template v-if="item.status === 0" #action>
                  <n-space justify="end">
                    <n-button type="error" ghost size="small" :loading="confirming.has(item.id)"
                      @click="handleReject(item.id)">
                      拒绝
                    </n-button>
                    <n-button type="primary" size="small" :loading="confirming.has(item.id)"
                      @click="handleConfirm(item.id)">
                      确认执行
                    </n-button>
                  </n-space>
                </template>
              </n-card>
            </n-gi>
          </n-grid>
        </n-spin>
      </n-gi>

      <n-gi>
        <n-h3>
          <n-text>可用工具</n-text>
        </n-h3>
        <n-spin :show="loading">
          <n-empty v-if="tools.length === 0 && !loading"
            description="未扫描到 @Tool 方法（检查 istream.ai.enabled=true 且 GuardToolsDemo 已注册）" />

          <n-grid v-else cols="2" :x-gap="12" :y-gap="12" responsive="screen">
            <n-gi v-for="tool in tools" :key="tool.methodName">
              <n-card size="small" :bordered="true">
                <template #header>
                  <n-tag type="success" size="small" round>
                    @Tool
                  </n-tag>
                  <n-text strong style="margin-left: 8px;">{{ tool.methodName }}</n-text>
                </template>
                <n-text depth="3">{{ tool.description }}</n-text>
              </n-card>
            </n-gi>
          </n-grid>
        </n-spin>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped>
.ai-page {
  max-width: 1200px;
}

code {
  background: #f4f4f5;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 0.9em;
}

html.dark code {
  background: #3a3a3d;
}

:deep(.n-tabs-pane-wrapper) {
  padding-top: 0;
}
</style>