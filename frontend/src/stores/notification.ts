import { ref } from 'vue'
import { defineStore } from 'pinia'
import { Client } from '@stomp/stompjs'
import { notificationApi } from '@/api/modules/notification'
import type { Notification, PageData } from '@/types'
import { ElMessage } from 'element-plus'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const chatUnreadCount = ref(0)
  const total = ref(0)
  const wsConnected = ref(false)
  let stompClient: Client | null = null
  const chatCallbacks: Array<(msg: Record<string, unknown>) => void> = []
  let pollTimer: ReturnType<typeof setInterval> | null = null

  const fetchList = async (page = 1, size = 20) => {
    try {
      const data: PageData<Notification> = await notificationApi.list(page, size)
      notifications.value = data.records
      total.value = data.total
    } catch {
      ElMessage.error('加载通知失败')
    }
  }

  const fetchUnreadCount = async () => {
    try {
      unreadCount.value = await notificationApi.getUnreadCount()
    } catch { /* silent */ }
  }

  const markAsRead = async (id: number) => {
    try {
      await notificationApi.markAsRead(id)
      const n = notifications.value.find(item => item.id === id)
      if (n && n.isRead === 0) {
        n.isRead = 1
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    } catch { /* silent */ }
  }

  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead()
      notifications.value.forEach(n => { n.isRead = 1 })
      unreadCount.value = 0
    } catch {
      ElMessage.error('操作失败')
    }
  }

  const onNotification = (msg: Notification) => {
    console.log('[WS] Notification received:', msg.type, msg.title)
    notifications.value.unshift(msg)
    unreadCount.value++
    total.value++
  }

  const connect = () => {
    // WebSocket: 仅在已登录且浏览器支持时连接
    try {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      if (!token || !userStr) {
        console.log('[WS] Cannot connect: no token or user')
        return
      }

      const user = JSON.parse(userStr)
      if (!user.id) {
        console.log('[WS] Cannot connect: no user.id')
        return
      }

      if (stompClient?.active) {
        console.log('[WS] Already connected')
        return
      }

      // 延迟连接，避免阻塞页面首次渲染
      setTimeout(() => {
        const wsUrl = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/api/ws`
        console.log('[WS] Connecting to:', wsUrl)
        stompClient = new Client({
          brokerURL: wsUrl,
          connectHeaders: { Authorization: `Bearer ${token}` },
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            wsConnected.value = true
            console.log('[WS] Connected, subscribing to queues...')
            try {
              stompClient!.subscribe(`/user/queue/notifications`, (message) => {
                try {
                  const body: Notification = JSON.parse(message.body)
                  console.log('[WS] Notification arrived:', body.type)
                  onNotification(body)
                } catch { /* ignore malformed */ }
              })
              stompClient!.subscribe(`/user/queue/chat`, (message) => {
                try {
                  const body = JSON.parse(message.body)
                  console.log('[WS] Chat message arrived:', body.content)
                  // 收到私信就 +1，确保用户在任何页面都能看到角标
                  chatUnreadCount.value++
                  // 转发给 Chat.vue 回调处理会话列表更新和消息展示
                  chatCallbacks.forEach(cb => cb(body))
                } catch { /* ignore malformed */ }
              })
              console.log('[WS] Subscribed OK')
            } catch { /* subscribe failed */ }
          },
          onDisconnect: () => {
            wsConnected.value = false
            console.log('[WS] Disconnected')
          },
          onStompError: () => {
            wsConnected.value = false
            console.log('[WS] STOMP error')
          },
        })
        stompClient!.activate()
        console.log('[WS] Activating...')
      }, 1000)
    } catch {
      console.log('[WS] Connection setup failed')
    }
  }

  const startPolling = () => {
    stopPolling()
    pollTimer = setInterval(() => {
      if (!wsConnected.value) {
        fetchUnreadCount()
      }
    }, 30000)
  }

  const stopPolling = () => {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  const disconnect = () => {
    try {
      stompClient?.deactivate()
    } catch { /* ignore */ }
    stompClient = null
    wsConnected.value = false
  }

  const subscribeChat = (callback: (msg: Record<string, unknown>) => void) => {
    chatCallbacks.push(callback)
    return () => {
      const idx = chatCallbacks.indexOf(callback)
      if (idx > -1) chatCallbacks.splice(idx, 1)
    }
  }

  return {
    notifications,
    unreadCount,
    chatUnreadCount,
    total,
    wsConnected,
    fetchList,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    connect,
    disconnect,
    subscribeChat,
    startPolling,
    stopPolling,
  }
})
