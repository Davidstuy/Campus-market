<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'

const auth = useAuthStore()
const notification = useNotificationStore()

let visibilityHandler: (() => void) | null = null

onMounted(() => {
  if (auth.isLoggedIn()) {
    auth.fetchUser()
    notification.connect()
    notification.startPolling()
    notification.fetchUnreadCount()
  }

  visibilityHandler = () => {
    if (document.visibilityState === 'visible' && auth.isLoggedIn()) {
      notification.fetchUnreadCount()
      if (!notification.wsConnected) {
        notification.connect()
      }
    }
  }
  document.addEventListener('visibilitychange', visibilityHandler)
})

onUnmounted(() => {
  notification.disconnect()
  notification.stopPolling()
  if (visibilityHandler) {
    document.removeEventListener('visibilitychange', visibilityHandler)
  }
})

watch(() => auth.token, (newToken) => {
  if (newToken) {
    notification.connect()
    notification.startPolling()
    notification.fetchUnreadCount()
  } else {
    notification.disconnect()
    notification.stopPolling()
  }
})
</script>
