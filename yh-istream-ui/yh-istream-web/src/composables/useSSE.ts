import { getUnreadCount, getSseTicket } from '@/api/modules/message'
import { TOKEN_KEY } from '@/stores/auth'

const SSE_ENDPOINT = `${import.meta.env.VITE_API_BASE_URL ?? '/api/v1'}/sse/subscribe`
const HEARTBEAT_INTERVAL = 30_000
const HEARTBEAT_TIMEOUT = 45_000
const RECONNECT_BASE_DELAY = 1_000
const RECONNECT_MAX_DELAY = 30_000
const MAX_RECONNECT_ATTEMPTS = 10

type ConnectionStatus = 'connected' | 'reconnecting' | 'disconnected'
type MessageHandler = (data: unknown) => void

interface SSEMessage {
  type: string
  data: unknown
  timestamp: string
}

/**
 * 后端通过 SseEmitter.event().name(type.toLowerCase()) 发送命名事件，
 * 因此 SSE event: 字段值为小写事件类型。前端必须用 addEventListener 按名监听。
 */
const SSE_EVENT_NAMES = ['data_change', 'notification', 'system', 'heartbeat', 'connected', 'ai_insight']

function createSSE() {
  const status = ref<ConnectionStatus>('disconnected')
  const unreadCount = ref(0)
  let eventSource: EventSource | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectAttempts = 0
  let lastEventTime = 0
  const handlers = new Map<string, Set<MessageHandler>>()

  function scheduleReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      status.value = 'disconnected'
      return
    }
    const delay = Math.min(RECONNECT_BASE_DELAY * Math.pow(2, reconnectAttempts), RECONNECT_MAX_DELAY)
    reconnectAttempts++
    status.value = 'reconnecting'
    reconnectTimer = setTimeout(connect, delay)
  }

  function startHeartbeat() {
    stopHeartbeat()
    lastEventTime = Date.now()
    heartbeatTimer = setInterval(() => {
      if (!eventSource || eventSource.readyState === EventSource.CLOSED) {
        return
      }
      if (Date.now() - lastEventTime > HEARTBEAT_TIMEOUT) {
        eventSource.close()
        eventSource = null
        stopHeartbeat()
        scheduleReconnect()
      }
    }, HEARTBEAT_INTERVAL)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data?.count ?? 0
    } catch {
      // 获取未读数失败静默处理
    }
  }

  function dispatchMessage(event: MessageEvent) {
    try {
      const msg: SSEMessage = JSON.parse(event.data)
      if (msg.type === 'HEARTBEAT') {
        return
      }

      const typeHandlers = handlers.get(msg.type)
      if (typeHandlers) {
        for (const handler of typeHandlers) {
          handler(msg.data)
        }
      }

      const allHandlers = handlers.get('*')
      if (allHandlers) {
        for (const handler of allHandlers) {
          handler(msg)
        }
      }
    } catch {
      // JSON 解析失败静默处理
    }
  }

  function onNamedEvent(event: MessageEvent) {
    lastEventTime = Date.now()
    dispatchMessage(event)
  }

  async function connect() {
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) {
      return
    }

    disconnect(false)

    let ticket: string
    try {
      const res = await getSseTicket()
      ticket = res.data?.ticket
      if (!ticket) {
        scheduleReconnect()
        return
      }
    } catch {
      scheduleReconnect()
      return
    }

    if (status.value === 'disconnected' && reconnectTimer === null) {
      return
    }

    const url = `${SSE_ENDPOINT}?ticket=${encodeURIComponent(ticket)}`
    eventSource = new EventSource(url)

    for (const name of SSE_EVENT_NAMES) {
      eventSource.addEventListener(name, onNamedEvent)
    }
    eventSource.onmessage = onNamedEvent

    eventSource.onopen = () => {
      status.value = 'connected'
      reconnectAttempts = 0
      startHeartbeat()
      fetchUnreadCount()
    }

    eventSource.onerror = () => {
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      stopHeartbeat()
      scheduleReconnect()
    }
  }

  function disconnect(resetStatus = true) {
    stopHeartbeat()
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    reconnectAttempts = 0
    if (resetStatus) {
      status.value = 'disconnected'
      handlers.clear()
    }
  }

  function on(type: string, handler: MessageHandler) {
    if (!handlers.has(type)) {
      handlers.set(type, new Set())
    }
    handlers.get(type)!.add(handler)
    return () => {
      handlers.get(type)?.delete(handler)
    }
  }

  function off(type: string, handler: MessageHandler) {
    handlers.get(type)?.delete(handler)
  }

  function decrementUnread() {
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  }

  function resetUnread() {
    unreadCount.value = 0
  }

  return {
    status,
    unreadCount,
    connect,
    disconnect,
    on,
    off,
    fetchUnreadCount,
    decrementUnread,
    resetUnread,
  }
}

let instance: ReturnType<typeof createSSE> | null = null

export function useSSE() {
  if (!instance) {
    instance = createSSE()
  }
  return instance
}