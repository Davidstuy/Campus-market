<template>
  <div class="chat-page">
    <!-- 左栏：会话列表 -->
    <div class="chat-sidebar" :class="{ hidden: activeConversation }">
      <div class="sidebar-header">
        <h3>会话列表</h3>
      </div>

      <el-skeleton v-if="loading" :rows="5" animated style="padding:16px" />

      <el-result
        v-else-if="error"
        icon="error"
        title="加载失败"
        sub-title="请检查网络后重试"
      >
        <template #extra>
          <el-button type="primary" @click="loadConversations">重新加载</el-button>
        </template>
      </el-result>

      <div v-else-if="conversations.length === 0" class="empty-chat">
        <el-icon :size="40" color="#cbd5e1"><ChatDotRound /></el-icon>
        <p>暂无消息</p>
        <span>浏览商品时点击"联系卖家"即可发起对话</span>
      </div>

      <div v-else class="conv-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conv-item"
          :class="{ active: activeConversation?.id === conv.id }"
          @click="openConversation(conv)"
        >
          <el-avatar :size="40" :src="conv.otherPartyAvatar" />
          <div class="conv-info">
            <div class="conv-top">
              <span class="conv-name">{{ conv.otherPartyName }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMessageAt) }}</span>
            </div>
            <div class="conv-bottom">
              <span class="conv-preview">{{ conv.lastMessage || '暂无消息' }}</span>
              <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" :max="99" />
            </div>
            <div v-if="conv.productTitle" class="conv-product">{{ conv.productTitle }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 中栏：聊天区域 -->
    <div class="chat-main" v-if="activeConversation">
      <div class="chat-header">
        <el-button text @click="closeChat" class="back-btn">
          <el-icon :size="20"><ArrowLeft /></el-icon>
        </el-button>
        <el-avatar :size="32" :src="activeConversation.otherPartyAvatar" />
        <div class="chat-header-info">
          <span class="chat-header-name">{{ activeConversation.otherPartyName }}</span>
          <span class="chat-header-product">{{ activeConversation.productTitle }}</span>
        </div>
      </div>

      <div class="msg-list" ref="msgListRef" @scroll="onScroll">
        <div v-if="hasMore" class="load-more">
          <el-button text size="small" :loading="loadingMore" @click="loadMoreMessages">
            加载更多
          </el-button>
        </div>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-bubble"
          :class="{ mine: msg.senderId === currentUserId }"
        >
          <div class="bubble-content">{{ msg.content }}</div>
          <div class="bubble-time">{{ formatTime(msg.createdAt) }}</div>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="1"
          placeholder="输入消息..."
          resize="none"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" :disabled="!inputText.trim()" @click="sendMessage">
          发送
        </el-button>
      </div>
    </div>

    <!-- 未选中会话 -->
    <div v-else class="chat-main chat-empty-main">
      <el-icon :size="56" color="#e2e8f0"><ChatLineSquare /></el-icon>
      <p>选择一条消息开始聊天</p>
    </div>

    <!-- 右栏：卖家信息 -->
    <div v-if="activeConversation && !isMobile" class="chat-seller">
      <div class="seller-panel">
        <el-avatar :size="64" :src="activeConversation.otherPartyAvatar" />
        <h4 class="seller-name">{{ activeConversation.otherPartyName }}</h4>
        <p class="seller-product" v-if="activeConversation.productTitle">{{ activeConversation.productTitle }}</p>
        <el-button type="primary" @click="goToShop">进入店铺</el-button>
      </div>
    </div>

    <!-- 移动端：返回按钮 -->
    <teleport to="body">
      <div v-if="isMobile && activeConversation" class="mobile-backdrop" />
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, ChatLineSquare, ArrowLeft } from '@element-plus/icons-vue'
import { chatApi } from '@/api/modules/chat'
import { notificationApi } from '@/api/modules/notification'
import { useNotificationStore } from '@/stores/notification'
import { useAuthStore } from '@/stores/auth'
import type { Conversation, ChatMessage } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const notificationStore = useNotificationStore()

