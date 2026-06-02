<template>
  <div class="notifications-page">
    <div class="page-header">
      <h2 class="page-title">消息通知</h2>
      <div class="header-actions">
        <el-button v-if="store.unreadCount > 0" text type="primary" @click="store.markAllAsRead()">
          全部已读
        </el-button>
        <el-popconfirm
          v-if="store.notifications.length > 0"
          title="确定删除全部通知？"
          @confirm="handleDeleteAll"
        >
          <template #reference>
            <el-button text type="danger">清空通知</el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="5" animated />

    <el-result
      v-else-if="error"
      icon="error"
      title="加载失败"
      sub-title="无法获取通知列表，请重试"
    >
      <template #extra>
        <el-button type="primary" @click="loadData">重新加载</el-button>
      </template>
    </el-result>

    <el-empty v-else-if="store.notifications.length === 0" description="暂无通知" />

    <div v-else class="notification-list">
      <div
        v-for="item in store.notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleClick(item)"
      >
        <div class="notif-left">
          <div class="notif-icon" :class="'type-' + item.type">
            <el-icon :size="16"><Bell /></el-icon>
          </div>
        </div>
        <div class="notif-body">
          <div class="notif-title">{{ item.title }}</div>
          <div class="notif-content">{{ item.content }}</div>
          <div class="notif-time">{{ formatTime(item.createdAt) }}</div>
        </div>
        <div v-if="item.isRead === 0" class="notif-dot"></div>
        <el-button
          class="notif-delete"
          text
          circle
          size="small"
          @click.stop="handleDelete(item.id)"
        >
          <el-icon :size="14"><Close /></el-icon>
        </el-button>
      </div>

      <div v-if="store.total > 20" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="20"
          :total="store.total"
          layout="prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Close } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'
import { notificationApi } from '@/api/modules/notification'
import { ElMessage } from 'element-plus'
import type { Notification } from '@/types'

const router = useRouter()
const store = useNotificationStore()
const loading = ref(false)
const error = ref(false)
const page = ref(1)

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const now = Date.now()
  const date = new Date(dateStr).getTime()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return dateStr.slice(0, 10)
}

const loadData = async () => {
  loading.value = true
  error.value = false
  try {
    await store.fetchList(page.value)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const handleClick = (item: Notification) => {
  if (item.isRead === 0) {
    store.markAsRead(item.id)
  }
  if (item.orderId) {
    router.push(`/orders/${item.orderId}`)
  }
}

const handleDelete = async (id: number) => {
  try {
    await notificationApi.deleteOne(id)
    const idx = store.notifications.findIndex(n => n.id === id)
    if (idx > -1) {
      const wasUnread = store.notifications[idx].isRead === 0
      store.notifications.splice(idx, 1)
      store.total--
      if (wasUnread) store.unreadCount = Math.max(0, store.unreadCount - 1)
    }
    ElMessage.success('已删除')
  } catch {
    // 响应拦截器已统一显示错误，此处静默
  }
}

const handleDeleteAll = async () => {
  try {
    await notificationApi.deleteAll()
    store.notifications.splice(0, store.notifications.length)
    store.total = 0
    store.unreadCount = 0
    ElMessage.success('已清空')
  } catch {
    // 响应拦截器已统一显示错误，此处静默
  }
}

onMounted(() => {
  store.fetchUnreadCount()
  loadData()
})
</script>

<style scoped>
.notifications-page {
  max-width: 680px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  letter-spacing: -0.3px;
  margin: 0;
}
.header-actions {
  display: flex;
  gap: 8px;
}

.notification-list {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  cursor: pointer;
  transition: background var(--transition-fast);
  position: relative;
}
.notification-item + .notification-item {
  border-top: 1px solid var(--border-color);
}
.notification-item:hover {
  background: var(--el-color-primary-light-9);
}
.notification-item.unread {
  background: var(--el-color-primary-light-9);
}
.notification-item.unread:hover {
  background: var(--el-color-primary-light-8);
}

.notif-left {
  flex-shrink: 0;
  margin-right: 14px;
}
.notif-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.notif-icon.type-CHAT { background: #f59e0b; }
.notif-icon.type-ORDER_CREATED,
.notif-icon.type-ORDER_PAID { background: #4f6ef7; }
.notif-icon.type-ORDER_SHIPPED { background: #10b981; }
.notif-icon.type-ORDER_COMPLETED { background: #10b981; }
.notif-icon.type-ORDER_CANCELLED { background: #94a3b8; }

.notif-body { flex: 1; min-width: 0; }
.notif-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.notif-content {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notif-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-primary);
  flex-shrink: 0;
  margin-top: 6px;
  margin-left: 8px;
}

.notif-delete {
  flex-shrink: 0;
  margin-left: 8px;
  margin-top: 2px;
  opacity: 0;
  color: var(--text-muted);
  transition: opacity 0.2s, color 0.2s;
}
.notification-item:hover .notif-delete {
  opacity: 1;
}
.notif-delete:hover {
  color: var(--el-color-danger);
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
}
</style>
