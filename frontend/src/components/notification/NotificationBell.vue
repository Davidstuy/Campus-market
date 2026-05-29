<template>
  <el-badge :value="store.unreadCount" :hidden="store.unreadCount === 0" :max="99" class="bell-badge">
    <el-icon :size="20" class="bell-icon" @click="$router.push('/notifications')">
      <Bell />
    </el-icon>
  </el-badge>
</template>

<script setup lang="ts">
import { Bell } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'

const store = useNotificationStore()
</script>

<style scoped>
.bell-icon {
  cursor: pointer;
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}
.bell-icon:hover {
  color: var(--el-color-primary);
}
.bell-badge {
  display: flex;
  align-items: center;
}
.bell-badge :deep(.el-badge__content) {
  font-size: 10px;
}
</style>