const conversations = ref<Conversation[]>([])
const activeConversation = ref<Conversation | null>(null)
const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const loading = ref(false)
const error = ref(false)
const loadingMore = ref(false)
const msgListRef = ref<HTMLElement>()
const currentPage = ref(1)
const hasMore = ref(true)
let unsubChat: (() => void) | null = null
let pollTimer: ReturnType<typeof setInterval> | null = null

const isMobile = ref(window.innerWidth < 768)
const currentUserId = computed(() => auth.user?.id || 0)
const otherPartyId = computed(() => {
  if (!activeConversation.value) return 0
  return activeConversation.value.buyerId === currentUserId.value
    ? activeConversation.value.sellerId
    : activeConversation.value.buyerId
})

const formatTime = (dateStr: string | null) => {
  if (!dateStr) return ''
  const now = Date.now()
  const date = new Date(dateStr).getTime()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  const mins = Math.floor(diff / 60000)
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  return dateStr.slice(0, 10)
}

const loadConversations = async () => {
  loading.value = true
  error.value = false
  try {
    conversations.value = await chatApi.listConversations()
    notificationStore.chatUnreadCount = conversations.value.reduce((sum, c) => sum + c.unreadCount, 0)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  if (!activeConversation.value) return
  try {
    const data = await chatApi.getMessages(activeConversation.value.id, 1)
    messages.value = data.records.reverse()
    hasMore.value = data.total > data.records.length
    currentPage.value = 1
    // 标记已读
    chatApi.markRead(activeConversation.value.id)
    // 清除通知铃铛中的 CHAT 未读（先标记后拉取，避免竞态）
    await notificationApi.markChatRead()
    notificationStore.fetchUnreadCount()
    // 更新本地未读数
    const conv = conversations.value.find(c => c.id === activeConversation.value!.id)
    if (conv) conv.unreadCount = 0
  } catch {
    ElMessage.error('加载消息失败')
  }
}

const loadMoreMessages = async () => {
  if (!activeConversation.value || loadingMore.value) return
  loadingMore.value = true
  try {
    const data = await chatApi.getMessages(activeConversation.value.id, currentPage.value + 1)
    if (data.records.length > 0) {
      messages.value = [...data.records.reverse(), ...messages.value]
      currentPage.value++
      hasMore.value = data.total > currentPage.value * 30
    } else {
      hasMore.value = false
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loadingMore.value = false
  }
}

const openConversation = (conv: Conversation) => {
  activeConversation.value = conv
  loadMessages()
  startPolling()
}

const closeChat = () => {
  stopPolling()
  activeConversation.value = null
  messages.value = []
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (!activeConversation.value || !auth.isLoggedIn()) return
    try {
      const data = await chatApi.getMessages(activeConversation.value.id, 1, 5)
      if (data.records.length === 0) return
      // 取本次最新的消息 ID（排在前面）
      const existingIds = new Set(messages.value.map(m => m.id))
      const newMsgs = data.records
        .filter(m => !existingIds.has(m.id))
        .reverse() // 时间正序
      if (newMsgs.length > 0) {
        messages.value.push(...newMsgs)
        chatApi.markRead(activeConversation.value.id)
        scrollToBottom()
      }
    } catch { /* silent poll */ }
  }, 15000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content || !activeConversation.value) return

  const receiverId = activeConversation.value.buyerId === currentUserId.value
    ? activeConversation.value.sellerId
    : activeConversation.value.buyerId

  inputText.value = ''
  try {
    const msg = await chatApi.sendMessage(activeConversation.value.id, receiverId, content)
    messages.value.push(msg)
    // 更新会话列表
    const conv = conversations.value.find(c => c.id === activeConversation.value!.id)
    if (conv) {
      conv.lastMessage = content
      conv.lastMessageAt = msg.createdAt
    }
    scrollToBottom()
  } catch {
    ElMessage.error('发送失败')
    inputText.value = content
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

const goToShop = () => {
  if (otherPartyId.value) {
    router.push(`/shop/${otherPartyId.value}`)
  }
}

const onScroll = () => {
  if (!msgListRef.value) return
  if (msgListRef.value.scrollTop === 0 && hasMore.value) {
    loadMoreMessages()
  }
}

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(async () => {
  notificationStore.chatUnreadCount = 0
  await loadConversations()
  window.addEventListener('resize', handleResize)

  // WebSocket 监听实时消息
  unsubChat = notificationStore.subscribeChat((msg: Record<string, unknown>) => {
    const chatMsg = msg as unknown as ChatMessage
    // 更新或创建会话
    const conv = conversations.value.find(c =>
      c.id === chatMsg.conversationId
    )
    if (conv) {
      conv.lastMessage = chatMsg.content
      conv.lastMessageAt = chatMsg.createdAt
      if (activeConversation.value?.id !== chatMsg.conversationId) {
        conv.unreadCount++
      }
    }

    // 如果当前正在看这个会话，直接添加到消息列表
    if (activeConversation.value?.id === chatMsg.conversationId) {
      messages.value.push(chatMsg)
      chatApi.markRead(activeConversation.value.id)
      scrollToBottom()
    }
  })

  // 如果 URL 带 conversation 参数，自动打开
  const convId = route.query.conversation
  if (convId) {
    const existingConv = conversations.value.find(c => c.id === Number(convId))
    if (existingConv) {
      openConversation(existingConv)
    }
  }
})

onUnmounted(() => {
  if (unsubChat) unsubChat()
  stopPolling()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - var(--header-height) - 24px);
  max-width: 1000px;
  margin: 12px auto;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ===== 左栏：会话列表 ===== */
.chat-sidebar {
  width: 260px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}
.sidebar-header h3 {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 700;
}
.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--text-muted);
  padding: 20px;
}
.empty-chat p {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
}
.empty-chat span {
  font-size: var(--text-xs);
}

.conv-list { flex: 1; overflow-y: auto; }
.conv-item {
  display: flex;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--border-color);
}
.conv-item:hover { background: var(--el-color-primary-light-9); }
.conv-item.active { background: var(--el-color-primary-light-8); }

.conv-info { flex: 1; min-width: 0; }
.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}
.conv-name { font-size: var(--text-sm); font-weight: 600; }
.conv-time { font-size: 11px; color: var(--text-muted); flex-shrink: 0; margin-left: 6px; }
.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-preview {
  font-size: var(--text-xs);
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.conv-product {
  font-size: 11px;
  color: var(--el-color-primary);
  margin-top: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 中栏：聊天主区域 ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-empty-main {
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--text-muted);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}
.back-btn { display: none; }
.chat-header-info { display: flex; flex-direction: column; }
.chat-header-name { font-size: var(--text-sm); font-weight: 600; }
.chat-header-product { font-size: 11px; color: var(--text-muted); }

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}
.load-more { text-align: center; margin-bottom: 10px; }

.msg-bubble {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
  max-width: 70%;
}
.msg-bubble.mine { align-self: flex-end; align-items: flex-end; }

.bubble-content {
  background: var(--el-color-primary-light-9);
  padding: 8px 12px;
  border-radius: 10px;
  font-size: var(--text-sm);
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}
.msg-bubble.mine .bubble-content {
  background: var(--el-color-primary);
  color: #fff;
}
.bubble-time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

.chat-input-area {
  padding: 8px 12px;
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 8px;
  align-items: flex-end;
  flex-shrink: 0;
}
.chat-input-area :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
}

/* ===== 右栏：卖家面板 ===== */
.chat-seller {
  width: 200px;
  flex-shrink: 0;
  border-left: 1px solid var(--border-color);
  background: var(--bg-page);
}
.seller-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 16px 20px;
  gap: 10px;
  text-align: center;
}
.seller-panel .seller-name {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 600;
}
.seller-panel .seller-product {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--text-muted);
}
.seller-panel .el-button {
  margin-top: 8px;
  width: 100%;
}

/* ===== 移动端 ===== */
@media (max-width: 768px) {
  .chat-page {
    height: calc(100vh - 56px);
    margin: 0;
    border-radius: 0;
    border: none;
    max-width: none;
  }
  .chat-sidebar {
    width: 100%;
    border-right: none;
  }
  .chat-sidebar.hidden { display: none; }
  .chat-main {
    position: fixed;
    inset: 56px 0 0 0;
    z-index: 50;
    background: var(--bg-white);
  }
  .chat-empty-main {
    position: static;
    flex: 1;
  }
  .back-btn { display: inline-flex; }
  .msg-bubble { max-width: 85%; }
}
</style>
